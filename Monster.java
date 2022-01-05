import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/******************************************************************************
* Monster (hostile or benign creature).
*
* @author   Daniel R. Collins (dcollins@superdan.net)
* @since    2014-05-20
******************************************************************************/

public class Monster {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Sentinel value for undefined EHD. */
	public static final int UNDEFINED_EHD = -1;

	/** Sides on standard hit dice. */
	private static final int BASE_HIT_DIE = 6;

	/** Maximum enemies who can melee us at once. */
	private static final int MAX_MELEERS = 6;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	String race;
	String sourceBook;
	char type;
	char environment;
	Dice numberAppearing;
	int armorClass;
	int moveInches;
	Dice hitDice;
	int inLairPct;
	char treasureType;
	Attack attack;
	Alignment alignment;
	float hitDiceDecimal;
	int equivalentHitDice;
	int hitPoints;
	int maxHitPoints;
	int dragonAge;
	int breathCharges;
	int killTally;
	int timesMeleed;
	Monster host;
	Set<SpecialType> specialList;
	Set<SpecialType> conditionList;
	AbstractMap<SpecialType, Integer> specialValues;
	SpellMemory spellMemory;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	* Very basic constructor (for testing).
	*/
	public Monster (String race, int AC, int MV, int hitDice, int damageDice) {
		this(race, AC, MV, 
			new Dice(hitDice, BASE_HIT_DIE), 
			new Attack(hitDice, damageDice));
	}

	/**
	* Fairly basic constructor (for testing).
	*/
	public Monster (String race, int AC, int MV, Dice hitDice, Attack attack) {
		this.race = race;
		this.armorClass = AC;
		this.moveInches = MV;
		this.hitDice = hitDice;
		this.attack = attack;
		this.alignment = Alignment.Neutral;
		equivalentHitDice = hitDice.getNum();
		specialList = EnumSet.noneOf(SpecialType.class);
		conditionList = EnumSet.noneOf(SpecialType.class);
		specialValues = new EnumMap<SpecialType, Integer>(SpecialType.class);
		rollHitPoints();
	}

	/**
   * Creates a prototype monster from text file specification.
	*
	* Some values are left undefined by this method:
	* E.g., dragon random ages (and hp), spells memorized.
	* For those, see the spawn() function.
	*
	* @param s specification string array.
	*/
	public Monster (String[] s) {

		// Primary fields
		race = s[0];
		numberAppearing = new Dice(s[1]);
		armorClass = CSVReader.parseInt(s[2]);
		moveInches = CSVReader.parseInt(s[3]);
		hitDice = parseHitDice(s[4]);
		inLairPct = CSVReader.parseInt(s[5]);
		treasureType = s[6].charAt(0);
		attack = parseAttackRoutine(s[7], s[8]);
		alignment = Alignment.getFromChar(s[9].charAt(0));
		type = s[10].charAt(0);
		equivalentHitDice = parseEHD(s[11]);
		hitDiceDecimal = parseFloat(s[12]);
		environment = s[13].charAt(0);
		sourceBook = s[14];

		// Special abilities & conditions
		specialList = EnumSet.noneOf(SpecialType.class);
		conditionList = EnumSet.noneOf(SpecialType.class);
		specialValues = new EnumMap<SpecialType, Integer>(SpecialType.class);
		setSpecialAbilities(s[15]);

		// Other fields
		spellMemory = null;
		rollHitPoints();
	}

