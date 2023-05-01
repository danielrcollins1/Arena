import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
	One character (player or non-player personae).

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2014-05-20
*/

public class Character extends Monster {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Base age in years. */
	private static final int BASE_AGE = 18;

	/** Base armor class. */
	private static final int BASE_ARMOR_CLASS = 9;

	/** Base movement. */
	private static final int BASE_MOVEMENT = 12;

	/** Base hit dice. */
	private static final Dice BASE_HD = new Dice(1, 6);

	/** Dice for ability scores. */
	private static final Dice ABILITY_DICE = new Dice(3, 6);

	/** How many ability boosts we give over level. */
	private static final int ABILITY_BOOSTS_OVER_LEVEL = 2;

	/** Maximum score to which we can boost an ability. */
	private static final int MAX_ABILITY_SCORE = 18;

	/** Base percent per level for magic items. */
	private static final int BASE_MAGIC_PER_LEVEL = 5;

	/** Whether we apply the prime-requisite XP bonus. */
	static final boolean APPLY_BONUS_XP = false;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Age in years. */
	private int age;

	/** Personal name of the character. */
	private String name;

	/** The six ability scores. */
	private int[] abilityScores;

	/** Ability score damage. */
	private int[] abilityScoreDamage;

	/** List of classes with XP scores. */
	private List<ClassRecord> classList;

	/** Armor worn. */
	private Armor armorWorn;

	/** Shield held. */
	private Armor shieldHeld;

	/** Weapon in hand. */
	private Weapon weaponInHand;	

	/** Magic ring. */
	private Equipment ringWorn;

	/** Magic wand. */
	private Equipment wandHeld;
	
	/** Equipment carried. */
	private List<Equipment> equipList;

	/** Primary personality trait. */
	private PersonalityTraits.PersonalityTrait primaryPersonality;
	
	/** Secondary personality trait. */
	private PersonalityTraits.PersonalityTrait secondaryPersonality;
	
	/** Rate of sweep attacks (vs. 1 HD targets) */
	private int sweepRate;

	/** Percent chance per level for magic items. */
	private static int pctMagicPerLevel = BASE_MAGIC_PER_LEVEL;

	/** Assign feats to characters? */
	private static boolean useFeats = false;

	/** Give attacks by fighter level vs. 1 HD creatures? */
	private static boolean useSweepAttacks = false;

	/** Print feats in string descriptor? */
	private static boolean printFeats = true;

	/** Print abilities in string descriptor? */
	private static boolean printAbilities = true;

	/** Print equipment in string descriptor? */
	private static boolean printEquipment = true;

	/** Print personality in string descriptor? */
	private static boolean printPersonality = true;

	/** Print spells in string descriptor? */
	private static boolean printSpells = true;

	/** Boost abilities & hit points at time of creation? */
	private static boolean boostInitialAbilities = false;

	/** Apply aging effects? */
	private static boolean applyAgingEffects = false;

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** 
		Age categories.
	*/
	public enum AgeCategory {
		Child, YoungAdult, Mature, MiddleAged, Old, Venerable;
	};

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (single class).
	*/
	public Character(String race, String classn, int level, String align) {
		super(race, BASE_ARMOR_CLASS, BASE_MOVEMENT, BASE_HD, null);
		assert race != null;
		assert classn != null;
		assert level >= 0;
		name = NameGenerator.getInstance().getRandom(race);
		ClassType classType = ClassIndex.getTypeFromName(classn);
		abilityScores = new int[Ability.size()];
		rollBaseAbilities();
		if (boostInitialAbilities) {
			boostBaseAbilities(classType, level);		
		}
		abilityScoreDamage = new int[Ability.size()];
		zeroAbilityDamage();
		classList = new ArrayList<ClassRecord>(1);
		equipList = new ArrayList<Equipment>(4);
		classList.add(new ClassRecord(this, classType, level));
		alignment = getAlignmentFromString(align);
		primaryPersonality = PersonalityTraits.getInstance().getRandom(alignment);
		secondaryPersonality = PersonalityTraits.getInstance().getRandom(null);
		age = BASE_AGE;
		sweepRate = 0;
		updateStats();
		setPerfectHealth();
	}

