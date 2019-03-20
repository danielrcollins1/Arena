import java.util.List;
import java.util.ArrayList;

/******************************************************************************
*  One character (player or non-player personae).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-20
*  @version  1.01
******************************************************************************/

public class Character extends Monster {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Base age in years. */
	static final int BASE_AGE = 18;

	/** Base armor class. */
	static final int BASE_ARMOR_CLASS = 9;

	/** Base movement. */
	static final int BASE_MOVEMENT = 12;

	/** Base hit dice. */
	static final Dice BASE_HD = new Dice(1, 6);

	/** Base percent per level for magic items. */
	static final int BASE_MAGIC_PER_LEVEL = 5;

	/** Dice for ability scores. */
	static final Dice[] ABILITY_DICE = new Dice[] 
		{new Dice(3, 6), new Dice(2, 6, 6), 
		new Dice(2, 4, 10), new Dice(2, 3, 12)};


	/** Whether we apply the prime-requisite XP bonus. */
	static final boolean APPLY_BONUS_XP = false;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Age in years. */
	int age;

	/** Personal name of the character. */
	String name;

	/** The six ability scores. */
	int[] abilityScore;

	/** Ability score damage. */
	int[] abilityScoreDamage;

	/** List of classes with XP scores. */
	List<ClassRecord> classList;

	/** Armor worn. */
	Armor armor;

	/** Shield held. */
	Armor shield;

	/** Weapon in hand. */
	Weapon weaponInHand;	

	/** Magic ring. */
	Equipment ring;

	/** Magic wand. */
	Equipment wand;
	
	/** Equipment carried. */
	List<Equipment> equipList;

	/** Primary personality trait. */
	PersonalityTraits.PersonalityTrait primaryPersonality;
	
	/** Secondary personality trait. */
	PersonalityTraits.PersonalityTrait secondaryPersonality;

	/** Percent chance per level for magic items. */
	static int pctMagicPerLevel = BASE_MAGIC_PER_LEVEL;

	/** Assign feats to characters? */
	static boolean useFeats = false;

	/** Print feats in string descriptor? */
	static boolean printFeats = true;

	/** Print abilities in string descriptor? */
	static boolean printAbilities = true;

	/** Print equipment in string descriptor? */
	static boolean printEquipment = true;

	/** Print personality in string descriptor? */
	static boolean printPersonality = true;

	/** Print spells in string descriptor? */
	static boolean printSpells = true;

	/** Boost abilities & hit points at time of creation? */
	static boolean boostInitialAbilities = false;

	/** Apply aging effects? */
	static boolean applyAgingEffects = false;

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** 
	*  Age categories.
	*/
	public enum AgeCategory {
		Child, YoungAdult, Mature, MiddleAged, Old, Venerable;
	};

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (single class).
	*/
	public Character (String race, String classn, int level, String align) {
		super(race, BASE_ARMOR_CLASS, BASE_MOVEMENT, BASE_HD, null);

		assert(level >= 0); 
		age = BASE_AGE;
		name = NameGenerator.getInstance().getRandom();
		ClassType classType = ClassIndex.getInstance().getTypeFromName(classn);
		abilityScore = new int[Ability.length];
		rollAbilityScores(classType, level);
		abilityScoreDamage = new int[Ability.length];
		classList = new ArrayList<ClassRecord>(1);
		equipList = new ArrayList<Equipment>(4);
		classList.add(new ClassRecord(this, classType, level));
		alignment = getAlignmentFromString(align);
		primaryPersonality = PersonalityTraits.getInstance().getRandom(alignment);
		secondaryPersonality = PersonalityTraits.getInstance().getRandom(null);
		updateStats();
		setPerfectHealth();
	}

