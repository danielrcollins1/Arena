import java.util.*;

/******************************************************************************
*  One character (player or non-player personae).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-20
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
	AbstractMap<Ability, Integer> abilityScores;

	/** Ability score damage. */
	AbstractMap<Ability, Integer> abilityScoreDamage;

	/** List of classes with XP scores. */
	List<ClassRecord> classList;

	/** Armor worn. */
	Armor armorWorn;

	/** Shield held. */
	Armor shieldHeld;

	/** Weapon in hand. */
	Weapon weaponInHand;	

	/** Magic ring. */
	Equipment ringWorn;

	/** Magic wand. */
	Equipment wandHeld;
	
	/** Equipment carried. */
	List<Equipment> equipList;

	/** Primary personality trait. */
	PersonalityTraits.PersonalityTrait primaryPersonality;
	
	/** Secondary personality trait. */
	PersonalityTraits.PersonalityTrait secondaryPersonality;
	
	/** Rate of sweep attacks (vs. 1 HD targets) */
	int sweepRate;

	/** Percent chance per level for magic items. */
	static int pctMagicPerLevel = BASE_MAGIC_PER_LEVEL;

	/** Assign feats to characters? */
	static boolean useFeats = false;

	/** Give attacks by fighter level vs. 1 HD creatures? */
	static boolean useSweepAttacks = false;

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
		abilityScores = new EnumMap<Ability, Integer>(Ability.class);
		rollAbilityScores(classType, level);
		abilityScoreDamage = new EnumMap<Ability, Integer>(Ability.class);
		zeroAbilityDamage();
		classList = new ArrayList<ClassRecord>(1);
		equipList = new ArrayList<Equipment>(4);
		classList.add(new ClassRecord(this, classType, level));
		alignment = getAlignmentFromString(align);
		primaryPersonality = PersonalityTraits.getInstance().getRandom(alignment);
		secondaryPersonality = PersonalityTraits.getInstance().getRandom(null);
		sweepRate = 0;
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

	// Name accessor
	public String getName () { return name; }

	// Implement null Monster equipment methods
	public Armor getArmor () { return armorWorn; }
	public Armor getShield () { return shieldHeld; }
	public Weapon getWeapon () { return weaponInHand; }

	// Access equipment carried
	public int getEquipmentCount () { return equipList.size(); }
	public Equipment getEquipment (int i) { return equipList.get(i); }
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
			for (Ability a: Ability.values()) {
				abilityScores.put(a, ABILITY_DICE[0].roll());
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
		int priority = 0;
		for (Ability a: priorityList) {
			abilityScores.put(a, getBoostedAbilityDice(level, priority).roll());
			priority++;
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
		else if (level <= 3) {
			switch (priority) {
				case 0: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}
		else if (level <= 7) {
			switch (priority) {
				case 0: case 1: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}
		else {
			switch (priority) {
				case 0: return ABILITY_DICE[2]; 
				case 1: case 2: case 3: return ABILITY_DICE[1];
				default: return ABILITY_DICE[0];
			}
		}
	}

	/**
	*  Get an ability score.
	*/
	public int getAbilityScore (Ability a) {
		int score = abilityScores.get(a).intValue();
		score -= abilityScoreDamage.get(a).intValue();
		if (a == Ability.Str 
				&& hasFeat(Feat.ExceptionalStrength)) {
			score += 3;
		}
		if (score < 0) score = 0;
		return score;
	}

	/*
	*  Take damage to an ability score.
	*/
	protected void takeAbilityDamage (Ability a, int damage) {
		int val = abilityScoreDamage.get(a).intValue();
		val += damage;
		abilityScoreDamage.put(a, Integer.valueOf(val));
		updateStats();
	}

	/*
	*  Clear any ability score damage.
	*/
	public void zeroAbilityDamage () {
		for (Ability a: Ability.values())
			abilityScoreDamage.put(a, 0);
	}

	/*
	*  Adjust all ability scores by indicated modifiers.
	*/
	private void adjustAllAbilityScores (int... modifiers) {
		int oldCon = getAbilityScore(Ability.Con);
		int idx = 0;
		for (Ability a: Ability.values()) {
			int val = abilityScores.get(a).intValue();
			val += modifiers[idx];
			abilityScores.put(a, Integer.valueOf(val));
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
	protected boolean hasNullAbilityScore () {

		// This is a critical-path function.
		// For performance, check only the Strength ability
		// (c.f. Shadow Strength draining)
		// If other ability-drain abilities arise, add here.
		if (getAbilityScore(Ability.Str) <= 0)
			return true;
		else
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
		sweepRate = computeSweepRate();
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
		if (armorWorn != null) {
			AC -= armorWorn.getBaseArmor() + armorWorn.getMagicBonus();
		}	
		if (shieldHeld != null) {
			AC -= shieldHeld.getBaseArmor() + shieldHeld.getMagicBonus();
		}
		if (ringWorn != null) {
			AC -= ringWorn.getMagicBonus();		
		}
		return AC;
	}

	/**
	*  Get movement based on armor.
	*/
	private int computeMoveInches() {
		float weight = getEncumbrance();
		int strength = getAbilityScore(Ability.Str);
		if (weight <= strength * 1./3)
			return 12;
		else if (weight <= strength * 2./3)
			return 9;
		else if (weight <= strength)
			return 6;
		else
			return 0;
	}

	/**
	*  Get total encumbrance of equipment.
	*/
	public float getEncumbrance () {
		float sum = 0;
		for (Equipment equip: equipList) {
			sum += equip.getWeight();
		}
		return sum;
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
			int rate = 1;
			int atkBonus = baseAttackBonus() + getAbilityBonus(Ability.Str) 
				+ weaponInHand.getMagicBonus();
			Dice damageDice = new Dice(weaponInHand.getBaseDamage());
			int damageAdd = damageDice.getAdd() + getAbilityBonus(Ability.Str) 
				+ weaponInHand.getMagicBonus();
			if (hasFeat(Feat.WeaponSpecialization)) {
				atkBonus += 2;
				damageAdd += 2;
			}
			damageDice.setAdd(damageAdd);
			EnergyType energy = weaponInHand.getEnergy();
			return new Attack(name, rate, atkBonus, damageDice, energy);
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
	protected int getMagicHitLevel () {
		return (weaponInHand == null ? 0 : weaponInHand.getMagicBonus());
	}

	/**
	*  Set armor worn.
	*/
	public void setArmor (Armor armor) { 
		if (armor != null) {
			if (!equipList.contains(armor)) {
				equipList.add(0, armor);
			}	
			if (armorWorn != armor) {
				equipList.remove(armorWorn);
			}
		}
		armorWorn = armor;
		updateStats();
	}

	/**
	*  Set shield carried.
	*/
	public void setShield (Armor shield) { 
		if (shield != null) {
			if (!equipList.contains(shield)) {
				equipList.add(shield);
			}	
			if (shieldHeld != shield) {
				equipList.remove(shieldHeld);
			}		
		}
		shieldHeld = shield;
		updateStats();
	}

	/**
	*  Draw a particular weapon.
	*/
	public void drawWeapon (Weapon weapon) {
		if (weapon != null) {
			if (!equipList.contains(weapon)) {
				equipList.add(weapon);
			}	
			if (weapon.getHandsUsed() <= 1 && shieldHeld == null) {
				setShield(findShieldInEquip());
			}
			if (weapon.getHandsUsed() >= 2 && shieldHeld != null) {
				setShield(null);
			}
		}
		weaponInHand = weapon;
		updateStats();
	}

	/**
	*  Find shield in equipment list, if any
	*/
	private Armor findShieldInEquip () {
		for (Equipment e: equipList) {
			if (e instanceof Armor) {
				Armor a = (Armor) e;
				if (a.getArmorType() == Armor.Type.Shield) {
					return a;
				}
			}
		}	
		return null;
	}	

	/**
	*  Draw next weapon in equipment iteratively.
	*/
	public void drawNextWeapon () {
		int index = (weaponInHand == null) ? 0
			: equipList.indexOf(weaponInHand) + 1;
		while (index < equipList.size()) {
			Equipment equip = equipList.get(index);
			if (equip instanceof Weapon) {
				drawWeapon((Weapon) equip);
				return;
			}		
			index++;
		}
		drawWeapon(null);
	}

	/**
	*  Draw best weapon against a given monster.
	*/
	public void drawBestWeapon (Monster monster) {
		drawWeapon(null);
		int maxDamage = -1;
		Weapon bestWeapon = null;
		while (true) {
			drawNextWeapon();
			if (weaponInHand == null) break;
			int damage = maxDamageVsMonster(monster);
			if (damage > maxDamage) {
				maxDamage = damage;
				bestWeapon = weaponInHand;
			}							
		}
		drawWeapon(bestWeapon);
	}

	/**
	*  Lose a given piece of equipment.
	*/
	protected void loseEquipment (Equipment equip) {
		assert(equip != null);
		equipList.remove(equip);
		if (equip == armorWorn) armorWorn = null;
		if (equip == shieldHeld) shieldHeld = null;
		if (equip == weaponInHand) weaponInHand = null;
		if (equip == ringWorn) ringWorn = null;
		if (equip == wandHeld) wandHeld = null;
		updateStats();
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
	*  Overrides method in Monster.
	*/
	protected void loseLevel () {
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
			if (primary.getHandsUsed() < 2) {
				setShield(Armor.makeType(Armor.Type.Shield));
			}
			addEquipment(primary);
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
		if (armorWorn != null && getMagicBoost()) {
			armorWorn.incMagicBonus();
		}
		if (shieldHeld != null && getMagicBoost()) {
			shieldHeld.incMagicBonus();			
		}
		if (weaponInHand != null && getMagicBoost()) {
			weaponInHand.incMagicBonus();
		}
		if (shieldHeld == null && getMagicBoost()) {
			incrementRing();
		}
		if (getTopClass().hasSpells() && getMagicBoost()) {
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
		if (ringWorn == null) {
			ringWorn = new Equipment("Ring of Protection", 
				Equipment.Material.Steel, 0, 1);
			equipList.add(ringWorn);
		}
		else {
			ringWorn.incMagicBonus();
		}
	}

	/**
	*	Increment magic wand carried.
	*/
	void incrementWand () {
		Equipment newWand;
		Wands wandTable = Wands.getInstance();

		// Determine the new wand
		if (wandHeld == null) {
			newWand = wandTable.getRandom(1);
		}
		else {
			int newTier = wandTable.getTier(wandHeld) + 1;
			newWand = wandTable.getRandom(newTier);
		}
		
		// Pick up new wand, discard old
		if (newWand != null) {
			if (wandHeld != null) {
				equipList.remove(wandHeld);
			}
			equipList.add(newWand);
			wandHeld = newWand;
		}
	}

	/**
	*	Roll a saving throw with modifier.
	*/
	protected boolean rollSave (SavingThrows.Type type, int modifier) {
		ClassRecord bestClass = bestClassForSave(type);
		if (ringWorn != null)
			modifier += ringWorn.getMagicBonus();
		return SavingThrows.getInstance().rollSave(
			type, bestClass.getClassType().getSaveAsClass(), 
			bestClass.getLevel(), modifier); 
	}

	/**
	*	Find the best class to use for a given saving throw.
	*/
	private ClassRecord bestClassForSave (SavingThrows.Type saveType) {
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
	protected boolean hasFeat (Feat feat) {
		for (ClassRecord rec: classList) {
			if (rec.hasFeat(feat))
				return true;
		}
		return false;
	}

	/**
	*  Set if we should be using sweep attacks.
	*/
	static public void setSweepAttacks (boolean permit) {
		useSweepAttacks = permit;	
	}

	/**
	*  Are we using sweep attacks?
	*/
	static public boolean useSweepAttacks () {
		return useSweepAttacks;
	}

	/**
	*  How many attacks does this character get against 1 HD opponents?
	*  Give this fighter bonus to any class with BAB 1 or above.
	*/
	private int computeSweepRate() {
		int rate = 0;
		for (ClassRecord rec: classList) {
			if (rec.attackBonus() >= rec.getLevel()) {
				if (rec.getLevel() > rate)
					rate = rec.getLevel();			
			}
		}
		return rate;
	}

	/**
	*  Get this character's recorded sweep rate.
	*  Redefines dummy method in Monster class.
	*/
	protected int getSweepRate () {
		return sweepRate;
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
			ageAdjustLevels();
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
	*  Prevents fighters from living indefinitely long;
	*  follows pattern Gygax showed for Conan in Dragon #36.
	*/
	private void ageAdjustLevels () {
		if (age > 40) {
			for (ClassRecord record: classList) {
				switch (record.getClassType().getPrimeReq()) {
					case Str: if (age % 2 == 0) record.loseLevel(); break;
					case Dex: if (age % 5 == 0) record.loseLevel(); break;
					case Int: break;
				}
			}
		}
	}

	/**
	*  Generate random treasure value by men type "A",
	*  scaled by level and nominal men number appearing.
	*  (Recommended for wilderness encounters only.)
	*/
	public int getTreasureValue () {
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

	/**
	*   Is this character a person? (redefines Monster method)
	*/
	public boolean isPerson () {
		return true;
	}

	/**
	*  Get the character's spell memory, if any.
	*  Overrides method in Monster.
	*  Warning: This assumes a character has at most one spellcasting class.
	*  If multiple spell classes supported, this system needs reworking.
	*/
	public SpellMemory getSpellMemory () {
		for (ClassRecord _class: classList) {
			if (_class.getSpellMemory() != null)
				return _class.getSpellMemory();
		}
		return null;
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

		// Basic stat string
		String s = name + ", " + race + " " + classString(true);
		s += ": AC " + getAC() + ", MV " + getMV() + ", HD " + getHD()
			+ ", hp " + getHP() + ", Atk " + getAttack();
		
		// Optional stuff
		if (printAbilities)
			s = addClause(s, abilityString());
		if (printPersonality)
			s = addClause(s, personalityString());
		if (printEquipment)
			s = addClause(s, "Gear: ", toSentenceCase(equipString()));
		if (printFeats)
			s = addClause(s, "Feats: ", toSentenceCase(featString()));
		if (printSpells)
			s = addClause(s, "Spells: ", toSentenceCase(spellString()));

		return s += ".";
	}	

	/**
	*  Short String representation of this character.
	*/
	public String shortString () {
		return name + ", " + race + " " 
			+ classString(true) + ": hp " + getHP();
	}

	/**
	*  String representation of all class and levels.
	*/
	private String classString (boolean slashes) {
		String s = "";
		for (ClassRecord record: classList) {
			if (slashes && s.length() > 0) s += "/";
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
		for (Equipment equip: equipList) {
			s = addItem(s, equip);
		}
		return s;
	}

	/**
	*  String representation of feats.
	*/
	public String featString () {
		String s = "";
		for (ClassRecord rec: classList) {
			s = addItem(s, rec.featsString());
		}
		return s;
	}

	/**
	*  String representation of skills.
	*/
	public String skillString () {
		String s = "";
		for (ClassRecord rec: classList) {
			s = addItem(s, rec.skillsString());
		}
		return s;
	}

	/**
	*  String representation of spells.
	*/
	public String spellString () {
		String s = "";
		for (ClassRecord cr: classList) {
			if (cr.getClassType().usesSpells()) {
				s = addItem(s, cr.spellsString());
			}
		}
		return s;
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
		return clause.length() > 0 ? addClause(s, label + clause) : s;
	}

	/**
	*  Convert a string to sentence case.
	*/
	public static String toSentenceCase(String s) {
		if (s.length() > 0) {
			return java.lang.Character.toUpperCase(s.charAt(0))
				+ s.substring(1).toLowerCase();
		}
		else
			return s;
	}

	/**
	*  Create a short race/class descriptor.
	*/
	public String getRaceClassDesc () {
		return race + " " + classString(true);	
	}

	/**
	*  Create a filename identifier.
	*/
	public String getFilename () {
		return name + "-" + race + classString(false);
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