	/**
		Constructor (double class).
		Note that this re-rolls ability scores set in the base constructor.
	*/
	public Character(String race, 
		String class1, int level1, 
		String class2, int level2, 
		String align) 
	{
		this(race, class1, level1, align);
		assert level2 >= 0;
		classList.clear();
		ClassType classType1 = ClassIndex.getTypeFromName(class1);
		ClassType classType2 = ClassIndex.getTypeFromName(class2);
		rollBaseAbilities();
		if (boostInitialAbilities) {
			boostBaseAbilities(classType1, level1, classType2, level2);
		}
		classList.add(new ClassRecord(this, classType1, level1));
		classList.add(new ClassRecord(this, classType2, level2));
		updateStats();
		setPerfectHealth();
	}
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Name accessor
	public String getName() { return name; }

	// Implement null Monster equipment methods
	@Override public Armor getArmor() { return armorWorn; }
	@Override public Armor getShield() { return shieldHeld; }
	@Override public Weapon getWeapon() { return weaponInHand; }

	// Access equipment carried
	public int getEquipmentCount() { return equipList.size(); }
	public Equipment getEquipment(int i) { return equipList.get(i); }
	public void addEquipment(Equipment equip) { equipList.add(equip); }
	public void dropAllEquipment() { equipList.clear(); }

	/**
		Roll base random ability scores.
	*/
	private void rollBaseAbilities() {
		for (int i = 0; i < Ability.size(); i++) {
			abilityScores[i] = ABILITY_DICE.roll();
		}
	}

	/**
		Boost starting abilities for a single-classed character.
	*/
	private void boostBaseAbilities(ClassType type, int level) {
		boostBaseAbilities(type, level, null, 0);	
	}

	/**
		Boost starting ability scores weighted by class and level.
	*/
	private void boostBaseAbilities(
		ClassType type1, int level1, 
		ClassType type2, int level2)
	{
		assert type1 != null;
		int[] weights = type1.getAbilityPriorityWeights();
		if (type2 != null) {
			int[] weights2 = type2.getAbilityPriorityWeights();
			for (int i = 0; i < Ability.size(); i++) {
				weights[i] += weights2[i];			
			}
		}		
		int numToBoost = Math.max(level1, level2) + ABILITY_BOOSTS_OVER_LEVEL;
		boostAbilitiesByWeights(weights, numToBoost);
	}

	/**
		Boost several random ability scores based on weights array.
	*/
	private void boostAbilitiesByWeights(int[] weights, int numToBoost) {
		assert weights.length == Ability.size();
		int sumWeights = Arrays.stream(weights).sum();
		for (int i = 0; i < numToBoost; i++) {
			boostAbilityByWeights(weights, sumWeights);
		} 
	}

	/**
		Boost one random ability score based on weights array.
		If we pick an ability at max, then pick any other random one.
	*/
	private void boostAbilityByWeights(int[] weights, int sumWeights) {
		assert weights.length == Ability.size();	
		int rollForIdx = Dice.roll(sumWeights);
		for (int idx = 0; idx < Ability.size(); idx++) {
			rollForIdx -= weights[idx];
			if (rollForIdx <= 0) {
				if (abilityScores[idx] < MAX_ABILITY_SCORE) {
					abilityScores[idx]++;
				}	
				else {
					boostRandomAbility();
				}
				break;
			}								
		}	
	}

	/**
		Boost a uniformly random ability score.
	*/
	private void boostRandomAbility() {
		if (!allAbilitiesAtMax()) {
			while (true) {
				int idx = Dice.roll(Ability.size()) - 1;
				if (abilityScores[idx] < MAX_ABILITY_SCORE) {
					abilityScores[idx]++;
					break;
				}					
			}	
		}
	}

	/**
		Are all of our ability scores at the maximum allowed?
	*/
	private boolean allAbilitiesAtMax() {
		for (int score: abilityScores) {
			if (score < MAX_ABILITY_SCORE) {
				return false;
			}		
		}	
		return true;
	}

