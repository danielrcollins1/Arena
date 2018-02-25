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

	/** Base armor class. */
	static final int BASE_ARMOR_CLASS = 9;

	/** Base movement. */
	static final int BASE_MOVEMENT = 12;

	/** Percent per level for NPC magic items. */
	static final int NPC_PCT_MAGIC_PER_LEVEL = 5;

	/** Whether we apply the prime-requisite XP bonus. */
	static final boolean APPLY_BONUS_XP = false;

	/** Dice for ability scores. */
	static final Dice ABILITY_DICE = new Dice(3, 6);

	/** Starting age. */
	static final int STARTING_AGE = 18;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Personal name of the character. */
	String name;

	/** The six ability scores. */
	int[] abilityScore;

	/** Ability score damage. */
	int[] abilityScoreDamage;

	/** List of classes with XP scores. */
	ArrayList<ClassRecord> classList;

	/** Armor worn. */
	Armor armor;

	/** Shield held. */
	Armor shield;

	/** Weapon in hand. */
	Weapon weaponInHand;	
	
	/** Equipment carried. */
	ArrayList<Equipment> equipList;

	/** Feats acquired. */
	ArrayList<Feat> featList;
	
	/** Use feats? */
	static boolean useFeats = false;

	/** Age in years */
	int age;

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** 
	*  Optional OED fighter feats. 
	*  Caution: Not all are implemented in code at this time.
	*/
	public enum Feat {
		Berserking, GreatCleave, GreatFortitude, GreatStrength, 
		IronWill, MountedCombat, RapidShot, RapidStrike, 
		Toughness, Tracking, TwoWeaponFighting, WeaponSpecialization;
		
		/** Total number of feats available. */
		public static final int length = Feat.values().length;
	};

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
	*  Constructor (single class, no equipment).
	*/
	public Character (String race, String classn, int level, Alignment align) {
		super(race, BASE_ARMOR_CLASS, BASE_MOVEMENT, 
			new Dice(Monster.BASE_HIT_DIE), null);

		assert(level >= 0); 
		age = STARTING_AGE;
		name = NameGenerator.getInstance().getRandom();
		alignment = (align != null) ? align : Alignment.random();
		abilityScore = newAbilityScores();
		abilityScoreDamage = new int[Ability.length];
		classList = new ArrayList<ClassRecord>(1);
		equipList = new ArrayList<Equipment>(4);
		featList = new ArrayList<Feat>(0);
		ClassType classType = ClassIndex.getInstance().getTypeFromName(classn);
		classList.add(new ClassRecord(this, classType, level));
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

	/**
	*  Roll random ability scores.
	*/
	private int[] newAbilityScores () {
		int[] scores = new int[Ability.length];
 		for (int i = 0; i < Ability.length; i++) {
 			scores[i] = ABILITY_DICE.roll();
 		}
		return scores;
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
			AC = AC - armor.getBaseArmor() - armor.getMagicBonus();
		}	
		if (shield != null) {
			AC = AC - shield.getBaseArmor() - shield.getMagicBonus();
		}
		return AC;
	}

	/**
	*  Get movement based on armor.
	*/
	private int computeMoveInches() {
		return (armor == null ? 
			BASE_MOVEMENT : armor.getMaxMove());
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
			Ability primeReq = cr.getClassType().getPrimeRequisite();
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
	*  Create human NPC with equipment from class title.
	*/
	static public Character newNPCFromTitle (String title, Alignment align) {
		ClassType classType = ClassIndex.getInstance().getTypeFromTitle(title);
		if (classType != null) {
			int level = classType.getLevelFromTitle(title);
			Character c = new Character("Human", classType.getName(), level, align);
			c.setBasicEquipment();
			for (int i = 1; i <= level; i++) {
				c.checkMagicArmsBoost(NPC_PCT_MAGIC_PER_LEVEL);			
			}
			return c;
		}
		return null;
	}

	/**
	*  Set basic equipment by top class.
	*/
	public void setBasicEquipment () {
		String className = getTopClass().getClassType().getName();
		if (className.equals("Fighter")) {
			switch (getLevel()) {
				case 0: setArmor(Armor.makeType(Armor.Type.Leather)); break;
				case 1: setArmor(Armor.makeType(Armor.Type.Chain)); break;
				default: setArmor(Armor.makeType(Armor.Type.Plate)); break;
			}
			setShield(Armor.makeType(Armor.Type.Shield));
			addEquipment(Weapon.randomPrimary());
			addEquipment(Weapon.randomSecondary());
		}
		else if (className.equals("Thief")) {
			setArmor(Armor.makeType(Armor.Type.Leather));
			addEquipment(new Weapon("Sword", new Dice(8), 1));
		}
		else {
			addEquipment(Weapon.silverDagger());
		}
	}

	/**
	*  Make rolls to possibly boost magic arms.
	*/
	public void checkMagicArmsBoost (int percent) {
		if (weaponInHand == null) {
			drawBestWeapon(null);
		}
		if (armor != null & Dice.roll(100) <= percent) {
			armor.incMagicBonus();
		}
		if (shield != null & Dice.roll(100) <= percent) {
			shield.incMagicBonus();			
		}
		if (weaponInHand != null & Dice.roll(100) <= percent) {
			weaponInHand.incMagicBonus();
		}
		updateStats();
	}

	/**
	*	Roll a saving throw with modifier.
	*/
	public boolean	rollSave	(SavingThrows.SaveType type, int modifier) {
		modifier += getFixedSaveModifiers(type);
		ClassRecord bestClass = bestClassForSave(type);
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
		return featList.contains(feat);
	}

	/**
	*  Add a random feat to this character.
	*/
	public void addFeat () {
		if (useFeats) {
			while	(true) {
				int rand = Dice.roll(Feat.length) - 1;
				Feat newFeat = Feat.values()[rand];
				if (!hasFeat(newFeat)) {
					featList.add(newFeat);
					return;
				}
			}	
		}
	}

	/**
	*  Lose the last feat for this character.
	*/
	public void loseFeat () {
		if (featList.size() > 1) {
			featList.remove(featList.size() - 1);
		}	
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
	public void incrementAge (boolean ageEffects) {

		// Increment the age
		AgeCategory startAgeCat = getAgeCategory();
		age++;

		// Apply effects
		if (ageEffects) {
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
			String className = record.getClassType().getName();
			if (className.equals("Fighter") && age % 2 == 0) {
				record.loseLevel();				
			}
			else if (className.equals("Thief") && age % 4 == 0) {
				record.loseLevel();				
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
	*  Identify this object as a string.
	*/
	public String toString () {

		// Header
		String s = name + ", " + race + " " + classString();

		// Stat block
		s += ": AC " + getAC() + ", MV " + getMV() + ", HD " + getHD()
			+ ", hp " + getHP() + ", Atk " + getAttack() + "; ";
		s += abilityString();

		// Feats
		String featStr = featString();
		if (featStr.length() > 0) {
			s += "; " + featStr;		
		}

		// Equipment
		String equipStr = equipString();
		if (equipStr.length() > 0) {
			s += "; " + equipStr;		
		}
		s += ".";
		return s;		
	}	

	/**
	*  String representation of all class and levels.
	*/
	String classString () {
		String s = "";
		for (ClassRecord record: classList) {
			s += record.getClassType().getAbbreviation() 
				+ record.getLevel() + "/";
		}
		return s.substring(0, s.length() - 1);
	}

	/**
	*  String representation of all ability scores.
	*/
	private String abilityString () {
		String s = "";
		for (Ability a: Ability.values()) {
			s += a.name() + " " + getAbilityScore(a) + ", ";
		}
		return s.substring(0, s.length() - 2);
	}

	/**
	*  String representation of feats.
	*/
	private String featString () {
		String s = "";
		for (Feat feat: featList) {
			s += formatFeat(feat) + ", ";
		}	
		return (s.length() < 2 ? "" : 	
			s.substring(0, s.length() - 2));
	}

	/**
	*  Format feat name with spaces.
	*/
	private String formatFeat (Feat feat) {
		String r = "";
		String s = feat.toString();
		for (int i = 0; i < s.length(); i++) {
			if (i > 0 && 
					java.lang.Character.isUpperCase(s.charAt(i))) {
				r += " ";
			}
			r += s.charAt(i);
		}			
		return r;	
	}

	/**
	*  String representation of equipment.
	*/
	private String equipString () {
		String s = "";
		if (armor != null) s += armor + ", ";
		if (shield != null) s += shield + ", ";	
		if (weaponInHand != null) s += weaponInHand + ", ";
		for (Equipment equip: equipList) {
			s += equip + ", ";		
		}
		return (s.length() < 2 ? "" : 	
			s.substring(0, s.length() - 2));
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		Character p = new Character("Human", "Fighter", 1, null);
		p.setBasicEquipment();
		p.drawBestWeapon(null);
		System.out.println(p);
	}
}

