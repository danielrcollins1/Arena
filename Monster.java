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

	/** Sides on standard hit dice. */
	static final int BASE_HIT_DIE = 6;

	/** Maximum enemies who can melee us at once. */
	static final int MAX_MELEERS = 6;

	/** Sentinel value for undefined EHD. */
	public static final int UNDEFINED_EHD = -1;

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
	float hitDiceAsFloat;
	int equivalentHitDice;
	int hitPoints;
	int maxHitPoints;
	int breathCharges;
	int killTally;
	int timesMeleed;
	Monster host;
	List<SpecialAbility> specialList;
	List<SpecialAbility> conditionList;
	SpellMemory spellMemory;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	* Constructor (very basic).
	*/
	public Monster (String race, int AC, int MV, int hitDice, int damageDice) {
		this(race, AC, MV, 
			new Dice(hitDice, BASE_HIT_DIE), 
			new Attack(hitDice, damageDice));
	}

	/**
	* Constructor (basic).
	*/
	public Monster (String race, int AC, int MV, Dice hitDice, Attack attack) {
		this.race = race;
		this.armorClass = AC;
		this.moveInches = MV;
		this.hitDice = hitDice;
		this.attack = attack;
		this.alignment = Alignment.Neutral;
		equivalentHitDice = hitDice.getNum();
		specialList = new ArrayList<SpecialAbility>(); 
		conditionList = new ArrayList<SpecialAbility>(); 
		spellMemory = null;
		rollHitPoints();
	}

	/**
	* Constructor (from String array).
	* @param s String array.
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
		specialList = parseSpecialAbilityList(s[9]);
		type = s[10].charAt(0);
		alignment = Alignment.getFromChar(s[11].charAt(0));
		hitDiceAsFloat = parseFloat(s[12]);
		equivalentHitDice = parseEHD(s[13]);
		environment = s[14].charAt(0);
		sourceBook = s[15];

		// Secondary fields
		conditionList = new ArrayList<SpecialAbility>(); 
		spellMemory = null;
		addImpliedSpecials();
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
		hitDiceAsFloat = src.hitDiceAsFloat;
		killTally = src.killTally;
		host = src.host;
		specialList = new ArrayList<SpecialAbility>(src.specialList); 
		conditionList = new ArrayList<SpecialAbility>(src.conditionList); 
		maxHitPoints = src.maxHitPoints;
		hitPoints = src.hitPoints;
		breathCharges = src.breathCharges;
		spellMemory = src.spellMemory == null ? null 
			: new SpellMemory(src.spellMemory);
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
	public float getHitDiceAsFloat () { return hitDiceAsFloat; }
	public int getKillTally () { return killTally; }
	public int getTimesMeleed () { return timesMeleed; }
	public Monster getHost () { return host; }

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

	// Null methods for Character inheritance
	public Armor getArmor () { return null; }
	public Armor getShield () { return null; }
	public Weapon getWeapon () { return null; }
	public void setArmor (Armor a) {}
	public void drawBestWeapon (Monster m) {}
	public void sheatheWeapon () {}
	public void boostMagicItemsOneLevel () {}
	public void takeAbilityDamage (Ability a, int n) {} 
	public void zeroAbilityDamage () {} 
	public boolean hasNullAbilityScore () { return false; }
	public boolean hasFeat (Feat feat) { return false; }
	public List<Spell> getSpellList () { return null; }
	public void addXP (int xp) {}
	public int getSweepRate() { return 0; }

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
		System.err.println("Error: Could not parse hit dice descriptor: " + s);
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
	private List<SpecialAbility> parseSpecialAbilityList (String s) {
		List<SpecialAbility> list = new ArrayList<SpecialAbility>(); 
		if (s.length() > 1) {
			String parts[] = s.split(", ");
			for (String part: parts) {
				SpecialAbility ability = 
					SpecialAbility.createFromString(part);
				if (ability != null) {
					list.add(ability);
				}
			}     
		}
		return list;
	}

	/**
	* Add some special qualifiers based on name or type.
	*/
	private void addImpliedSpecials() {
		if (getRace().startsWith("Dragon"))
			specialList.add(0, new SpecialAbility(SpecialType.Dragon));
		if (getRace().startsWith("Golem"))
			specialList.add(0, new SpecialAbility(SpecialType.Golem));
		if (getType() == 'U')
			specialList.add(0, new SpecialAbility(SpecialType.Undead));
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
		if (hasSpecial(SpecialType.NPC)) {
			Character c = Character.evilNPCFromTitle(race);
			c.race = race; // Reset monster race/title for kill tally
			for (SpecialAbility s: specialList) {
				c.specialList.add(s);
			}
			return c;
		}
		else {
			Monster m = new Monster(this);
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
		else if (hasSpecial(SpecialType.Multiheads)) {
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
	* Kill this monster (reduce to 0 hp).
	*/
	private void instaKill () {
		hitPoints = 0;
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
	public void boundHitPoints () {
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
		checkRegeneration();
		checkConstriction();
		checkSlowing(enemies);
		if (checkGrabbing()) return true;
		if (checkBloodDrain()) return true;
		if (checkBreathWeapon(enemies)) return true;
		if (checkConfused(friends)) return true;
		if (checkCastSpellInMelee(enemies)) return true;
		if (checkEyeTyranny(enemies)) return true;
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
			if (hasFeat(Feat.RapidStrike) && Dice.roll(6) <= 3) rate++;
			return rate;
		}
	}

	/**
	* Make one attack on another creature.
	*/
	public void singleAttack (Attack attack, Monster target, boolean last) {
		if (canAttack(target)) {
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
	* Can we feasibly attack this target?
	*/
	public boolean canAttack (Monster target) {
		boolean canHit = true;

		// Check silver to hit
		if (target.hasSpecial(SpecialType.SilverToHit)) {
			boolean hasSilverWeapon = (getWeapon() != null 
				&& getWeapon().getMaterial() == Weapon.Material.Silver);
			boolean atkSilverToHit = hasSpecial(SpecialType.SilverToHit);
			if (!hasSilverWeapon && !atkSilverToHit && getMagicHitLevel() <= 0)
				canHit = false;			
		}

		// Check magic to hit
		if (target.hasSpecial(SpecialType.MagicToHit)) {
			SpecialAbility magicSpecial = target.findSpecial(SpecialType.MagicToHit);
			if (getMagicHitLevel() < magicSpecial.getParam())
				canHit = false;
		}

		// Check total weapon immunity
		if (target.hasSpecial(SpecialType.ChopImmunity)) {
			canHit = false;
		}

		// Check phasing (3-in-6 to be out of phase)
		if (target.hasSpecial(SpecialType.Phasing) 
				&& new Dice(6).roll() <= 3) {
			canHit = false;
		}

		return canHit;
	}

	/**
	* Find what level of magic-to-hit we can strike.
	*/
	public int getMagicHitLevel () {

		// Check if we are also magic-to-hit
		SpecialAbility special = findSpecial(SpecialType.MagicToHit);
		int magicHitLevel = (special == null ? 0 : special.getParam());

		// Check if we have high hit dice
		int fractionHD = getHD() / 4;
		return Math.max(magicHitLevel, fractionHD);
	}

	/**
	* Return hit modifier against a target.
	* @return Modifier to hit.
	*/
	public int hitModifier (Monster target) {
		int modifier = 0;

		// General hit bonus
		if (hasSpecial(SpecialType.HitBonus)) {
			modifier += findSpecial(SpecialType.HitBonus).getParam();
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
		
		// Blindness
		if (hasCondition(SpecialType.Blindness)) {
			modifier -= 4;
		}

		return modifier;
	}

	/**
	* Check for damage reduction on target.
	* @return Adjusted damage.
	*/
	public int checkDamageReduction (Monster target, int damage) {

		// Damage reduction ability
		if (target.hasSpecial(SpecialType.DamageReduction)) {
			damage /= 2;
		}

		// Corroding oozes delay damage until bare flesh
		if (hasSpecial(SpecialType.Corrosion)
				&& target.getArmor() != null){
			damage = 0;
		}

		return damage;
	}

	/**
	* Check for special ability triggers when we hit.
	*/
	public void checkSpecialOnHit (Monster target, int totalRoll, boolean isLastAttack) {

		// Special abilities of this attacking monster
		for (SpecialAbility s: specialList) {
			switch (s.getType()) {

				case Poison:
					if (isLastAttack) {
						castCondition(target, SpecialType.Poison, -s.getParam());
					}
					break;

				case Paralysis:
					castCondition(target, SpecialType.Paralysis);
					break;

				case Petrification:
					castCondition(target, SpecialType.Petrification);
					break;

				case EnergyDrain:
					boolean saved = SavingThrows.ENERGY_DRAIN_SAVE
						&& target.rollSave(SavingThrows.Type.Death);
					if (!saved) {      
						for (int j = 0; j < s.getParam(); j++) {
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
					if (!isLastAttack && totalRoll > 24) {
						target.addCondition(SpecialType.Swallowing);
					}
					break;

				case StrengthDrain:
					target.takeAbilityDamage(Ability.Str, 1);
					break;

				case CharmTouch:
					castCharm(target, -s.getParam());
					break;

				case Corrosion:
					corrodeArmor(target);
					break;

				case Grabbing:
					setHost(target);
					break;

				case Constriction:
					setHost(target);
					break;

				case BloodDrain: 
					setHost(target);
					break;
			}
		}

		// Special abilities of the target monster
		if (target.hasSpecial(SpecialType.SporeCloud)) {
			if (Dice.roll(100) <= 50) {
				target.castCondition(this, SpecialType.SporeCloud);
			}  
		}
	}

	/**
	* Make a special attack on an enemy party.
	* Ranged/outside of melee specials should go here.
	* Uses all abilities possessed.
	*/
	void makeSpecialAttack (Party enemy) {
		Monster target;
		Attack attack;
		int modifier;

		// Check for offensive spell-casting
		if (checkCastSpellPreMelee(enemy)) {
			return;
		}

		// Check monster special abilities list
		for (SpecialAbility s: specialList) {
			switch (s.getType()) {

				case RockHurling: 
					target = enemy.random();
					attack = new Attack("Rock", 1, getHD(), new Dice(2, 6));
					singleAttack(attack, target, false);
					break;

				case TailSpikes:
					attack = new Attack("Tail Spike", 1, getHD(), new Dice(1, 6));
					for (int i = 0; i < 6; i++) {
						target = enemy.random();
						singleAttack(attack, target, false);
					}
					break;

				case PetrifyingGaze:
					for (Monster targetPetrify: enemy) {
						castCondition(targetPetrify, SpecialType.Petrification);
					}
					break;

				case Fear:
					Dice moraleDice = new Dice(2, 6); 
					for (Monster targetFear: enemy) {
						if (moraleDice.roll() + targetFear.getHD() < 9) {
							targetFear.addCondition(SpecialType.Fear);
						}
					}
					break;

				case WallOfFire:
					Dice fireDamage = new Dice(1, 6); 
					for (Monster targetFire: enemy) {
						castEnergy(targetFire, fireDamage.roll(), 
							EnergyType.Fire, SavingThrows.Type.Spells);
					}
					break;

				case ConeOfCold:
					int damage = new Dice(8, 6).roll();
					int maxVictims = getMaxVictimsInCone(6);
					castEnergyArea(enemy, maxVictims, damage, 
						EnergyType.Cold, SavingThrows.Type.Spells);
					break;

				case AcidSpitting:
					target = enemy.random();
					attack = new Attack("Acid Spit", 1, getHD(), new Dice(2, 6));
					singleAttack(attack, target, false);
					break;

				case Whirlwind:
					int diameter = s.getParam();
					int numVictims = diameter * diameter;
					List<Monster> victims = enemy.randomGroup(numVictims);
					for (Monster m: victims) {
						if (m.getHD() < 2) {
							m.instaKill();
						}     
					}
					break;

				case Confusion:
					target = enemy.random();
					modifier = -s.getParam();
					if (target.hasFeat(Feat.IronWill)) modifier += 4;
					castCondition(target, SpecialType.Confusion, modifier);
					break;

				case MindBlast: 
					maxVictims = getMaxVictimsInCone(6);
					mindBlastArea(enemy, maxVictims);
					break;    

				case SappingStrands:
					attack = new Attack("Sapping Strand", 1, getHD(), new Dice(0, 6));
					for (int i = 0; i < 6; i++) {
						target = enemy.random();
						singleAttack(attack, target, false);
					}
					break;

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
					break;

				case Charm:
					castCharm(enemy.random(), -s.getParam());
					break;
					
				case EyeTyranny:
					eyeTyrantSalvo(enemy);
					break;
			}     
		}
	}

	/**
	* Find if we have a given type of special ability.
	*/
	public SpecialAbility findSpecial (SpecialType type) {
		for (SpecialAbility s: specialList) {
			if (s.getType() == type) {
				return s;
			}
		}
		return null;
	}

	/**
	* Check if this monster has a given type of special ability.
	*/
	public boolean hasSpecial (SpecialType type) {
		return findSpecial(type) != null;
	}

	/**
	* Add a condition suffered from a special ability.
	*/
	private void addCondition (SpecialType type) {
		conditionList.add(new SpecialAbility(type));
	}

	/**
	* Take a given condition unless we resist.
	*/
	public void saveVsCondition (SpecialType condition, int casterLevel, int saveMod) {
		if (!rollSave(condition.getSaveType(), saveMod)) {
			addCondition(condition);
		}
	}

	/**
	* Take a given condition unless we resist (no modifier).
	*/
	public void saveVsCondition (SpecialType condition, int casterLevel) {
		saveVsCondition(condition, casterLevel, 0);
	}

	/**
	* Find if we suffer from a given condition.
	*/
	public SpecialAbility findCondition (SpecialType type) {
		for (SpecialAbility s: conditionList) {
			if (s.getType() == type) {
				return s;
			}
		}
		return null;
	}

	/**
	* Check if we suffer from a given condition.
	*/
	public boolean hasCondition (SpecialType type) {
		return findCondition(type) != null; 
	}

	/**
	* Check if we suffer from a disabling condition.
	*/
	public boolean hasDisablingCondition () {
		for (SpecialAbility s: conditionList) {
			if (s.getType().isDisabling()) {
				return true;
			}
		}
		return false;
	}

	/**
	* Die unless we resist.
	*/
	public void saveOrDie (int casterLevel) {
		if (!rollSave(SavingThrows.Type.Death)) {
			instaKill();
		}
	}

	/**
	* Take energy damage; save for half. 
	*/
	public void saveVsEnergy (EnergyType energy, int damage, 
		SavingThrows.Type saveType, int casterLevel) 
	{
		if (isImmuneToEnergy(energy)) return;
		boolean saved = rollSave(saveType);
		takeDamage(saved ? damage / 2 : damage);
	}

	/**
	* Get our breath weapon (if any).
	*/
	public SpecialAbility getBreathWeapon () {
		for (SpecialAbility s: specialList) {
			if (s.getType().isBreathWeapon()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Get our gaze weapon (if any).
	*/
	public SpecialAbility getGazeWeapon () {
		for (SpecialAbility s: specialList) {
			if (s.getType().isGazeWeapon()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Get our summons ability (if any).
	*/
	public SpecialAbility getSummonsAbility () {
		for (SpecialAbility s: specialList) {
			if (s.getType().isSummonsAbility()) {
				return s;
			}
		}
		return null;
	}

	/**
	* Regenerate hit points if appropriate.
	*/
	public void checkRegeneration () {
		SpecialAbility special = findSpecial(SpecialType.Regeneration);
		if (special != null) {
			hitPoints += special.getParam();
			boundHitPoints();
		}
	}

	/**
	* Drain blood from host if appropriate.
	* @return true if we drained blood
	*/
	public boolean checkBloodDrain () {
		SpecialAbility special = findSpecial(SpecialType.BloodDrain);
		if (special != null && host != null) {
			int maxDamage = special.getParam();
			int drain = new Dice(1, maxDamage).roll();
			host.takeDamage(drain);
			if (host.horsDeCombat()) {
				host = null;
			}
			return true;
		}
		return false;
	}

	/**
	* Constrict host if appropriate.
	* @return Did we constrict?
	*/
	public boolean checkConstriction () {
		SpecialAbility special = findSpecial(SpecialType.Constriction);
		if (special != null && host != null) {
			int damage = new Dice(1, 6).roll();
			host.takeDamage(damage);
			if (host.horsDeCombat()) {
				host = null;   
			}
			return true;
		}
		return false;
	}

	/**
	* Grab host if appropriate.
	* @return Did we grab?
	*/
	public boolean checkGrabbing () {
		SpecialAbility special = findSpecial(SpecialType.Grabbing);
		if (special != null && host != null) {

			// Absorption: game over, man.
			if (hasSpecial(SpecialType.Absorption)) {
				host.instaKill();
				host = null;
				return true;
			}

			// Brain Consumption: comparable to 40% to kill/round.
			if (hasSpecial(SpecialType.BrainConsumption)) {
				if (Dice.roll(10) <= 4) {
					host.instaKill();
					host = null;
				}   
				return true;
			}

			// Corrosion: must dissolve armor before damage.
			if (hasSpecial(SpecialType.Corrosion)) {
				if (host.getArmor() != null) {
					corrodeArmor(host);    
					return true;    
				}   
			}

			// Automatic damage per attack form
			int damage = attack.getDamage().roll();
			host.takeDamage(damage);
			if (host.horsDeCombat()) {
				host = null;
			}
			return true;
		}
		return false;
	}

	/**
	* Check for a breath weapon attack.
	*  Estimates max number hit for given area.
	* @return Did we make a breath attack?
	*/
	public boolean checkBreathWeapon (Party enemy) {
		if (breathCharges > 0 && new Dice(2, 6).roll() >= 7) {
			SpecialAbility breath = getBreathWeapon();
			int param = breath.getParam();
			int damage, maxVictims, numVictims;
			switch (breath.getType()) {

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
							System.err.println("Error: Unknown acid breath.");
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
					System.err.println("Error: Breath weapon type not handled:"
						+ breath.getType());
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
						m.instaKill();     
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
	public void checkSlowing (Party enemy) {
		Monster target = enemy.random();
		if (!target.hasCondition(SpecialType.Slowing)
				&& !target.rollSave(SavingThrows.Type.Spells)) {
			target.addCondition(SpecialType.Slowing);
		}
	}

	/**
	* Summon any minions to our party.
	*/
	public void summonMinions (Party party) {
		SpecialAbility summons = getSummonsAbility();
		if (summons != null) {
			switch (summons.getType()) {

				case SummonVermin: 
					party.addMonsters("Wolf", new Dice(3, 6).roll()); 
					break;

				case SummonTrees:
					party.addMonsters("Tree, Animated", 2);
					break;

				default:
					System.err.println("Error: Summons type not found: " 
						+ summons.getType());
			}  
		}
	}

	/**
	* Check if we are confused on our turn to attack.
	*/
	public boolean checkConfused (Party party) {
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
	* Corrode target's armor (metal or leather).
	*/
	public void corrodeArmor (Monster target) {
		Armor armor = target.getArmor();
		if (armor != null) {
			if (armor.getMagicBonus() > 0) {
				armor.setMagicBonus(armor.getMagicBonus() - 1);
			}
			else {
				target.setArmor(null);
			}
		}
	}

	/**
	* Attach ourselves to some creature (e.g., blood drain).
	*/
	public void setHost (Monster host) {
		this.host = host; 
	}

	/**
	* Try to cast an attack spell at enemy group.
	* @param enemies possible targets of attack spell.
	* @param area true if we want area-effect spell.
	* @return true if we cast a spell.
	*/
	private boolean tryCastSpellAttack (Party enemies, boolean area) {
		if (hasSpells()) {
			Spell spell = getBestAttackSpell(area);
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
			if (tryCastSpellAttack(enemies, true))
				return true;
			else if (tryCastSpellAttack(enemies, false))
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
		return tryCastSpellAttack(enemies, false);
	}

	/**
	* Count current heads for multiheaded types.
	* Set one attack per full hit die.
	*/
	void headCount () {
		if (hasSpecial(SpecialType.Multiheads)) {
			int hitDieSides = getHitDice().getSides();
			int newRate = (getHP() - 1) / hitDieSides + 1;
			getAttack().setRate(newRate);
		}
	}

	/**
	* Lose a level (e.g., energy drain).
	*/
	public void loseLevel () {
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
	public boolean rollSaveSpells () {
		return rollSave(SavingThrows.Type.Spells, 0);
	}

	/**
	* Roll a saving throw with no modifier.
	*/
	public boolean rollSave (SavingThrows.Type type) {
		return rollSave(type, 0);
	}

	/**
	* Roll a saving throw with modifier.
	*/
	public boolean rollSave (SavingThrows.Type type, int modifier) {
		modifier += getFixedSaveModifiers(type);
		return SavingThrows.getInstance().rollSave(
			type, "Fighter", getHD(), modifier);
	}

	/**
	* Add up fixed save modifiers for this monster.
	*/
	public int getFixedSaveModifiers (SavingThrows.Type type) {
		int modifier = 0;

		// Save bonus special ability
		SpecialAbility saveBonus = findSpecial(SpecialType.SaveBonus);
		if (saveBonus != null) {
			modifier += saveBonus.getParam();  
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
	int getXPAward () {
		return XPAwardTable.getInstance().getXPAward(this);
	}

	/**
	* Generate random treasure value by treasure type, 
	* for one monster, scaled by nominal number appearing.
	* (Recommended for wilderness encounters only.)
	*/
	int getTreasureValue () {
		int avgNum = numberAppearing.avgRoll();
		return MonsterTreasureTable.getInstance()
			.randomValueByCode(treasureType) / avgNum;
	}

	/**
	* Return if this monster has an undefined EHD.
	*/
	boolean hasUndefinedEHD () {
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
	public String specialString() {
		String s = specialList.toString();
		return s.substring(1, s.length()-1);
	}

	/**
	* Identify EHD as a string.
	*/
	public String EHDString() {
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
	* Get a dragon's age category from the name.
	* If no descriptor match, return a random age.
	*/
	int getDragonAge () {
		String ageDesc[] = {"Very Young", "Young", 
			"Sub-Adult", "Adult", "Old", "Very Old"};
		for (int i = 0; i < ageDesc.length; i++) {
			if (race.endsWith(", " + ageDesc[i]))
				return i + 1;
		}
		return Dice.roll(BASE_HIT_DIE);
	}

	/**
	* Ability score defaults for a monster. 
	* This is redefined for the Character class.
	*/
	public int getAbilityScore (Ability ability) { 
		switch (ability) {
			case Str: case Con: return getHD() / 2 * 3 + 10;
			case Int: case Wis: case Cha: return 6;
			default: return 10;
		}	
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
		return spellMemory != null;
	}

	/**
	*  Get the best (highest-level) castable attack spell.
	*  @param area true if area-effect spell desired.
	*  @return the best spell in memory.
	*/
	public Spell getBestAttackSpell (boolean areaEffect) {
		return spellMemory == null ? null 
			: spellMemory.getBestAttackSpell(areaEffect);
	}

	/**
	*  Remove a spell from memory.
	*  Scan all classes to find a copy to remove.
	*/
	public boolean wipeSpellFromMemory (Spell s) {
		if (spellMemory != null && spellMemory.remove(s))
			return true;
		else {
			System.err.println("ERROR: Request to wipe a spell not in monster memory.");
			return false;
		}
	}		

	/**
	* Beholder eye-tyranny salvo.
	*
	* References available castable spells.
	*/
	private void eyeTyrantSalvo (Party enemy) {
		final String eyeEffectNames[] = {
			"Charm Person", "Charm Monster", "Sleep", "Disintegrate", "Fear"};

		// Construct list of available spell-effects.
		ArrayList<Spell> eyeEffects = new ArrayList<Spell>(); 
		for (String name: eyeEffectNames) {
			Spell spell = SpellsIndex.getInstance().findByName(name);
			if (spell != null) {
				assert(spell.isCastable());
				eyeEffects.add(spell);
			}
		}

		// Cast random 1-4 of the spell-effects.
		int numZaps = Dice.roll(4);
		assert(eyeEffects.size() >= 4);
		Collections.shuffle(eyeEffects);
		for (int i = 0; i < numZaps; i++) {
			eyeEffects.get(i).cast(getHD(), enemy);
		}
	}

	/**
	* Are we a beholder casting eye-effects in melee?
	*/
	private boolean checkEyeTyranny (Party enemy) {
		if (hasSpecial(SpecialType.EyeTyranny)) {
			eyeTyrantSalvo(enemy);		
			return true;
		}	
		return false;
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