	/**
		Get an ability score.
	*/
	public int getAbilityScore(Ability a) {
		int index = a.ordinal();
		int score = abilityScores[index];
		score -= abilityScoreDamage[index];
		if (a == Ability.Strength 
			&& hasFeat(Feat.ExceptionalStrength)) 
		{
			score += 3;
		}
		if (score < 0) {
			score = 0;
		}
		return score;
	}

	/**
		Take damage to an ability score.
	*/
	@Override
	protected void takeAbilityDamage(Ability a, int damage) {
		abilityScoreDamage[a.ordinal()] += damage;
		updateStats();
	}

	/**
		Clear any ability score damage.
	*/
	@Override
	public void zeroAbilityDamage() {
		for (int i = 0; i < Ability.size(); i++) {
			abilityScoreDamage[i] = 0;
		}
	}

	/**
		Adjust all ability scores by indicated modifiers.
	*/
	private void adjustAllAbilityScores(int... modifiers) {
		int oldCon = getAbilityScore(Ability.Constitution);
		for (int i = 0; i < Ability.size(); i++) {
			abilityScores[i] += modifiers[i];		
		}
		handleConChange(oldCon); 
		updateStats();
	}

	/**
		Handle a Constitution change to hit points.
	*/
	private void handleConChange(int oldCon) {
		for (ClassRecord record: classList) {
			record.handleConChange(oldCon);
		}
	}

	/**
		Are any of our ability scores zero?
		Because then we are dead.
	*/
	@Override
	protected boolean hasNullAbilityScore() {

		// This is a critical-path function.
		// For performance, check only the Strength ability
		// (c.f. Shadow Strength draining)
		// If other ability-drain abilities arise, add here.
		return getAbilityScore(Ability.Strength) <= 0;
	}