	/**
	*  Constructor (double class).
	*/
	public Character (String race, String class1, int level1, 
			String class2, int level2, String align) {
		this(race, class1, level1, align);	

		assert(level2 >= 0); 
		classList.clear();
		ClassIndex classIndex = ClassIndex.getInstance();
		ClassType classType1 = classIndex.getTypeFromName(class1);
		ClassType classType2 = classIndex.getTypeFromName(class2);
		rollAbilityScoresDblClass(classType1, level1, classType2, level2);
		classList.add(new ClassRecord(this, classType1, level1));
		classList.add(new ClassRecord(this, classType2, level2));
		updateStats();
		setPerfectHealth();			
	}
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Implement null Monster equipment methods
	public Armor getArmor () { return armor; }
	public Armor getShield () { return shield; }
	public Weapon getWeapon () { return weaponInHand; }

	// Set equipment carried
	public void addEquipment (Equipment equip) { equipList.add(equip); }
	public void dropAllEquipment () { equipList.clear(); }

	// Class-detecting methods
	public boolean isFighter () { return hasPrimeReqClass(Ability.Str); }
	public boolean isThief () { return hasPrimeReqClass(Ability.Dex); }
	public boolean isWizard () { return hasPrimeReqClass(Ability.Int); }

	/**
	*  Roll random ability scores for single-class character.
	*/
	private void rollAbilityScores (ClassType type, int level) {
		if (boostInitialAbilities) {
			rollPriorityAbilityScores(type.getAbilityPriority(), level);
		}
		else {		
			for (int i = 0; i < Ability.length; i++) {
				abilityScore[i] = ABILITY_DICE[0].roll();
			}
		}
	}

	/**
	*  Roll random ability scores for double-class character.
	*  (This is ugly b/c it needs to happen before we add class records.)
	*/
	private void rollAbilityScoresDblClass(ClassType type1, int level1, 
			ClassType type2, int level2) {
		if (boostInitialAbilities) {
			Ability[] priorityList;
			if (type1.getPrimeReq() == Ability.Int || type2.getPrimeReq() == Ability.Int) {
				priorityList = new Ability[] {Ability.Dex, Ability.Int, 
					Ability.Str, Ability.Con, Ability.Cha, Ability.Wis};
			}		
			else {
				priorityList = (level1 >= level2) ?
					type1.getAbilityPriority() : type2.getAbilityPriority();
			}
			rollPriorityAbilityScores(priorityList, Math.max(level1, level2));
		}			
	}

	/**
	*  Roll boosted random ability scores as per given priority.
	*/
	private void rollPriorityAbilityScores(Ability[] priorityList, int level) {
		for (int i = 0; i < Ability.length; i++) {
			int idx = priorityList[i].ordinal();
			abilityScore[idx] = getBoostedAbilityDice(level, i).roll();
 		}
	}