	/**
	* Constructor (copy).
	*/
	public Monster (Monster src) {
		race = src.race;
		numberAppearing = src.numberAppearing;
		armorClass = src.armorClass;
		moveInches = src.moveInches;
		hitDice = src.hitDice;
		inLairPct = src.inLairPct;
		treasureType = src.treasureType;
		attack = src.attack;
		alignment = src.alignment;
		type = src.type;
		equivalentHitDice = src.equivalentHitDice;
		sourceBook = src.sourceBook;
		hitDiceDecimal = src.hitDiceDecimal;
		killTally = src.killTally;
		host = src.host;
		specialList = EnumSet.copyOf(src.specialList);
		conditionList = EnumSet.copyOf(src.conditionList);
		specialValues = new EnumMap<SpecialType, Integer>(src.specialValues);
		maxHitPoints = src.maxHitPoints;
		hitPoints = src.hitPoints;
		breathCharges = src.breathCharges;
		dragonAge = src.dragonAge;
		spellMemory = null;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Primary accessors
	public String getRace () { return race; }
	public String getSourceBook () { return sourceBook; }
	public Dice getNumberAppearing () { return numberAppearing; }
	public int getArmorClass () { return armorClass; }
	public int getHitPoints () { return hitPoints; }
	public int getMaxHitPoints () { return maxHitPoints; }
	public int getInLairPct () { return inLairPct; }
	public Dice getHitDice () { return hitDice; }
	public int getHitDiceNum () { return hitDice.getNum(); }
	public int getLevel () { return getHitDiceNum(); }
	public char getTreasureType () { return treasureType; }
	public Attack getAttack () { return attack; }
	public Alignment getAlignment () { return alignment; }
	public char getType () { return type; }
	public int getEquivalentHitDice () { return equivalentHitDice; }
	public char getEnvironment () { return environment; }
	public int getKillTally () { return killTally; }
	public int getTimesMeleed () { return timesMeleed; }

	// Shortcut accessors
	public int getAC () { return getArmorClass(); }
	public int getHD () { return getHitDiceNum(); }
	public int getHP () { return getHitPoints(); }
	public int getMV () { return getMoveInches(); }
	public int getEHD () { return getEquivalentHitDice(); }

	// Basic mutators
	public void setAlignment (Alignment align) { alignment = align; }
	public void clearTimesMeleed () { timesMeleed = 0; }
	public void incTimesMeleed () { timesMeleed++; }

	// Public null methods for subclass inheritance
	public Armor getArmor () { return null; }
	public Armor getShield () { return null; }
	public Weapon getWeapon () { return null; }
	public void drawBestWeapon (Monster m) {}
	public void sheatheWeapon () {}
	public void boostMagicItemsOneLevel () {}
	public void zeroAbilityDamage () {} 
	public void addXP (int xp) {}

	// Protected null methods for subclass inheritance
	protected void loseEquipment (Equipment e) {}
	protected void takeAbilityDamage (Ability a, int n) {} 
	protected boolean hasNullAbilityScore () { return false; }
	protected boolean hasFeat (Feat feat) { return false; }
	protected int getSweepRate() { return 0; }

	/**
	* Parse hit dice record from short descriptor.
	*/
	private Dice parseHitDice (String s) {
		Pattern p = Pattern.compile("(\\d+)([x/]\\d+)?([+-]\\d+)?");
		Matcher m = p.matcher(s);
		if (m.matches()) {
			int mul = 1, add = 0;
			int num = Integer.parseInt(m.group(1));
			if (m.group(2) != null) {
				boolean positive = m.group(2).startsWith("x");
				int val = Integer.parseInt(m.group(2).substring(1));
				mul = positive ? val : -val;
			}
			if (m.group(3) != null) {
				add = Integer.parseInt(m.group(3));
			}
			return new Dice(num, BASE_HIT_DIE, mul, add);
		}
		System.err.println("Could not parse hit dice descriptor: " + s);
		return null;
	}

	/**
	* Parse attack routine from rate and damage.
	*/
	private Attack parseAttackRoutine (String atkRate, String damageDesc) {
		int attackBonus = hitDice.getNum();
		int attackRate = Integer.parseInt(atkRate);
		return new Attack(null, attackRate, attackBonus, new Dice(damageDesc));
	}

	/**
	* Parse special ability list from descriptor string.
	*/
	private void setSpecialAbilities (String s) {
		if (s.length() > 1) {
			String parts[] = s.split(", ");
			for (String part: parts) {
				SpecialAbility ability = 
					SpecialAbility.createFromString(part);
				if (ability != null) {
					addSpecial(ability.getType(), ability.getParam());
				}
			}     
		}
		addImpliedSpecials();
	}

	/**
	* Add some special qualifiers based on name or type.
	*/
	private void addImpliedSpecials() {
		if (getRace().startsWith("Dragon")) {
			addSpecial(SpecialType.Dragon);
			dragonAge = parseDragonAge();
		}
		if (getRace().startsWith("Golem"))
			addSpecial(SpecialType.Golem);
		if (getType() == 'U')
			addSpecial(SpecialType.Undead);
	}

	/**
	* Parse the EHD value (possibly undefined).
	*/
	private int parseEHD (String s) {
		return s.equals("?") ? UNDEFINED_EHD : Integer.parseInt(s);
	}

	/**
	* Spawn a new monster of this type, with different hit points.
	*/
	public Monster spawn () {

		// NPC-types
		if (hasSpecial(SpecialType.NPC)) {
			Character c = Character.evilNPCFromTitle(race);
			c.race = race; // Reset race name for kill tally
			for (SpecialType s: specialList) {
				c.addSpecial(s, getSpecialParam(s));
			}
			return c;
		}
		
		// Standard monsters
		else {
			Monster m = new Monster(this);
			if (m.hasSpecial(SpecialType.Dragon)) {
				m.rollDragonAge();
			}
			if (m.hasSpecial(SpecialType.Spells)) {
				m.memorizeSpells();
			}
			m.rollHitPoints();
			return m;
		}
	}

	/**
	* Roll hit points from hit dice.
	*/
	private void rollHitPoints () {
		if (hasSpecial(SpecialType.Dragon)) {
			maxHitPoints = hitDice.getNum() * getDragonAge();
		}
		else if (hasSpecial(SpecialType.ManyHeads)) {
			maxHitPoints = hitDice.maxRoll();
		}
		else {
			maxHitPoints = hitDice.roll();
		}
		maxHitPoints = Math.max(maxHitPoints, 1);
		hitPoints = maxHitPoints;
	}

	/**
	* Take damage (minimum 0 hp).
	*/
	private void takeDamage (int damage) {
		hitPoints -= damage;
		boundHitPoints();
		headCount();
	}

	/**
	* Check if the monster is out of the fight.
	*/
	public boolean horsDeCombat() {
		return hitPoints <= 0
			|| hasNullAbilityScore()
			|| hasDisablingCondition();
	}

	/**
	* Set to perfect health.
	*/
	public void setPerfectHealth () {
		hitPoints = getMaxHitPoints();
		conditionList.clear();
		zeroAbilityDamage();
	}

	/**
	* Bound current hit points.
	*/
	protected void boundHitPoints () {
		int min = 0;
		int max = getMaxHitPoints();
		if (hitPoints < min) hitPoints = min;
		if (hitPoints > max) hitPoints = max;
	}

	/**
	* Check if we are subject to more melee attacks.
	*/
	public boolean isOpenToMelee () { 
		return !horsDeCombat() && (timesMeleed < MAX_MELEERS); 
	}

	/*
	* Commentary on how melee attacks are allocated:
	*
	* Per 1E DMG p. 70, "it is generally not possible to select a specific
	* opponent in a mass melee... simply use some random number generation
	* to find out which attacks are upon which opponents."
	* 
	* For simplicity, we've done this here, on a per-attack basis.
	*
	* The DMG passage does go on, "If characters or similar intelligent
	* creatures are able to single out an opponent or opponents, then the
	* concerned figures will remain locked in melee until one side is dead..."
	* Likewise, Swords & Spells p. 18 says that high-level figures should
	* seek each other out for combat instead of low-level types.
	*
	* However, that exception is hard to implement, partly because
	* (a) the criteria for the "if" above is unclear, (b) we don't have data 
	* on intelligence of monsters, (c) sweep attacks further suggest an abstract 
	* approach (given a fighter is likely to have more attacks than immediately 
	* adjacent opponents), and (d) no current use-case of this code suite
	* sees a high-level monster in a party with 1st-level types.
	* 
	* Additionally, there are several high-attack monsters in the MM that 
	* explicitly allow or require their attacks to be spread out among multiple
	* opponents, which would require exception code on the exception.
	*
	* E.g.: Hydra, Squid, Octopus, Elephant, Mastodon, Scorpion.
	*/

	/**
	* Take our turn against an enemy party.
	*/
	public void takeTurn (Party friends, Party enemies) {
		if (checkSpecialsInMelee(friends, enemies)) return;
		boolean isSweeping = useSweepAttacks(enemies);
		int attackRate = getAttackRate(isSweeping);
		for (int i = 0; i < attackRate; i++) {
			Monster target = enemies.getRandomMeleeTarget();
			if (target != null) {
				if (isSweeping && target.getLevel() > 1) continue;
				boolean isLastAttack = (i == attackRate - 1);
				singleAttack(getAttack(), target, isLastAttack);
				target.incTimesMeleed();
			}
		}
	}

	/**
	* Check for special ability use in melee.
	* @return true if our turn is consumed (no melee attacks)
	*/
	private boolean checkSpecialsInMelee (Party friends, Party enemies) {

		// Primary abilities & conditions
		checkRegeneration();
		if (checkWebbing()) return true;
		if (checkConfusion(friends)) return true;
		if (checkBreathWeapon(enemies)) return true;
		if (checkCastSpellInMelee(enemies)) return true;
		if (checkDrawNewWeapon(enemies)) return true;

		// Secondary abilities (in block for performance)
		if (!specialList.isEmpty()) {
			checkSlowing(enemies);
			if (checkAttachment()) return true;
			if (checkManyEyesSalvo(enemies)) return true;
		}
		return false;
	}

	/**
	* Check if we should be making sweep attacks.
	*/
	private boolean useSweepAttacks(Party enemies) {
		return Character.useSweepAttacks() 
			&& getSweepRate() > getAttack().getRate()
			&& enemies.isModeFirstLevel();
	}

	/**
	* Get the current attack rate.
	*/
	private int getAttackRate(boolean isSweeping) {
		if (isSweeping) {
			return getSweepRate();
		}
		else {
			int rate = getAttack().getRate();
			if (hasFeat(Feat.RapidStrike) && Dice.coinFlip()) rate++;
			return rate;
		}
	}

	/**
	* Make one attack on another creature.
	*/
	private void singleAttack (Attack attack, Monster target, boolean last) {
		if (canAttack(target) && target.isInPhase()) {
			int naturalRoll = Dice.roll(20);
			int totalRoll = naturalRoll + attack.getBonus() 
				+ target.getAC() + hitModifier(target);
			if (totalRoll >= 20 || naturalRoll == 20) {
				int damage = attack.rollDamage();
				damage = checkDamageReduction(target, damage);
				target.takeDamage(damage);
				checkSpecialOnHit(target, totalRoll, last);
			}
		}
	}

	/**
	* Can we feasibly attack this target in melee?
	* This needs to be deterministic (no random rolls),
	* because it's checked by the FightManager to stop a fight.
	*/
	protected boolean canAttack (Monster target) {

		// Check any attack available
		if (getAttack() == null) {
			return false;
		}

		// Check silver to hit
		if (target.hasSpecial(SpecialType.SilverToHit)) {
			boolean hasSilverWeapon = (getWeapon() != null 
				&& getWeapon().getMaterial() == Weapon.Material.Silver);
			boolean atkSilverToHit = hasSpecial(SpecialType.SilverToHit);
			if (!hasSilverWeapon && !atkSilverToHit && getMagicHitLevel() <= 0)
				return false;
		}

		// Check magic to hit
		if (target.hasSpecial(SpecialType.MagicToHit)) {
			int targetMagicReq = target.getSpecialParam(SpecialType.MagicToHit);
			if (getMagicHitLevel() < targetMagicReq)
				return false;
		}

		// Check chop-immunity (non-energy weapon blows)
		if (target.hasSpecial(SpecialType.ChopImmunity)) {
			EnergyType atkEnergy = getAttack().getEnergy();
			if (atkEnergy == null || target.isImmuneToEnergy(atkEnergy))
				return false;
		}

		return true;
	}

	/**
	* Find what level of magic-to-hit we can strike.
	*/
	protected int getMagicHitLevel () {
		int magicHitLevel = 0;

		// Check if this monster has magic-to-hit
		if (hasSpecial(SpecialType.MagicToHit))
			magicHitLevel = getSpecialParam(SpecialType.MagicToHit);

		// Check if this monster has high hit dice
		int fractionHD = getHD() / 4;
		return Math.max(magicHitLevel, fractionHD);
	}

	/**
	* Return hit modifier against a target.
	* @return Modifier to hit.
	*/
	protected int hitModifier (Monster target) {
		int modifier = 0;

		// General hit bonus
		if (hasSpecial(SpecialType.HitBonus)) {
			modifier += getSpecialParam(SpecialType.HitBonus);
		}

		// Berserker bonus (NPC)
		if (hasSpecial(SpecialType.Berserking) 
				|| hasCondition(SpecialType.Berserking)) {
			modifier += 2;
		}

		// Berserker bonus (Feat)
		if (hasFeat(Feat.Berserking)) {
			modifier += 4;
		}

		// Giant & target dodges giants
		if (getType() == 'H' && getHD() >= 4
				&& target.hasSpecial(SpecialType.DodgeGiants)) {
			modifier -= 4;    
		}

		// Blinking (2-in-6 for rear attack)
		if (hasSpecial(SpecialType.Blinking) && new Dice(6).roll() <= 2) {
			modifier += 2;
		}

		// Phasing (3-in-6 for rear attack)
		if (hasSpecial(SpecialType.Phasing) && new Dice(6).roll() <= 3) {
			modifier += 2;
		}
		
		// Stench nausea
		if (hasCondition(SpecialType.Stench)) {
			modifier -= 2;		
		}

		// Blindness
		if (hasCondition(SpecialType.Blindness)) {
			modifier -= 4;
		}

		// Target displacement
		if (target.hasSpecial(SpecialType.Displacement)) {
			modifier -= 2;
		}

		// Target invisibility
		if (target.hasSpecial(SpecialType.Invisibility)
				&& !hasSpecial(SpecialType.Detection)) {
			modifier -= 4;    
		}

		// Target gaze attack (assume we look aside)
		if (target.getGazeWeapon() != null) {
			modifier -= 4;
		}

		// Target protection from evil
		if (target.hasSpecial(SpecialType.ProtectionFromEvil)) {
			modifier -= 1;		
		}

		return modifier;
	}

	/**
	* Check for damage reduction on target.
	* @return Adjusted damage.
	*/
	private int checkDamageReduction (Monster target, int damage) {

		// Damage reduction ability
		if (target.hasSpecial(SpecialType.DamageReduction)) {
			damage /= 2;
		}

		return damage;
	}

	/**
	* Check for special ability triggers when we hit.
	*/
	private void checkSpecialOnHit (Monster target, int totalRoll, boolean isLastAttack) {

		// Special abilities of this attacking monster
		for (SpecialType s: specialList) {
			switch (s) {

				case Poison:
					if (isLastAttack) {
						castCondition(target, SpecialType.Poison, -getSpecialParam(s));
					}
					break;

				case Paralysis:
					castCondition(target, SpecialType.Paralysis);
					break;

				case Petrification:
					castCondition(target, SpecialType.Petrification);
					break;

				case EnergyDrain:
					if (!target.rollSave(SavingThrows.Type.Death)) {
						for (int j = 0; j < getSpecialParam(s); j++) {
							target.loseLevel();
						}
					}
					break;

				case Rotting: 
					target.addCondition(SpecialType.Rotting);
					break;

				case Immolation: 
					if (isLastAttack && new Dice(2, 6).roll() >= 7) {
						int damage = new Dice(3, 6).roll();
						castEnergy(target, damage, EnergyType.Fire, 
							SavingThrows.Type.Breath);
					}
					break;

				case SappingStrands:
					if (!isLastAttack && !target.hasCondition(SpecialType.SappingStrands)
							&& !target.rollSave(SavingThrows.Type.Death)) {
						int strength = target.getAbilityScore(Ability.Str);       
						target.takeAbilityDamage(Ability.Str, strength/2);
						target.addCondition(SpecialType.SappingStrands);
					}
					break;

				case Swallowing:
					if (!isLastAttack && totalRoll >= 25) {
						target.addCondition(SpecialType.Swallowing);
					}
					break;

				case StrengthDrain:
					target.takeAbilityDamage(Ability.Str, 1);
					break;

				case CharmTouch:
					castCharm(target, -getSpecialParam(s));
					break;

				case BloodDrain: 
					setHost(target);
					break;

				case Constriction:
					setHost(target);
					break;

				case Rending:
					if (totalRoll >= 25) {
						setHost(target);
					}
					break;

				case FleshEating:
					target.saveVsCondition(SpecialType.FleshEating, getHD());
			}
		}

		// Check attacker eating a piece of equipment
		if (canEatEquipment()) {
			eatEquipment(target);
		}

		// Check target eating weapon its hit with
		if (target.canEatEquipment()) {
			Weapon weapon = getWeapon();
			if (target.canEatEquipment(weapon)) {
				saveVsEquipmentLoss(weapon);			
			}
		}

		// Check target throwing off spores on hit
		if (target.hasSpecial(SpecialType.SporeCloud)) {
			if (Dice.coinFlip()) {
				target.castCondition(this, SpecialType.SporeCloud);
			}  
		}
	}

	/**
	* Make one special attack on an enemy party.
	* Pre-melee, ranged-attack specials go here.
	* Mostly uses just one ability (first one in monster list).
	*/
	public void makeSpecialAttack (Party enemy) {
		Monster target;
		Attack attack;
		int modifier;

		// Fear aura takes effect in addition to others
		if (hasSpecial(SpecialType.Fear)) {
			int maxLevel = getSpecialParam(SpecialType.Fear);
			for (Monster m: enemy) {
				if (m.getHD() <= maxLevel)
					m.saveVsCondition(SpecialType.Fear, getHD());
			}			
		}

		// Check for offensive spell-casting
		if (checkCastSpellPreMelee(enemy)) {
			return;
		}

		// Check monster special abilities list
		for (SpecialType s: specialList) {
			switch (s) {

				case RockHurling: 
					target = enemy.random();
					attack = new Attack("Rock", 1, getHD(), new Dice(2, 6));
					singleAttack(attack, target, false);
					return;

				case TailSpikes:
					attack = new Attack("Tail Spike", 1, getHD(), new Dice(1, 6));
					for (int i = 0; i < 6; i++) {
						target = enemy.random();
						singleAttack(attack, target, false);
					}
					return;

				case PetrifyingGaze:
					for (Monster targetPetrify: enemy) {
						castCondition(targetPetrify, SpecialType.Petrification);
					}
					return;

				case WallOfFire:
					Dice fireDamage = new Dice(1, 6); 
					for (Monster targetFire: enemy) {
						castEnergy(targetFire, fireDamage.roll(), 
							EnergyType.Fire, SavingThrows.Type.Spells);
					}
					return;

				case ConeOfCold:
					int damage = new Dice(8, 6).roll();
					int maxVictims = getMaxVictimsInCone(6);
					castEnergyArea(enemy, maxVictims, damage, 
						EnergyType.Cold, SavingThrows.Type.Spells);
					return;

				case AcidSpitting:
					target = enemy.random();
					attack = new Attack("Acid Spit", 1, getHD(), new Dice(2, 6));
					singleAttack(attack, target, false);
					return;

				case Whirlwind:

					// Assume we plow a straight line of given diameter
					// through roughly square enemy formation.
					int enemyLength = (int) Math.sqrt(enemy.size());
					int widthEffect = Math.min(getSpecialParam(s), enemyLength);
					int lengthEffect = Math.min(getMoveInches(), enemyLength);
					int numVictims = widthEffect * lengthEffect;
					List<Monster> victims = enemy.randomGroup(numVictims);
					for (Monster m: victims) {
						if (m.getHD() <= 1)
							m.saveVsCondition(SpecialType.BlownAway, getHD());
					}
					return;

				case Confusion:
					target = enemy.random();
					modifier = -getSpecialParam(s);
					if (target.hasFeat(Feat.IronWill)) modifier += 4;
					castCondition(target, SpecialType.Confusion, modifier);
					return;

				case MindBlast: 
					maxVictims = getMaxVictimsInCone(6);
					mindBlastArea(enemy, maxVictims);
					return;    

				case SappingStrands:
					attack = new Attack("Sapping Strand", 1, getHD(), new Dice(0, 6));
					for (int i = 0; i < 6; i++) {
						target = enemy.random();
						singleAttack(attack, target, false);
					}
					return;

				case Stench:
					for (Monster targetStench: enemy) {

						// Each enemy only needs to make one save vs. stinky monster party
						if (!targetStench.hasCondition(SpecialType.ResistStench)) {
							if (!targetStench.rollSave(SavingThrows.Type.Breath)) {
								targetStench.addCondition(SpecialType.Stench);
							}
							else {
								targetStench.addCondition(SpecialType.ResistStench);
							}
						}
					}
					return;

				case Charm:
					castCharm(enemy.random(), -getSpecialParam(s));
					return;
					
				case ManyEyeFunctions:
					manyEyesSalvo(enemy);
					return;
			}     
		}
	}

	/**
	* Add a special ability (no parameter).
	*/
	protected void addSpecial (SpecialType type) {
		addSpecial(type, 0);
	}

	/**
	* Add a special ability.
	*/
	protected void addSpecial (SpecialType type, int param) {
		specialList.add(type);	
		specialValues.put(type, Integer.valueOf(param));
	}

	/**
	* Check if this monster has a given type of special ability.
	*/
	private boolean hasSpecial (SpecialType type) {
		return specialList.contains(type);
	}

	/**
	* Get the parameter for a given special ability.
	*/
	private int getSpecialParam (SpecialType type) {
		return specialValues.get(type).intValue();
	}	

	/**
	* Add a condition suffered from a special ability.
	*/
	private void addCondition (SpecialType type) {
		conditionList.add(type);
	}

	/**
	* Remove a condition of a given special type.
	*/
	private void removeCondition (SpecialType type) {
		conditionList.remove(type);
	}

	/**
	* Check if we suffer from a given condition.
	*/
	public boolean hasCondition (SpecialType type) {
		return conditionList.contains(type);
	}

	/**
	* Check if we suffer from a disabling condition.
	*/
	private boolean hasDisablingCondition () {
		for (SpecialType s: conditionList) {
			if (s.isDisabling()) {
				return true;
			}
		}
		return false;
	}

	/**
	* Take a given condition unless we resist (no modifier).
	*/
	public void saveVsCondition (SpecialType condition, int casterLevel) {
		saveVsCondition(condition, casterLevel, 0);
	}

	/**
	* Take a given condition unless we resist.
	*/
	public void saveVsCondition (SpecialType condition, int casterLevel, int saveMod) {
		if (checkResistMagic(casterLevel)) return;
		if (condition.isUndeadImmune() && hasUndeadImmunity()) return;
		if (condition == SpecialType.Fear && hasSpecial(SpecialType.Fearlessness)) return;
		if (condition.isMentalAttack()) {
			saveMod += Ability.getBonus(getAbilityScore(Ability.Wis));
		}
		if (rollSave(condition.getSaveType(), saveMod)) return;
		addCondition(condition);
	}

	/**
	* Take energy damage; save for half. 
	*/
	public void saveVsEnergy (EnergyType energy, int damage, 
		SavingThrows.Type saveType, int casterLevel) 
	{
		if (isImmuneToEnergy(energy)) return;
		if (checkResistMagic(casterLevel)) return;
		boolean saved = rollSave(saveType);
		takeDamage(saved ? damage / 2 : damage);
	}

	/**
	* Check if we specially resist a magic spell.
	* @return true if we resist or have immunity to a spell.
	*/
	private boolean checkResistMagic (int casterLevel) {

		// Magic immunity
		if (hasSpecial(SpecialType.MagicImmunity)
				|| hasCondition(SpecialType.AntimagicSphere))
			return true;
		

		// Magic resistance
		if (hasSpecial(SpecialType.MagicResistance)) {
			int basePct = getSpecialParam(SpecialType.MagicResistance);
			int adjustPct = basePct + (casterLevel - 11) * 5;
			if (Dice.rollPct() <= adjustPct)
				return true;
		}
			
		// No resistance
		return false;	
	}

	/**
	* Get our breath weapon (if any).
	*/
	private SpecialType getBreathWeapon () {
		for (SpecialType s: specialList) {
			if (s.isBreathWeapon()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Get our gaze weapon (if any).
	*/
	private SpecialType getGazeWeapon () {
		for (SpecialType s: specialList) {
			if (s.isGazeWeapon()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Get our summons ability (if any).
	*/
	private SpecialType getSummonsAbility () {
		for (SpecialType s: specialList) {
			if (s.isSummonsAbility()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Get our attachment ability (if any).
	*/
	private SpecialType getAttachmentAbility () {
		for (SpecialType s: specialList) {
			if (s.isAttachmentAbility()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Regenerate hit points if appropriate.
	*/
	private void checkRegeneration () {
		if (hasSpecial(SpecialType.Regeneration)) {
			hitPoints += getSpecialParam(SpecialType.Regeneration);
			boundHitPoints();
		}
	}

	/**
	* Check if we're attached to some host.
	* If so, do special violence instead of a normal attack.
	* @return true if interrupted from making a normal attack
	*/
	private boolean checkAttachment () {
		if (host == null) {
			return false;
		}
		else if (host.horsDeCombat()) {
			host = null;
			return false;
		}
		else {
			SpecialType type = getAttachmentAbility();
			switch (type) {
				case BloodDrain: doBloodDrain(); break;
				case Constriction: doConstriction(); break;
				case Rending: doRending(); break;
				default: 
					System.err.println("Unknown attachment ability: " + type);
			}
			return true;
		}
	}

	/**
	* Drain blood from host.
	*/
	private void doBloodDrain () {
		assert(hasSpecial(SpecialType.BloodDrain) && host != null);
		int maxDamage = getSpecialParam(SpecialType.BloodDrain);
		int drain = new Dice(1, maxDamage).roll();
		host.takeDamage(drain);
	}

	/**
	* Constrict host for damage.
	* This ability is used by serpent/tentacle-type monsters.
	* Automatic initiation on any hit.
	*/
	private void doConstriction () {
		assert(hasSpecial(SpecialType.Constriction) && host != null);

		// Brain Consumption: OED converts this to 2-in-6
		// chance per round (80% likely to end in 1-4 rounds)
		if (hasSpecial(SpecialType.BrainConsumption)) {
			if (Dice.roll(6) <= 2)
				host.addCondition(SpecialType.Death);
		}

		// Default approximation is to automatically 
		// score half the full attack routine
		// (in book usually double per-tentacle hit)
		else {
			Attack atk = getAttack();
			int freeHits = (atk.getRate() + 1) / 2;
			for (int i = 0; i < freeHits; i++) {
				int damage = atk.getDamage().roll();
				host.takeDamage(damage);
			}
		}
	}

	/**
	* Rend our host.
	* This ability is used by animals like bears and cats.
	* Requires high total hit score to initiate (25+)
	* (book is nat 18+, converted here for convenience)
	*/
	private void doRending () {
		assert(hasSpecial(SpecialType.Rending) && host != null);

		// Automatically score full attack
		Attack atk = getAttack();
		for (int i = 0; i < atk.getRate(); i++) {
			int damage = atk.getDamage().roll();
			host.takeDamage(damage);
		}
	}

	/**
	* Check for a breath weapon attack.
	*  Estimates max number hit for given area.
	* @return Did we make a breath attack?
	*/
	private boolean checkBreathWeapon (Party enemy) {
		if (breathCharges > 0 && new Dice(2, 6).roll() >= 7) {
			SpecialType breathType = getBreathWeapon();
			int param = getSpecialParam(breathType);
			int damage, maxVictims, numVictims;
			switch (breathType) {

				case FireBreath: case SteamBreath:
					// As a data simplifying assumption, 
					// we assume damage dice = length of cone.
					switch (param) {
						case 0: damage = maxHitPoints; // Dragon
							maxVictims = getMaxVictimsInCone(9); break;
						default: damage = new Dice(param, 6).roll();
							maxVictims = getMaxVictimsInCone(param); break;
					}
					numVictims = getBreathVictims(enemy, maxVictims);
					castEnergyArea(enemy, numVictims, damage, 
						EnergyType.Fire, SavingThrows.Type.Breath);
					break;

				case ColdBreath: // Dragon only
					maxVictims = getMaxVictimsInCone(8);
					numVictims = getBreathVictims(enemy, maxVictims);
					castEnergyArea(enemy, numVictims, maxHitPoints, 
						EnergyType.Cold, SavingThrows.Type.Breath);
					break;

				case VoltBreath: // Dragon only
					numVictims = getBreathVictims(enemy, 5);
					castEnergyArea(enemy, numVictims, maxHitPoints, 
						EnergyType.Volt, SavingThrows.Type.Breath);
					break;

				case AcidBreath:
					switch (param) {
						case 0: damage = maxHitPoints; // Dragon
							maxVictims = 3; break;
						case 2: damage = new Dice(2, 6).roll(); // Beetle
							maxVictims = 1; break;
						default: damage = 0; maxVictims = 0;
							System.err.println("Unknown acid breath.");
					}
					numVictims = getBreathVictims(enemy, maxVictims);
					castEnergyArea(enemy, numVictims, damage, 
						EnergyType.Acid, SavingThrows.Type.Breath);
					break;

				case PetrifyingBreath: // Gorgon only
					maxVictims = getMaxVictimsInCone(6);
					numVictims = getBreathVictims(enemy, maxVictims);
					castConditionArea(enemy, numVictims, SpecialType.Petrification);
					break;

				case PoisonBreath: 
					if (param == 0) { // Dragon
						maxVictims = 10;
						numVictims = getBreathVictims(enemy, maxVictims);
						castEnergyArea(enemy, numVictims, maxHitPoints, 
							EnergyType.Poison, SavingThrows.Type.Breath);
					}
					else { // Iron Golem
						castConditionArea(enemy, 1, SpecialType.Poison);
					}
					break;

				default:
					System.err.println("Breath weapon type not handled:"
						+ breathType);
			}
			breathCharges--;
			return true;
		}
		return false;
	}

	/**
	* Compute maximum number of victims in a cone area.
	*
	*  Following red dragon breath, cone width is one-third the length.
	*  We assume that number of targets is same as area.
	*  Hence: Targets = 1/2 * L * (1/3 * L) = L^2/6 (round up). 
	*  See AreasOfEffect experiment images for confirmation.
	*/
	private int getMaxVictimsInCone (int length) {
		return length * length / 6 + 1;
	}

	/**
	* Compute number of victims hit by breath weapon.
	*
	*  After melee is engaged, we assume party is spread out,
	*  so at most we can hit half of enemy party (rounded up).
	*  Do not use this method for specials prior to melee.
	*/
	private int getBreathVictims (Party enemy, int maxVictimsByArea) {
		return Math.min((enemy.size() + 1) / 2, maxVictimsByArea);
	}

	/**
	* Apply energy damage to multiple enemy party numbers.
	*/
	private void castEnergyArea (Party enemy, int number, int damage, 
			EnergyType energy, SavingThrows.Type saveType) {
		List<Monster> targets = enemy.randomGroup(number);
		for (Monster m: targets) {
			castEnergy(m, damage, energy, saveType);
		}
	}

	/**
	* Apply energy damage to one enemy monster.
	*/
	private void castEnergy (Monster target, int damage, 
			EnergyType energy, SavingThrows.Type saveType) 
	{
		target.saveVsEnergy(energy, damage, saveType, getHD());			
	}

	/**
	* Is this monster immune to this energy type?
	*/
	private boolean isImmuneToEnergy (EnergyType energy) {
		switch (energy) {
			case Fire: return hasSpecial(SpecialType.FireImmunity);
			case Cold: return hasSpecial(SpecialType.ColdImmunity);
			case Acid: return hasSpecial(SpecialType.AcidImmunity);
			case Volt: return hasSpecial(SpecialType.VoltImmunity);
			default: return false;
		}
	} 

	/**
	* Force a condition on multiple enemy party numbers.
	*/
	private void castConditionArea (Party enemy, int number, SpecialType condition) {
		List<Monster> targets = enemy.randomGroup(number);
		for (Monster m: targets) {
			castCondition(m, condition);
		}
	}

	/**
	* Force a condition on one enemy monster (no save modifier).
	*/
	private void castCondition (Monster target, SpecialType condition) {
		castCondition(target, condition, 0);
	}

	/**
	* Force a condition on one enemy monster.
	*/
	private void castCondition (Monster target, SpecialType condition, int saveMod) {
		target.saveVsCondition(condition, saveMod);
	}

	/**
	* Cast a charm on one enemy monster.
	*/
	private void castCharm (Monster target, int saveMod) {
		if (target.hasFeat(Feat.IronWill)) saveMod += 4;
		if (!target.rollSave(SavingThrows.Type.Spells, saveMod)) {
			target.addCondition(SpecialType.Charm);
		}
	}

	/**
	* Apply mind blast to enemy party numbers.
	*  
	*  Saving throws simplified; assume at long range.
	*  Any result other than Confusion/Enrage takes
	*    condition MindBlast (thus, hors de combat). 
	*/
	private void mindBlastArea (Party enemy, int number) {
		List<Monster> targets = enemy.randomGroup(number);
		for (Monster m: targets) {
			int intel = m.getAbilityScore(Ability.Int);
			if (intel > 0) {
				if (Dice.roll(20) + intel < 20) {
					if (intel < 5) {
						m.addCondition(SpecialType.Death);
					}
					else if (intel < 13) {
						m.addCondition(SpecialType.MindBlast);
					}
					else if (intel < 15) {
						m.addCondition(SpecialType.Confusion);
					}
					else if (intel < 17) {
						m.addCondition(SpecialType.Berserking);
					}
					else {
						m.addCondition(SpecialType.MindBlast);
					}
				}
			}  
		}
	}

	/**
	* Cast slow on one of the enemy.
	*/
	private void checkSlowing (Party enemy) {
		if (hasSpecial(SpecialType.Slowing)) {
			Monster target = enemy.random();
			target.saveVsCondition(SpecialType.Slowing, getHD());
		}
	}

	/**
	* Summon any minions to our party.
	*/
	public void summonMinions (Party party) {
		SpecialType summonsType = getSummonsAbility();
		if (summonsType != null) {
			switch (summonsType) {

				case SummonVermin: 
					party.addMonsters("Wolf", new Dice(3, 6).roll()); 
					break;

				case SummonTrees:
					party.addMonsters("Tree, Animated", 2);
					break;

				default:
					System.err.println("Summons type not found: " 
						+ summonsType);
			}  
		}
	}

	/**
	* Check if we are confused on our turn to attack.
	*/
	private boolean checkConfusion (Party party) {
		if (hasCondition(SpecialType.Confusion)) {
			int reaction = new Dice(2, 6).roll();
			if (reaction <= 5)   // act normally
				return false; 
			else if (reaction <= 8) // stand motionless
				return true;
			else {       // make one attack on own party
				Monster target = party.getRandomMeleeTarget();
				if (target != null && target != this) {
					singleAttack(getAttack(), target, true);
				}
				return true;
			}
		}
		return false;
	}

	/**
	* Check if we are entangled in webs on our turn.
	*/
	private boolean checkWebbing () {
		if (hasCondition(SpecialType.Webs)) {

			// Make check vs. strength bonus to break out
			int strBonus = getAbilityBonus(Ability.Str);
			boolean breakOut = Dice.roll(6) <= strBonus;
			if (breakOut) removeCondition(SpecialType.Webs);
			return true;
		}
		return false;
	}

	/**
	* Attach ourselves to some creature (e.g., blood drain).
	*/
	private void setHost (Monster host) {
		this.host = host; 
	}

	/**
	* Try to cast an attack spell at enemy group.
	* @param enemies possible targets of attack spell.
	* @param area true if we want area-effect spell.
	* @return true if we cast a spell.
	*/
	private boolean tryCastAttackSpell (Party enemies, boolean area) {
		if (hasSpells() 
			&& !hasCondition(SpecialType.AntimagicSphere)) 
		{
			Spell spell = getBestAttackSpell(enemies, area);
			if (spell != null) {
				spell.cast(getLevel(), enemies);
				wipeSpellFromMemory(spell);
				return true;
			} 
		}
		return false;	
	}	

	/**
	* Check casting an attack spell before melee.
	* Look for area-effects first; then if none, try targeted.
	* @return true if we cast a spell.
	*/
	private boolean checkCastSpellPreMelee (Party enemies) {
		if (hasSpells()) {
			if (tryCastAttackSpell(enemies, true))
				return true;
			else if (tryCastAttackSpell(enemies, false))
				return true;
		}
		return false;
	}

	/**
	* Check casting an attack spell in melee.
	* Only targeted spells are allowed (not area-effects).
	* @return true if we cast a spell.
	*/
	private boolean checkCastSpellInMelee (Party enemies) {
		return tryCastAttackSpell(enemies, false);
	}

	/**
	* Count current heads for multiheaded types.
	* Set one attack per full hit die.
	*/
	private void headCount () {
		if (hasSpecial(SpecialType.ManyHeads)) {
			int hitDieSides = getHitDice().getSides();
			int newRate = (getHP() - 1) / hitDieSides + 1;
			getAttack().setRate(newRate);
		}
	}

	/**
	* Lose a level (e.g., energy drain).
	*/
	protected void loseLevel () {
		int HD = getHitDiceNum();
		if (HD <= 1) {
			maxHitPoints = 0;
			hitDice.setNum(0);
		}
		else {
			maxHitPoints = maxHitPoints * (HD - 1)/HD;
			hitDice.setNum(HD - 1);
		}
		boundHitPoints();
	}

	/**
	* Roll a saving throw vs. spells with no modifier.
	*/
	private boolean rollSaveSpells () {
		return rollSave(SavingThrows.Type.Spells, 0);
	}

	/**
	* Roll a saving throw with no modifier.
	*/
	private boolean rollSave (SavingThrows.Type type) {
		return rollSave(type, 0);
	}

	/**
	* Roll a saving throw with modifier.
	*/
	protected boolean rollSave (SavingThrows.Type type, int modifier) {
		modifier += getFixedSaveModifiers(type);
		return SavingThrows.getInstance().rollSave(
			type, "Fighter", getHD(), modifier);
	}

	/**
	* Add up fixed save modifiers for this monster.
	*/
	protected int getFixedSaveModifiers (SavingThrows.Type type) {
		int modifier = 0;

		// Save bonus special ability
		if (hasSpecial(SpecialType.SaveBonus)) {
			modifier += getSpecialParam(SpecialType.SaveBonus);
		}

		// Displacement effect
		if (hasSpecial(SpecialType.Displacement)) {
			modifier += 2;  
		}

		// Great Fortitude feat vs. death saves 
		if (type == SavingThrows.Type.Death && 
				hasFeat(Feat.GreatFortitude)) {
			modifier += 4;  
		}

		// Berserker bonus (Feat)
		if (hasFeat(Feat.Berserking)) {
			modifier += 4;
		}

		// Protection from evil
		if (hasSpecial(SpecialType.ProtectionFromEvil)) {
			modifier += 1;
		}

		return modifier;
	}

	/**
	* Add to kill tally.
	*/
	public void addToKillTally (int num) {
		killTally += num; 
	}

	/**
	* Get the XP award value for defeating this monster.
	*/
	public int getXPAward () {
		return XPAwardTable.getInstance().getXPAward(this);
	}

	/**
	* Generate random treasure value by treasure type, 
	* for one monster, scaled by nominal number appearing.
	* (Recommended for wilderness encounters only.)
	*/
	public int getTreasureValue () {
		int avgNum = numberAppearing.avgRoll();
		return MonsterTreasureTable.getInstance()
			.randomValueByCode(treasureType) / avgNum;
	}

	/**
	* Return if this monster has an undefined EHD.
	*/
	public boolean hasUndefinedEHD () {
		return getEHD() == UNDEFINED_EHD;
	}

	/**
	* Get current movement rate.
	*/
	public int getMoveInches () {
		return hasCondition(SpecialType.Slowing) ? moveInches/2 : moveInches;
	}

	/**
	* Initialize breath weapon charges.
	*/
	public void initBreathCharges () {
		if (getBreathWeapon() != null) {
			breathCharges = 3;
		}
	}

	/**
	* Identify this object as a string.
	*/
	public String toString() {
		return getRace() 
			+ ": AC "   + getAC() 
			+ ", MV " + getMV() 
			+ ", HD "   + getHD() 
			+ (getHD() == getEHD() ? "" : "; EHD " + EHDString())
			+ ", hp " + getHP() 
			+ ", Atk " + getAttack().getRate() 
			+ ", Dam " + getAttack().getDamage()
			+ (specialList.isEmpty() ? "" : "; SA " + specialString())
			+ ".";
	}

	/**
	* Identify special abilities as a string.
	*/
	private String specialString() {
		String s = specialList.toString();
		return s.substring(1, s.length()-1);
	}

	/**
	* Identify EHD as a string.
	*/
	private String EHDString() {
		int EHD = getEHD();
		return (EHD == UNDEFINED_EHD ? "?" : String.valueOf(EHD));
	}

	/**
	* Parse a floating-point string safely.
	*/
	private float parseFloat (String s) {
		float f;
		try {
			f = Float.parseFloat(s);							
		}	
		catch (Exception e) {
			f = 0.0f;
		}
		return f;
	}

	/**
	* Parse a dragon's age value from the name.
	* @return the age bracket (1-6, or 0 if not given)
	*/
	private int parseDragonAge () {
		assert(hasSpecial(SpecialType.Dragon));
		final String ageDesc[] = {"Very Young", "Young", 
			"Sub-Adult", "Adult", "Old", "Very Old"};
		for (int age = 1; age <= ageDesc.length; age++) {
			if (race.endsWith(", " + ageDesc[age - 1]))
				return age;
		}
		return 0;
	}

	/**
	* Roll a dragon's age category, if not set already.
	*/
	private void rollDragonAge () {
		assert(hasSpecial(SpecialType.Dragon));
		if (dragonAge < 1) 
			dragonAge = Dice.roll(BASE_HIT_DIE);
	}

	/**
	* Get a dragon's age category.
	*/
	private int getDragonAge () {
		assert(hasSpecial(SpecialType.Dragon));
		return dragonAge;
	}

	/**
	* Ability score defaults for a monster. 
	*/
	public int getAbilityScore (Ability ability) { 
		switch (ability) {
			case Str: case Con: return getHD() / 2 * 3 + 10;
			case Dex: case Wis: return 10;
			case Int: case Cha: return 8;
		}	
		System.err.println("Unknown ability score type.");
		return 0;
	}

	/**
	*  Get an ability score bonus/modifier.
	*/
	public int getAbilityBonus (Ability ability) {
		int score = getAbilityScore(ability);
		return Ability.getBonus(score);
	}

	/**
	* Does this creature count as a person? (c.f., charm, hold spells)
	*/
	public boolean isPerson () {
		return type == 'M'
			|| (type == 'H' && getHD() <= 1);
	}

	/**
	*  Does this monster know any spells?
	*/
	public boolean hasSpells () {
		return getSpellMemory() != null;
	}

	/**
	*  Does this monster have any castable spells?
	*/
	private boolean hasCastableSpells () {
		SpellMemory memory = getSpellMemory();
		return memory != null && memory.hasCastableSpells();
	}

	/**
	*  Get the best castable attack spell.
	*  Search for viable spell that affects the most targets.
	*  @param area true if area-effect spell desired.
	*  @return the best spell in memory.
	*/
	public Spell getBestAttackSpell (Party enemies, boolean areaEffect) {
		assert(hasSpells());
		Spell bestSpell = null;
		Monster sampleFoe = enemies.random();
		for (Spell spell: getSpellMemory()) {

			// Is this spell viable in our current situation?
			if (spell.isCastable()
				&& spell.isAreaEffect() == areaEffect
				&& sampleFoe.getHD() <= spell.getMaxTargetHD()
				&& (sampleFoe.isPerson() || !spell.isPersonEffectOnly()))
			{
				if (bestSpell == null) {
					bestSpell = spell;
				}			
				else {

					// Compare which spell affects more targets
					int bestSpellHits = Math.min(
						enemies.size(), bestSpell.getMaxTargetNum());
					int thisSpellHits = Math.min(
						enemies.size(), spell.getMaxTargetNum());
					if (bestSpellHits < thisSpellHits) {
						bestSpell = spell;
					}

					// Break ties by spell level
					else if (bestSpellHits == thisSpellHits) {
						if (bestSpell.getLevel() < spell.getLevel())
							bestSpell = spell;					
					}
				}			
			}
		}
		return bestSpell;		
	}

	/**
	*  Remove a spell from memory.
	*  Scan all classes to find a copy to remove.
	*/
	public boolean wipeSpellFromMemory (Spell s) {
		SpellMemory memory = getSpellMemory();
		assert(memory != null);
		if (memory.remove(s))
			return true;
		else {
			System.err.println("Request to wipe a spell not in memory.");
			return false;
		}
	}		

	/**
	* Are we a beholder casting eye-functions in melee?
	*/
	private boolean checkManyEyesSalvo (Party enemy) {
		if (hasSpecial(SpecialType.ManyEyeFunctions)
			&& !enemy.allHaveCondition(SpecialType.AntimagicSphere))
		{		
			manyEyesSalvo(enemy);		
			return true;
		}	
		return false;
	}

	/**
	* Beholder many-eyes attacks.
	*
	* References available castable spells.
	*/
	private void manyEyesSalvo (Party enemy) {
		final String eyeFunctionNames[] = {
			"Charm Person", "Charm Monster", "Sleep", "Disintegrate", "Fear"};

		// Construct list of castable spell-functions.
		ArrayList<Spell> eyeFuncs = new ArrayList<Spell>(); 
		for (String name: eyeFunctionNames) {
			Spell spell = SpellsIndex.getInstance().findByName(name);
			if (spell != null) {
				assert(spell.isCastable());
				eyeFuncs.add(spell);
			}
		}

		// Cast random 1-4 of the spell-effects.
		int numZaps = Dice.roll(4);
		numZaps = Math.min(numZaps, eyeFuncs.size());
		Collections.shuffle(eyeFuncs);
		for (int i = 0; i < numZaps; i++) {
			eyeFuncs.get(i).cast(getHD(), enemy);
		}
		
		// Cast the central antimagic ray at a spellcaster.
		List<Monster> enemyShuffle = enemy.randomGroup(enemy.size());
		for (Monster m: enemyShuffle) {
			if (m.hasSpells() && !m.hasCondition(SpecialType.AntimagicSphere)) {
				m.saveVsCondition(SpecialType.AntimagicSphere, getHD());			
			}
		}		
	}

	/**
	* Does this creature have undead-style immunities?
	*/
	private boolean hasUndeadImmunity () {
		return hasSpecial(SpecialType.Undead)
			|| hasSpecial(SpecialType.UndeadImmunity);
	}

	/**
	* Get this monster's spell memory.
	*/
	public SpellMemory getSpellMemory () { 
		return spellMemory; 
	}

	/**
	* Memorize spells for a monster that has them.
	*/
	private void memorizeSpells () {
		assert(hasSpecial(SpecialType.Spells));
		spellMemory = new SpellMemory();

		// Gold Dragon
		// - Gain one level of spell per age category.
		// - As per AD&D idiom, gain two such spells per level.
		if (race.startsWith("Dragon, Gold")) {
			int age = getDragonAge();
			for (int level = 1; level <= age; level++) {
				for (int num = 0; num < 2; num++)
					spellMemory.addRandom(level);
			}
		}

		// Titan
		// - Gain two spells per level available.
		// - Inspired by AD&D rule, roll for levels available.
		// - Ignore cleric spells (post-melee recovery)
		if (race.equals("Titan")) {
			int maxLevel = Dice.roll(3) + 3;
			for (int level = 1; level <= maxLevel; level++) {
				for (int num = 0; num < 2; num++)
					spellMemory.addRandom(level);
			}
		}

		// Triton
		// - Handle different HD classes.
		// - Estimate 2 spells per level available.
		if (race.equals("Triton")) {
			int maxLevel = getHD() - 3;
			for (int level = 1; level <= maxLevel; level++) {
				for (int num = 0; num < 2; num++)
					spellMemory.addRandom(level);
			}
		}
			
		// Lich
		// - Assume the "typical" 18th level of wizardry.
		// - Note levels for spell effects just use the hit dice.
		if (race.equals("Lich")) {
			final int wizLevel = 18;
			spellMemory.addSpellsForWizard(wizLevel);
		}

		// Ogre Mage
		// - Spells are fixed per book description.
		if (race.equals("Ogre Mage")) {
			spellMemory.addByName("Sleep");
			spellMemory.addByName("Charm Person");
			spellMemory.addByName("Ice Storm");
		}
		
		// Lammasu
		// - Spells are clerical, which we ignore here (post-melee recovery)
		if (race.equals("Lammasu")) {
			return;		
		}		

		// Check for unrecognized monster with spells.
		if (spellMemory.isBlank()) {
			System.err.println("Unknown monster with spells: " + race);
		}
	}

	/**
	* Eat one piece of target equipment, if available.
	*/
	private void eatEquipment (Monster target) {
		if (canEatEquipment(target.getShield()))
			target.saveVsEquipmentLoss(target.getShield());
		else if (canEatEquipment(target.getArmor()))
			target.saveVsEquipmentLoss(target.getArmor());
		else if (canEatEquipment(target.getWeapon()))
			target.saveVsEquipmentLoss(target.getWeapon());
	}

	/**
	* Can this monster eat a given type of equipment?
	*/
	private boolean canEatEquipment (Equipment equip) {
		if (equip == null)
			return false;
		else if (isFlamingWeapon(equip))
			return false;
		else if (hasSpecial(SpecialType.MetalEating)
				&& equip.isMetallic())
			return true;
		else if (hasSpecial(SpecialType.WoodEating)
				&& equip.getMaterial() == Equipment.Material.Wood)
			return true;
		else
			return false;	
	}

	/**
	* Can this monster eat some kind of equipment?
	*/
	private boolean canEatEquipment () {
		return hasSpecial(SpecialType.WoodEating)
			|| hasSpecial(SpecialType.MetalEating);	
	}

	/**
	* A piece of this creature's equipment must save or be lost.
	*/
	private void saveVsEquipmentLoss (Equipment equip) {
		if (!equip.rollSave()) {
			loseEquipment(equip);
		}	
	}

	/**
	* Is this equipment a flaming weapon?
	*/
	private boolean isFlamingWeapon (Equipment equip) {
		return equip != null
			&& equip instanceof Weapon
			&& ((Weapon) equip).getEnergy() != null
			&& ((Weapon) equip).getEnergy() == EnergyType.Fire;
	}

	/**
	* Check if we need to draw a new weapon mid-fight.
	* @return true if we drew a new weapon
	*/
	private boolean checkDrawNewWeapon (Party enemies) {
		if (getAttack() == null) {
			drawBestWeapon(enemies.random());
			return true;
		}
		return false;
	}

	/**
	* Does this monster have any way of harming that enemy?
	*/
	public boolean isViableAgainst (Monster enemy) {
		return canAttack(enemy) 
			|| (hasCastableSpells() 
				&& !enemy.hasSpecial(SpecialType.MagicImmunity));
	}

	/**
	* Is this monster currently in phase and subject to attack?
	* Per Dragon #131, Sage Advice, this comes down to initiative,
	* i.e., basically just a coin-flip.
	*/
	private boolean isInPhase () {
		boolean outOfPhase = hasSpecial(SpecialType.Phasing) && Dice.coinFlip();
		return !outOfPhase;
	}

	/**
	* Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		Monster m = new Monster("Orc", 6, 9, 
			new Dice(1, 6), new Attack(1, 1));
		System.out.println(m);
	}
}