	/**
		Update derived statistics after any character changes.
	*/
	private void updateStats() {
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
		Do we have any class of the given base type?
	*/
	private boolean hasBaseClassType(BaseClassType type) {
		for (ClassRecord cr: classList) {
			if (cr.getClassType().getBaseClassType() == type) {
				return true;
			}
		}
		return false;	
	}		

	/**
		Get highest level class.
	*/
	public ClassRecord getTopClass() {
		ClassRecord topClass = classList.get(0);
		for (ClassRecord cr: classList) {
			if (cr.getLevel() > topClass.getLevel()) {
				topClass = cr;
			}
		}
		return topClass;
	}

	/**
		Get highest class level.
	*/
	public int getLevel() {
		return getTopClass().getLevel();
	}

	/**
		Get raw number of hit dice.
		When "HD" referenced in RPG rules, it means "level" for PC/NPCs.
		E.g.: Attacks, saves, magic effects, XP awards.
	*/
	public int getHitDiceNum() {
		return Math.max(getLevel(), 1);
	}

	/**
		Get adjusted armor class.
	*/
	private int computeArmorClass() { 
		int ac = BASE_ARMOR_CLASS - getAbilityBonus(Ability.Dexterity);
		if (armorWorn != null) {
			ac -= armorWorn.getBaseArmor() + armorWorn.getMagicBonus();
		}	
		if (shieldHeld != null) {
			ac -= shieldHeld.getBaseArmor() + shieldHeld.getMagicBonus();
		}
		if (ringWorn != null) {
			ac -= ringWorn.getMagicBonus();		
		}
		return ac;
	}

	/**
		Get movement based on armor.
	*/
	private int computeMoveInches() {
		float weight = getEncumbrance();
		int strength = getAbilityScore(Ability.Strength);
		if (weight <= strength * 1. / 3) {
			return 12;
		}
		else if (weight <= strength * 2. / 3) {
			return 9;
		}
		else if (weight <= strength) {
			return 6;
		}
		else {
			return 0;
		}
	}

	/**
		Get total encumbrance of equipment.
	*/
	public float getEncumbrance() {
		float sum = 0;
		for (Equipment equip: equipList) {
			sum += equip.getWeight();
		}
		return sum;
	}

	/**
		Get weapon-based melee attack routine.
	*/
	private Attack computeAttack() { 
		if (weaponInHand == null) {
			return null;
		}	
		else {
			String weaponName = weaponInHand.getName();
			int rate = 1;
			int atkBonus = baseAttackBonus() + getAbilityBonus(Ability.Strength) 
				+ weaponInHand.getMagicBonus();
			Dice damageDice = new Dice(weaponInHand.getBaseDamage());
			int damageAdd = damageDice.getAdd() + getAbilityBonus(Ability.Strength)
				+ weaponInHand.getMagicBonus();
			if (hasFeat(Feat.WeaponSpecialization)) {
				atkBonus += 2;
				damageAdd += 2;
			}
			damageDice.setAdd(damageAdd);
			EnergyType energy = weaponInHand.getEnergy();
			return new Attack(weaponName, rate, atkBonus, damageDice, energy);
		}	
	}

	/**
		Returns base attack bonus (max over all classes).
	*/
	private int baseAttackBonus() {
		int maxBAB = 0;
		for (ClassRecord record: classList) {
			if (record.attackBonus() > maxBAB) {
				maxBAB = record.attackBonus();
			}
		}
		return maxBAB;
	}

	/**
		Find what level of magic-to-hit we can strike.
	*/
	protected int getMagicHitLevel() {
		return (weaponInHand == null ? 0 : weaponInHand.getMagicBonus());
	}

	/**
		Set armor worn.
	*/
	public void setArmor(Armor armor) { 
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
		Set shield carried.
	*/
	public void setShield(Armor shield) { 
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
		Draw a particular weapon.
	*/
	public void drawWeapon(Weapon weapon) {
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
		Find shield in equipment list, if any.
	*/
	private Armor findShieldInEquip() {
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
		Draw next weapon in equipment iteratively.
	*/
	public void drawNextWeapon() {
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
		Draw best weapon against a given monster.
	*/
	@Override
	public void drawBestWeapon(Monster monster) {
		drawWeapon(null);
		int maxDamage = -1;
		Weapon bestWeapon = null;
		while (true) {
			drawNextWeapon();
			if (weaponInHand == null) {
				break;
			}
			int damage = maxDamageVsMonster(monster);
			if (damage > maxDamage) {
				maxDamage = damage;
				bestWeapon = weaponInHand;
			}							
		}
		drawWeapon(bestWeapon);
	}

	/**
		Lose a given piece of equipment.
	*/
	@Override
	protected void loseEquipment(Equipment equip) {
		assert equip != null;
		equipList.remove(equip);
		if (equip == armorWorn) {
			armorWorn = null;
		}
		if (equip == shieldHeld) {
			shieldHeld = null;
		}
		if (equip == weaponInHand) {
			weaponInHand = null;
		}
		if (equip == ringWorn) {
			ringWorn = null;
		}
		if (equip == wandHeld) {
			wandHeld = null;
		}
		updateStats();
	}

	/**
		Compute max damage against a given monster.
	*/
	private int maxDamageVsMonster(Monster monster) {
		Attack atk = getAttack();
		if (atk == null) {
			return 0;
		}
		if (monster != null && !canAttack(monster)) {
			return 0;
		}
		return atk.getDamage().maxRoll();		
	}

	/**
		Returns total XP (sum over all classes).
	*/
	public int totalXP() {
		int sum = 0;
		for (ClassRecord record: classList) {
			sum += record.getXP();
		}
		return sum;
	}

	/**
		Add XP to the first character class.
	*/
	@Override
	public void addXP(int xp) {
		addXP(xp, 0);	
	}

	/**
		Add XP to a specified character class.
	*/
	public void addXP(int xp, int classIdx) {
		ClassRecord cr = classList.get(classIdx);
		xp = adjustForPrimeReq(xp, cr);
		cr.addXP(xp);
		updateStats();
	}

	/**
		Adjust XP award for the class prime requisite.
	*/
	private int adjustForPrimeReq(int xp, ClassRecord cr) {
		if (!APPLY_BONUS_XP) {
			return xp;
		}
		else {
			Ability primeReq = cr.getClassType().getPrimeReq();
			int score = getAbilityScore(primeReq);
			int bonusPct = Ability.bonusPercentXP(score);
			return xp + xp * bonusPct / 100;
		}
	}

	/**
		Lose a level (e.g., energy drain).
	*/
	@Override 
	protected void loseLevel() {
		int damage = maxHitPoints - hitPoints;
		getTopClass().loseLevel();
		updateStats();
		hitPoints = maxHitPoints - damage;
		boundHitPoints();
	}

	/**
		Find maximum hit points (supremum over all classes).
	*/
	private int findSupMaxHitPoints() {
		int supMaxHP = 0;
		for (ClassRecord record: classList) {
			int hitPoints = record.getHitPoints();
			if (hasFeat(Feat.Toughness)) {
				hitPoints += record.getLevel() * 2;
			}
			supMaxHP = Math.max(supMaxHP, hitPoints);
		}
		return supMaxHP;
	}

	/**
		Create evil human NPC with equipment from class title.
	*/
	public static Character evilNPCFromTitle(String title) {
		ClassType classType = ClassIndex.getTypeFromTitle(title);
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
		Set basic equipment by class.
	*/
	public void setBasicEquipment() {
		if (hasBaseClassType(BaseClassType.Fighter)) {
			switch(getLevel()) {
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
			if (hasBaseClassType(BaseClassType.Wizard)) {
				setArmor(Armor.makeType(Armor.Type.Chain));
				setShield(null);			
			}
		}
		else if (hasBaseClassType(BaseClassType.Thief)) {
			setArmor(Armor.makeType(Armor.Type.Leather));
			addEquipment(Weapon.randomThieving());
			addEquipment(Weapon.dagger());
		}
		else if (hasBaseClassType(BaseClassType.Wizard)) {
			addEquipment(Weapon.dagger());
		}
		else {
			System.err.println("Unhandled base class type.");		
		}
	}

	/**
		Make rolls to boost magic items for one level.
	*/
	@Override
	public void boostMagicItemsOneLevel() {
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
		Boost all magic items to the character's top level. 
	*/
	public void boostMagicItemsToLevel() {
		int level = getLevel();
		for (int i = 1; i < level; i++) {
			boostMagicItemsOneLevel();
		}
	}

	/**
		Check if a magic item boost is gained. 
	*/
	boolean getMagicBoost() {
		return Dice.roll(100) <= pctMagicPerLevel;
	}

	/**
		Increment magic ring worn.
	*/
	void incrementRing() {
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
		Increment magic wand carried.
	*/
	void incrementWand() {
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
		Roll a saving throw with modifier.
	*/
	protected boolean rollSave(SavingThrows.Type type, int modifier) {
		ClassRecord bestClass = bestClassForSave(type);
		if (ringWorn != null) {
			modifier += ringWorn.getMagicBonus();
		}
		return SavingThrows.getInstance().rollSave(
			type, bestClass.getClassType().getSaveAsClass(), 
			bestClass.getLevel(), modifier); 
	}

	/**
		Find the best class to use for a given saving throw.
	*/
	private ClassRecord bestClassForSave(SavingThrows.Type saveType) {
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
	Set if we should be using optional feats.
	*/
	public static void setFeatUsage(boolean permit) {
		useFeats = permit;	
	}

	/**
		Are we using optional feats?
	*/
	public static boolean useFeats() {
		return useFeats;
	}
	
	/**
		Does this character have a given feat?
	*/
	@Override
	protected boolean hasFeat(Feat feat) {
		for (ClassRecord rec: classList) {
			if (rec.hasFeat(feat)) {
				return true;
			}
		}
		return false;
	}

	/**
		Set if we should be using sweep attacks.
	*/
	public static void setSweepAttacks(boolean permit) {
		useSweepAttacks = permit;	
	}

	/**
		Are we using sweep attacks?
	*/
	public static boolean useSweepAttacks() {
		return useSweepAttacks;
	}

	/**
		How many attacks does this character get against 1 HD opponents?
		Give this fighter bonus to any class with BAB 1 or above.
	*/
	private int computeSweepRate() {
		int rate = 0;
		for (ClassRecord rec: classList) {
			if (rec.attackBonus() >= rec.getLevel()) {
				if (rec.getLevel() > rate) {
					rate = rec.getLevel();
				}	
			}
		}
		return rate;
	}

	/**
		Get this character's recorded sweep rate.
	*/
	@Override
	protected int getSweepRate() {
		return sweepRate;
	}
	
	/**
		Get the age of the character.
	*/
	public int getAge() {
		return age;
	}

	/**
		Increment the age of the character.
	*/
	public void incrementAge() {

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
		Get the age category of the character.
		Caution: Works for Humans only.
	*/
	AgeCategory getAgeCategory() {
		if (age <= 13) {
			return AgeCategory.Child;
		}
		else if (age <= 20) {
			return AgeCategory.YoungAdult;
		}
		else if (age <= 40) {
			return AgeCategory.Mature;
		}
		else if (age <= 60) {
			return AgeCategory.MiddleAged;
		}
		else if (age <= 90) {
			return AgeCategory.Old;
		}
		else {
			return AgeCategory.Venerable;
		}
	}

	/**
		Adjust ability scores on reaching a new age category.
	*/
	private void ageAdjustAbilities() {
		switch(getAgeCategory()) {
			case Child: break;
			case YoungAdult: adjustAllAbilityScores(0, 0, -1, 0, +1, 0); break;
			case Mature:     adjustAllAbilityScores(+1, 0, +1, 0, 0, 0); break;
			case MiddleAged: adjustAllAbilityScores(-1, +1, +1, 0, -1, 0); break;
			case Old:        adjustAllAbilityScores(-2, 0, +1, -2, -1, 0); break;
			case Venerable:  adjustAllAbilityScores(-1, +1, +1, -1, -1, 0); break;
			default: System.err.println("Unknown age category.");
		}	
	}

	/**
		Adjust levels on advancing age.
		Prevents fighters from living indefinitely long;
		follows pattern Gygax showed for Conan in Dragon #36.
	*/
	private void ageAdjustLevels() {
		if (age > 40) {
			for (ClassRecord record: classList) {
				switch(record.getClassType().getBaseClassType()) {
					case Fighter:
						if (age % 2 == 0) {
							record.loseLevel(); 
						}
						break;
					case Thief: 
						if (age % 5 == 0) {
							record.loseLevel(); 
						}
						break;
					case Wizard:
					case Cleric:
						break;
					default: 
						System.err.println("Unknown base class type.");
				}
			}
		}
	}

	/**
		Generate random treasure value by men type "A",
		scaled by level and nominal men number appearing.
		(Recommended for wilderness encounters only.)
	*/
	public int getTreasureValue() {
		final int avgNum = 165;
		int level = Math.max(getLevel(), 1);
		return MonsterTreasureTable.getInstance()
			.randomValueByCode('A') * level / avgNum;
	}

	/**
		Convert string to alignment (random if null).
	*/
	Alignment getAlignmentFromString(String s) {
		Alignment align = Alignment.getFromString(s);
		if (align == null) {
			align = Alignment.randomNormal();
		}
		return align;	
	}

	/**
		Mutator to initial ability/hp boost switch.
	*/
	public static void setBoostInitialAbilities(boolean boost) {
		boostInitialAbilities = boost; 
	}

	/**
		Accessor to initial ability/hp boost switch.
	*/
	public static boolean getBoostInitialAbilities() {
		return boostInitialAbilities;	
	}

	/**
		Mutator to aging effect switch.
	*/
	public static void setApplyAgingEffects(boolean aging) {
		applyAgingEffects = aging;
	}

	/**
		Mutator to percent magic per level. 
	*/
	public static void setPctMagicPerLevel(int pct) {
		pctMagicPerLevel = pct;	
	}

	/**
		Is this character a person? 
	*/
	@Override
	public boolean isPerson() {
		return true;
	}

	/**
		Get the character's spell memory, if any.
		Warning: This assumes a character has at most one spellcasting class.
		If multiple spell classes supported, this system needs reworking.
	*/
	@Override
	public SpellMemory getSpellMemory() {
		for (ClassRecord classRec: classList) {
			if (classRec.getSpellMemory() != null) {
				return classRec.getSpellMemory();
			}
		}
		return null;
	}

	//--------------------------------------------------------------------------
	//  Printing Methods
	//--------------------------------------------------------------------------

	// Print option switches
	public static void setPrintFeats(boolean p) { printFeats = p; }
	public static void setPrintAbilities(boolean p) { printAbilities = p; }
	public static void setPrintEquipment(boolean p) { printEquipment = p; }
	public static void setPrintPersonality(boolean p) { printPersonality = p; }
	public static void setPrintSpells(boolean p) { printSpells = p; }

	/**
		Identify this object as a string.
	*/
	public String toString() {

		// Basic stat string
		String s = name + ", " + race + " " + classString(true);
		s += ": AC " + getAC() + ", MV " + getMV() + ", HD " + getHD()
			+ ", hp " + getHP() + ", Atk " + getAttack();
		
		// Optional stuff
		if (printAbilities) {
			s = addClause(s, abilityString());
		}
		if (printPersonality) {
			s = addClause(s, toSentenceCase(personalityString()));
		}
		if (printEquipment) {
			s = addClause(s, "Gear: ", toSentenceCase(equipString()));
		}
		if (printFeats) {
			s = addClause(s, "Feats: ", toSentenceCase(featString()));
		}
		if (printSpells) {
			s = addClause(s, "Spells: ", toSentenceCase(spellString()));
		}
		return s + ".";
	}	

	/**
		Short String representation of this character.
	*/
	public String shortString() {
		return name + ", " + race + " " 
			+ classString(true) + ": hp " + getHP();
	}

	/**
		String representation of all class and levels.
	*/
	private String classString(boolean slashes) {
		String s = "";
		for (ClassRecord record: classList) {
			if (slashes && s.length() > 0) {
				s += "/";
			}
			s += record.getClassType().getAbbreviation() 
				+ record.getLevel();
		}
		return s;
	}

	/**
		String representation of all ability scores.
	*/
	private String abilityString() {
		String s = "";
		for (Ability a: Ability.values()) {
			s = addItem(s, a.getAbbreviation() 
				+ " " + getAbilityScore(a));
		}
		return s;
	}

	/**
		String representation of alignment and personality.
	*/
	private String personalityString() {
		return alignment + ", " 
			+ primaryPersonality + ", " 
			+ secondaryPersonality;	
	}

	/**
		String representation of equipment.
	*/
	private String equipString() {
		String s = "";
		for (Equipment equip: equipList) {
			s = addItem(s, equip);
		}
		return s;
	}

	/**
		String representation of feats.
	*/
	public String featString() {
		String s = "";
		for (ClassRecord rec: classList) {
			s = addItem(s, rec.featsString());
		}
		return s;
	}

	/**
		String representation of skills.
	*/
	public String skillString() {
		String s = "";
		for (ClassRecord rec: classList) {
			s = addItem(s, rec.skillsString());
		}
		return s;
	}

	/**
		String representation of spells.
	*/
	public String spellString() {
		String s = "";
		for (ClassRecord cr: classList) {
			if (cr.getClassType().usesSpells()) {
				s = addItem(s, cr.spellsString());
			}
		}
		return s;
	}

	/**
		Add item to a string if not null.
	*/
	private String addItem(String s, Object item) {
		return item == null ? s : s + (s.length() > 0 ? ", " : "") + item;
	}

	/**
		Add an independent clause to a string, if nonempty.
	*/
	private String addClause(String s, String clause) {
		return clause.length() > 0 ? s + "; " + clause : s;
	}

	/**
		Add a label & clause to a string, if nonempty.
	*/
	private String addClause(String s, String label, String clause) {
		return clause.length() > 0 ? addClause(s, label + clause) : s;
	}

	/**
		Convert a string to sentence case.
	*/
	public static String toSentenceCase(String s) {
		if (s.length() > 0) {
			return java.lang.Character.toUpperCase(s.charAt(0))
				+ s.substring(1).toLowerCase();
		}
		else {
			return s;
		}
	}

	/**
		Create a short race/class descriptor.
	*/
	public String getRaceClassDesc() {
		return race + " " + classString(true);	
	}

	/**
		Create a filename identifier.
	*/
	public String getFilename() {
		return name + "-" + race + classString(false);
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
		Dice.initialize();
		Character.setBoostInitialAbilities(true);
		Character p = new Character("Human", "Fighter", 1, null);
		p.setBasicEquipment();
		p.drawBestWeapon(null);
		System.out.println(p);
	}
}