	/**
	*  Get ability dice boosted by level and priority.
	*  This is as per OED rule observed from Monte Carlo analysis.
	*/
	private Dice getBoostedAbilityDice (int level, int priority) {
		if (level <= 0) {
			return ABILITY_DICE[0];
		}	
		else if (level == 1) {
			switch (priority) {
				case 0: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}
		else if (level <= 4) {
			switch (priority) {
				case 0: case 1: case 2: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}
		else if (level <= 7) {
			switch (priority) {
				case 0: return ABILITY_DICE[2]; 
				case 1: case 2: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}
		else {
			switch (priority) {
				case 0: return ABILITY_DICE[3]; 
				case 1: return ABILITY_DICE[2];
				case 2: case 3: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}					
	}

	/**
	*  Get an ability score.
	*/
	public int getAbilityScore (Ability ability) {
		int idx = ability.ordinal();
		int score = abilityScore[idx];
		score -= abilityScoreDamage[idx];
		if (ability == Ability.Str && hasFeat(Feat.GreatStrength)) {
			score += 2;
		}
		return score;
	}

	/**
	*  Get an ability score bonus/modifier.
	*/
	public int getAbilityBonus (Ability ability) {
		int score = getAbilityScore(ability);
		return Ability.getBonus(score);
	}

	/*
	*  Take damage to an ability score.
	*/
	public void takeAbilityDamage (Ability ability, int damage) {
		abilityScoreDamage[ability.ordinal()] += damage;	
		updateStats();
	}

	/*
	*  Clear any ability score damage.
	*/
	public void zeroAbilityDamage () {
 		for (int i = 0; i < Ability.length; i++) {
 			abilityScoreDamage[i] = 0;
 		}
	}

	/*
	*  Adjust all ability scores by indicated modifiers.
	*/
	private void adjustAllAbilityScores (int... modifiers) {
		int oldCon = getAbilityScore(Ability.Con);
		int index = 0;
		for (int mod: modifiers) {
			abilityScore[index] += mod;		
			index++;
		}
		handleConChange(oldCon); 
		updateStats();
	}

	/**
	*  Handle a Constitution change to hit points.
	*/
	private void handleConChange (int oldCon) {
		for (ClassRecord record: classList) {
			record.handleConChange(oldCon);
		}
	}

	/**
	*  Are any of our ability scores zero?
	*  Because then we are dead.
	*/
	public boolean hasNullAbilityScore () {
		for (int a: abilityScore) {
			if (a <= 0) return true;
		}
		return false;
	}

	/**
	*  Update derived statistics after any character changes.
	*/
	private void updateStats () {
		attack = computeAttack();
		armorClass = computeArmorClass();	
		moveInches = computeMoveInches();
		hitDice = getTopClass().getHitDice();
		equivalentHitDice = hitDice.getNum();
		maxHitPoints = findSupMaxHitPoints();
		boundHitPoints();
	}

	/**
	*  Do we have any class with the given prime requisite?
	*/
	private boolean hasPrimeReqClass (Ability prime) {
		for (ClassRecord cr: classList)
			if (cr.getClassType().getPrimeReq() == prime)
				return true;
		return false;	
	}

	/**
	*  Get highest level class.
	*/
	public ClassRecord getTopClass() {
		ClassRecord topClass = classList.get(0);
		for (ClassRecord cr: classList) {
			if (cr.getLevel() > topClass.getLevel())
				topClass = cr;
		}
		return topClass;
	}

	/**
	*  Get highest class level.
	*/
	public int getLevel () {
		return getTopClass().getLevel();
	}

	/**
	*  Get raw number of hit dice.
	*  When "HD" referenced in RPG rules, it means "level" for PC/NPCs.
	*  E.g.: Attacks, saves, magic effects, XP awards.
	*/
	public int getHitDiceNum () {
		return Math.max(getLevel(), 1);
	}

	/**
	*  Get adjusted armor class.
	*/
	private int computeArmorClass() { 
		int AC = BASE_ARMOR_CLASS - getAbilityBonus(Ability.Dex);
		if (armor != null) {
			AC -= armor.getBaseArmor() + armor.getMagicBonus();
		}	
		if (shield != null) {
			AC -= shield.getBaseArmor() + shield.getMagicBonus();
		}
		if (ring != null) {
			AC -= ring.getMagicBonus();		
		}
		return AC;
	}

	/**
	*  Get movement based on armor.
	*/
	private int computeMoveInches() {
		return (armor == null) ? BASE_MOVEMENT : armor.getMaxMove();
	}

	/**
	*  Get weapon-based melee attack routine.
	*/
	private Attack computeAttack () { 
		if (weaponInHand == null) {
			return null;
		}	
		else {
			String name = weaponInHand.getName();
			int rate = (hasFeat(Feat.RapidStrike) ? 2 : 1);
			int atkBonus = baseAttackBonus() + getAbilityBonus(Ability.Str) 
				+ weaponInHand.getMagicBonus();
			Dice damageDice = new Dice(weaponInHand.getBaseDamage());
			int damageAdd = damageDice.getAdd() + getAbilityBonus(Ability.Str) 
				+ weaponInHand.getMagicBonus();
			if (hasFeat(Feat.WeaponSpecialization)) {
				atkBonus += 1;
				damageAdd += 2;
			}
			damageDice.setAdd(damageAdd);
			return new Attack(name, rate, atkBonus, damageDice);
		}	
	}

	/**
	*  Returns base attack bonus (max over all classes).
	*/
	private int baseAttackBonus () {
		int maxBAB = 0;
		for (ClassRecord record: classList) {
			if (record.attackBonus() > maxBAB)
				maxBAB = record.attackBonus();
		}
		return maxBAB;
	}

	/**
	*  Find what level of magic-to-hit we can strike.
	*/
	public int getMagicHitLevel () {
		return (weaponInHand == null ? 0 : weaponInHand.getMagicBonus());
	}

	/**
	*  Set armor worn.
	*/
	public void setArmor (Armor armor) { 
		this.armor = armor; 
		updateStats();
	}

	/**
	*  Set shield carried.
	*/
	public void setShield (Armor shield) { 
		this.shield = shield; 
		updateStats();
	}

	/**
	*  Draw a particular weapon from equipment.
	*/
	public void drawWeapon (Weapon weapon) {
		assert(weaponInHand == null);
		if (weapon.getHandsUsed() > 1 && shield != null) {
			equipList.add(shield);
			shield = null;		
		}
		equipList.remove(weapon);
		weaponInHand = weapon;
		updateStats();
	}

	/**
	*  Sheathe weapon in hand back to equipment.
	*/
	public void sheatheWeapon () {
		assert(weaponInHand != null);
		equipList.add(weaponInHand);	
		weaponInHand = null;
		updateStats();
	}

	/**
	*  Draw best weapon against a given monster.
	*/
	public void drawBestWeapon (Monster monster) {
		if (weaponInHand != null) {
			sheatheWeapon();
		}
		drawWeapon(bestWeaponAgainst(monster));	
	}

	/**
	*  Decide on the best weapon against a given monster.
	*  Considers only weapons in equipment list.
	*/
	private Weapon bestWeaponAgainst (Monster monster) {
		assert(weaponInHand == null);
		int maxDamage = -1;
		Weapon bestWeapon = null;
		for (Equipment equip: equipList) {
			if (equip instanceof Weapon) {
				weaponInHand = (Weapon) equip;
				updateStats();
				int damage = maxDamageVsMonster(monster);
				if (damage > maxDamage) {
					maxDamage = damage;
					bestWeapon = weaponInHand;
				}							
			}		
		}	
		weaponInHand = null;
		return bestWeapon;
	}

	/**
	*  Compute max damage against a given monster.
	*/
	private int maxDamageVsMonster (Monster monster) {
		Attack atk = getAttack();
		if (atk == null) return 0;
		if (monster != null && !canAttack(monster)) return 0;
		return atk.getDamage().maxRoll();		
	}

	/**
	*  Returns total XP (sum over all classes).
	*/
	public int totalXP () {
		int sum = 0;
		for (ClassRecord record: classList) {
			sum += record.getXP();
		}
		return sum;
	}

	/**
	*  Add XP to the first character class.
	*/
	public void addXP (int xp) {
		addXP(xp, 0);	
	}

	/**
	*  Add XP to a specified character class.
	*/
	public void addXP (int XP, int classIdx) {
		ClassRecord cr = classList.get(classIdx);
		XP = adjustForPrimeReq(XP, cr);
		cr.addXP(XP);
		updateStats();
	}

	/**
	*  Adjust XP award for the class prime requisite.
	*/
	private int adjustForPrimeReq (int XP, ClassRecord cr) {
		if (!APPLY_BONUS_XP) {
			return XP;
		}
		else {
			Ability primeReq = cr.getClassType().getPrimeReq();
			int score = getAbilityScore(primeReq);
			int bonusPct = Ability.bonusPercentXP(score);
			return XP + XP * bonusPct/100;
		}
	}

	/**
	*  Lose a level (e.g., energy drain).
	*/
	public void loseLevel () {
		int damage = maxHitPoints - hitPoints;
		getTopClass().loseLevel();
		updateStats();
		hitPoints = maxHitPoints - damage;
		boundHitPoints();
	}

	/**
	*  Find maximum hit points (supremum over all classes).
	*/
	private int findSupMaxHitPoints () {
		int supMaxHP = 0;
		for (ClassRecord record: classList) {
			int hitPoints = record.getHitPoints();
			if (hasFeat(Feat.Toughness))
				hitPoints += record.getLevel() * 2;
			supMaxHP = Math.max(supMaxHP, hitPoints);
		}
		return supMaxHP;
	}

	/**
	*  Create evil human NPC with equipment from class title.
	*/
	static public Character evilNPCFromTitle (String title) {
		ClassType classType = ClassIndex.getInstance().getTypeFromTitle(title);
		if (classType != null) {
			String classn = classType.getName();
			int level = classType.getLevelFromTitle(title);
			Character c = new Character("Human", classn, level, "Chaotic");
			c.setBasicEquipment();
			c.boostMagicItemsToLevel();
			return c;
		}
		return null;
	}

	/**
	*  Set basic equipment by top class.
	*/
	public void setBasicEquipment () {
		if (isFighter()) {
			switch (getLevel()) {
				case 0: setArmor(Armor.makeType(Armor.Type.Leather)); break;
				case 1: setArmor(Armor.makeType(Armor.Type.Chain)); break;
				default: setArmor(Armor.makeType(Armor.Type.Plate)); break;
			}
			Weapon primary = Weapon.randomPrimary();
			addEquipment(primary);
			if (primary.getHandsUsed() < 2) {
				setShield(Armor.makeType(Armor.Type.Shield));
			}
			addEquipment(Weapon.randomSecondary());
			if (isWizard()) {
				setArmor(Armor.makeType(Armor.Type.Chain));
				setShield(null);			
			}
		}
		else if (isThief()) {
			setArmor(Armor.makeType(Armor.Type.Leather));
			addEquipment(Weapon.randomThieving());
			addEquipment(Weapon.dagger());
		}
		else {
			addEquipment(Weapon.dagger());
		}
	}

	/**
	*  Make rolls to boost magic items for one level.
	*/
	public void boostMagicItemsOneLevel () {
		if (weaponInHand == null) {
			drawBestWeapon(null);
		}
		if (armor != null && getMagicBoost()) {
			armor.incMagicBonus();
		}
		if (shield != null && getMagicBoost()) {
			shield.incMagicBonus();			
		}
		if (weaponInHand != null && getMagicBoost()) {
			weaponInHand.incMagicBonus();
		}
		if (shield == null && getMagicBoost()) {
			incrementRing();
		}
		if (getTopClass().getClassType().usesSpells() && getMagicBoost()) {
			incrementWand();
		}
		updateStats();
	}

	/**
	*  Boost all magic items to the character's top level. 
	*/
	public void boostMagicItemsToLevel () {
		int level = getLevel();
		for (int i = 1; i < level; i++) {
			boostMagicItemsOneLevel();
		}
	}

	/**
	*	Check if a magic item boost is gained. 
	*/
	boolean getMagicBoost () {
		return Dice.roll(100) <= pctMagicPerLevel;
	}

	/**
	*	Increment magic ring worn.
	*/
	void incrementRing () {
		if (ring == null)
			ring = new Equipment("Ring of Protection", 1);
		else
			ring.incMagicBonus();		
	}

	/**
	*	Increment magic wand carried.
	*/
	void incrementWand () {
		Wands wandTable = Wands.getInstance();
		if (wand == null)
			wand = wandTable.getRandom(1);
		else {
			int newTier = wandTable.getTier(wand) + 1;
			Equipment newWand = wandTable.getRandom(newTier);
			if (newWand != null)
				wand = newWand;				
		}		
	}

	/**
	*	Roll a saving throw with modifier.
	*/
	public boolean	rollSave (SavingThrows.SaveType type, int modifier) {
		ClassRecord bestClass = bestClassForSave(type);
		modifier += getFixedSaveModifiers(type);
		if (ring != null)
			modifier += ring.getMagicBonus();
		return SavingThrows.getInstance().rollSave(
			type, bestClass.getClassType().getSaveAsClass(), 
			bestClass.getLevel(), modifier); 
	}

	/**
	*	Find the best class to use for a given saving throw.
	*/
	private ClassRecord bestClassForSave (SavingThrows.SaveType saveType) {
		ClassRecord bestClass = null;
		int bestScore = Integer.MAX_VALUE;
		for (ClassRecord record: classList) {
			String saveAs = record.getClassType().getSaveAsClass();
			int score = SavingThrows.getInstance().getSaveTarget(
				saveType, saveAs, record.getLevel());
			if (score < bestScore) {
				bestScore = score;
				bestClass = record;			
			}
		}
		return bestClass;
	}	

	/**
	*  Set if we should be using optional feats.
	*/
	static public void setFeatUsage (boolean permit) {
		useFeats = permit;	
	}

	/**
	*  Are we using optional feats?
	*/
	static public boolean useFeats () {
		return useFeats;
	}
	
	/**
	*  Does this character have a given feat?
	*/
	public boolean hasFeat (Feat feat) {
		for (ClassRecord rec: classList) {
			if (rec.hasFeat(feat))
				return true;
		}
		return false;
	}

	/**
	*  Get the age of the character.
	*/
	public int getAge () {
		return age;
	}

	/**
	*  Increment the age of the character.
	*/
	public void incrementAge () {

		// Increment the age
		AgeCategory startAgeCat = getAgeCategory();
		age++;

		// Apply effects
		if (applyAgingEffects) {
			if (getAgeCategory() != startAgeCat) {
				ageAdjustAbilities();		
			}
			if (age > 40) {
				ageAdjustLevels();
			}
			updateStats();
		}
	}

	/**
	*  Get the age category of the character.
	*  Caution: Works for Humans only.
	*/
	AgeCategory getAgeCategory () {
		if (age <= 13) return AgeCategory.Child;
		else if (age <= 20) return AgeCategory.YoungAdult;
		else if (age <= 40) return AgeCategory.Mature;
		else if (age <= 60) return AgeCategory.MiddleAged;
		else if (age <= 90) return AgeCategory.Old;
		else return AgeCategory.Venerable;
	}

	/**
	*  Adjust ability scores on reaching a new age category.
	*/
	private void ageAdjustAbilities () {
		switch (getAgeCategory()) {
			case Child: break;
			case YoungAdult: adjustAllAbilityScores(0, 0, -1, 0, +1, 0); break;
			case Mature:     adjustAllAbilityScores(+1, 0, +1, 0, 0, 0); break;
			case MiddleAged: adjustAllAbilityScores(-1, +1, +1, 0, -1, 0); break;
			case Old:        adjustAllAbilityScores(-2, 0, +1, -2, -1, 0); break;
			case Venerable:  adjustAllAbilityScores(-1, +1, +1, -1, -1, 0); break;
		}	
	}

	/**
	*  Adjust levels on advancing age.
	*/
	private void ageAdjustLevels () {
		for (ClassRecord record: classList) {
			switch (record.getClassType().getPrimeReq()) {
				case Str: if (age % 5 == 0) record.loseLevel(); break;
				case Dex: if (age % 10 == 0) record.loseLevel(); break;
			}
		}
	}

	/**
	*  Generate random treasure value by men type "A",
	*  scaled by level and nominal men number appearing.
	*  (Recommended for wilderness encounters only.)
	*/
	int getTreasureValue () {
		final int avgNum = 165;
		int level = Math.max(getLevel(), 1);
		return MonsterTreasureTable.getInstance()
			.randomValueByCode('A') * level / avgNum;
	}

	/**
	*  Convert string to alignment (random if null).
	*/
	Alignment getAlignmentFromString (String s) {
		Alignment align = Alignment.getFromString(s);
		if (align == null)
			align = Alignment.randomNormal();
		return align;	
	}

	/**
	*  Mutator to initial ability/hp boost switch.
	*/
	static public void setBoostInitialAbilities (boolean boost) {
		boostInitialAbilities = boost; 
	}

	/**
	*  Accessor to initial ability/hp boost switch.
	*/
	static public boolean getBoostInitialAbilities () {
		return boostInitialAbilities;	
	}

	/**
	*  Mutator to aging effect switch.
	*/
	static public void setApplyAgingEffects (boolean aging) {
		applyAgingEffects = aging;
	}

	/**
	*  Mutator to percent magic per level. 
	*/
	static public void setPctMagicPerLevel (int pct) {
		pctMagicPerLevel = pct;	
	}

	//--------------------------------------------------------------------------
	//  Printing Methods
	//--------------------------------------------------------------------------

	// Print option switches
	static public void setPrintFeats (boolean p) { printFeats = p; }
	static public void setPrintAbilities (boolean p) { printAbilities = p; }
	static public void setPrintEquipment (boolean p) { printEquipment = p; }
	static public void setPrintPersonality (boolean p) { printPersonality = p; }
	static public void setPrintSpells (boolean p) { printSpells = p; }

	/**
	*  Identify this object as a string.
	*/
	public String toString () {

		// Standard stat block
		String s = name + ", " + race + " " + classString();
		s += ": AC " + getAC() + ", MV " + getMV() + ", HD " + getHD()
			+ ", hp " + getHP() + ", Atk " + getAttack();

		// Optional information
		if (printAbilities)
			s = addClause(s, abilityString());
		if (printPersonality)
			s = addClause(s, personalityString());
		if (printEquipment)
			s = addClause(s, "Gear", equipString());
		if (printFeats)
			s = addClause(s, "Feats", featString());
		if (printSpells)
			s = addClause(s, "Spells", spellsString());
		
		return s += ".";
	}	

	/**
	*  String representation of all class and levels.
	*/
	String classString () {
		String s = "";
		for (ClassRecord record: classList) {
			if (s.length() > 0) s += "/";
			s += record.getClassType().getAbbreviation() 
				+ record.getLevel();
		}
		return s;
	}

	/**
	*  String representation of all ability scores.
	*/
	private String abilityString () {
		String s = "";
		for (Ability a: Ability.values()) {
			s = addItem(s, a.name() + " " + getAbilityScore(a));
		}
		return s;
	}

	/**
	*  String representation of alignment and personality.
	*/
	private String personalityString () {
		return alignment + ", " 
			+ primaryPersonality + ", " 
			+ secondaryPersonality;	
	}

	/**
	*  String representation of equipment.
	*/
	private String equipString () {
		String s = "";
		s = addItem(s, armor);
		s = addItem(s, shield);
		s = addItem(s, ring);
		s = addItem(s, weaponInHand);
		s = addItem(s, wand);
		for (Equipment equip: equipList) {
			s = addItem(s, equip);
		}
		return toTitleCase(s);
	}

	/**
	*  String representation of feats.
	*/
	private String featString () {
		String s = "";
		for (ClassRecord rec: classList) {
			s = addItem(s, rec.featsString());
		}
		return toTitleCase(s);
	}

	/**
	*  String representation of spells.
	*/
	private String spellsString () {
		String s = "";
		for (ClassRecord cr: classList) {
			if (cr.getClassType().usesSpells()) {
				s = addItem(s, cr.spellsString());
			}
		}
		return toTitleCase(s);
	}

	/**
	*  Add item to a string if not null.
	*/
	private String addItem (String s, Object item) {
		if (item == null)
			return s;
		else 
			return s + (s.length() > 0 ? ", " : "") + item;
	}

	/**
	*  Add an independent clause to a string, if nonempty.
	*/
	private String addClause (String s, String clause) {
		return clause.length() > 0 ? s + "; " + clause : s;
	}

	/**
	*  Add a label & clause to a string, if nonempty.
	*/
	private String addClause (String s, String label, String clause) {
		return clause.length() > 0 ? addClause(s, label + ": " + clause) : s;
	}

	/**
	*  Convert a string to title case.
	*/
	private String toTitleCase(String s) {
		if (s.length() > 0) {
			return java.lang.Character.toUpperCase(s.charAt(0))
				+ s.substring(1).toLowerCase();
		}
		else
			return s;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		Character.setBoostInitialAbilities(true);
		Character p = new Character("Human", "Fighter", 1, null);
		p.setBasicEquipment();
		p.drawBestWeapon(null);
		System.out.println(p);
	}
}

