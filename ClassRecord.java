import java.util.*;

/******************************************************************************
*  Record of one class gained by a character (XP, level, hit points, etc.).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-22
******************************************************************************/

public class ClassRecord {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Hit die for level 0. */
	static final int LEVEL_ZERO_HIT_DIE = 6;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Character reference. */
	Character character;

	/** Class type for this class. */
	ClassType classType;

	/** Level in this class. */
	int level;

	/** Hit points earned for this class. */
	int hitPoints;

	/** Experience points earned towards this class. */
	int XP;

	/** Spells known for this class. */
	SpellMemory spellsKnown;

	/** Feats known for this class. */
	Set<Feat> featsKnown;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (by ClassInfo, level, random hit points).
	*/
	public ClassRecord (Character character, ClassType type, int level) {
		this.character = character;
		this.classType = type;
		this.level = level;
		this.XP = type.getXpReq(level);
		rollFullHitPoints();
		addAllFeats();
		addAllSpells();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public ClassType getClassType () { return classType; }
	public int getLevel () { return level; }
	public int getHitPoints () { return hitPoints; }
	public int getXP () { return XP; }

	/**
	*  Return the base attack bonus for this class/level.
	*/
	public int attackBonus () {
		return classType.getAttackBonus(level);
	}

	/**
	*  Add XP to this class.
	*/
	public void addXP (int awardXP) {
		XP += awardXP;
		if (XP >= classType.getXpReqNext(level)) {
			addLevel();
		}
	}

	/**
	*  Increment the level.
	*  Cap XP at one less than the next level.
	*/
	public void addLevel () {
		level++;	
		XP = Math.min(XP, classType.getXpReqNext(level) - 1);
		addNewHitPoints(level, false);
		if (isFeatLevel(level)) {
			addFeat();
		}
	}

	/**
	*  Decrement the level.
	*/
	public void loseLevel () {

		// At level 0, death/lose class
		if (level < 1) {
			hitPoints = 0;		
			level = -1;
			XP = Integer.MIN_VALUE;
		}

		// Else reduce hit points proportionally
		else {
			if (isFeatLevel(level))
				loseFeat();
			if (hasSpells())
				spellsKnown.loseSpellLevel(level);
			Dice diceInc = classType.getHitDiceInc(level);
			if (diceInc.getAdd() > 0)
				hitPoints -= diceInc.getAdd();
			else
				hitPoints = hitPoints * (level - 1)/level;
			level--;
			XP = classType.getXpMidpoint(level);
		}
	}

	/**
	*  Is this a level where we gain a feat?
	*/
	boolean isFeatLevel (int newLevel) {
		return Character.useFeats()
			&& classType.usesFeats()
			&& newLevel % 4 == 0;
	}

	/**
	*  Choose all feats for new character.
	*/
	void addAllFeats () {
		if (Character.useFeats() && classType.usesFeats()) {
			featsKnown = EnumSet.noneOf(Feat.class);
			for (int i = 1; i <= level; i++) {
				if (isFeatLevel(i)) {
					addFeat();
				}
			}
		}
	}

	/**
	*  Add a random feat to this character.
	*/
	void addFeat () {
		if (featsKnown.size() < Feat.number) {
			Feat newFeat;
			do {
				int rand = Dice.roll(Feat.number) - 1;
				newFeat = Feat.values()[rand];
			} while (featsKnown.contains(newFeat));
			featsKnown.add(newFeat);
		}	
	}

	/**
	*  Lose a random feat for this character.
	*/
	void loseFeat () {
		for (Feat f: featsKnown) {
			featsKnown.remove(f);
			break;
		}
	}

	/**
	*  Is this feat known by this class record?
	*/
	public boolean hasFeat (Feat f) {
		if (featsKnown == null) return false;
		return featsKnown.contains(f);
	}

	/**
	*  String representation of feats known.
	*/
	String featsString () {
		if (featsKnown == null)
			return null;
		else {
			String s = "";
			for (Feat feat: featsKnown) {
				if (s.length() > 0)
					s += ", ";
				s += Feat.formatName(feat);
			}
			return s;
		}
	}

	/**
	*  String representation of skills known.
	*/
	String skillsString () {
		if (!classType.usesSkills()) {
			return null;
		}
		else {
			int skillBonus = level + character.getAbilityBonus(Ability.Dex);
			int hearBonus = 1 + level/4 + character.getAbilityBonus(Ability.Int);
			int climbBonus = 1 + level/4 + character.getAbilityBonus(Ability.Dex);
			int backstabMult = level/4 + 2;
			return "Pick, disarm, filch, sneak, hide " + Dice.formatBonus(skillBonus) + "; "
				+ "hear " + Dice.formatBonus(hearBonus) + ", "
				+ "climb " + Dice.formatBonus(climbBonus) + ", "
				+ "backstab " + Dice.formatMultiplier(backstabMult);
		}
	}

	/**
	*  Add all expected wizard spells.
	*/
	void addAllSpells() {
		if (classType.usesSpells()) {
			spellsKnown = new SpellMemory();
			spellsKnown.addSpellsForWizard(level);
		}
	}

	/**
	*  Does this class have spells?
	*/
	public boolean hasSpells() {
		return spellsKnown != null;
	}

	/**
	*  Access our spell memory. 
	*/
	public SpellMemory getSpellMemory () {
		return spellsKnown;
	}

	/**
	*  String representation of spells known.
	*/
	String spellsString () {
		return spellsKnown == null ? null : spellsKnown.toString();
	}

	/**
	*  Get adjusted hit dice for this class.
	*/
	public Dice getHitDice () {
		Dice dice = new Dice(classType.getHitDiceTotal(level));
		int conBonus = character.getAbilityBonus(Ability.Con);
		int hpBonus = conBonus * dice.getNum();
		dice.setAdd(dice.getAdd() + hpBonus);
		return dice;
	}

	/**
	*  Roll full hit points from start to current level.
	*/
	void rollFullHitPoints () {
		hitPoints = 0;
		boolean boost = Character.getBoostInitialAbilities();
		for (int newLevel = 0; newLevel <= level; newLevel++) {
			addNewHitPoints(newLevel, boost);
		}
	}

	/**
	*  Add hit points for a given level.
	*  Per OED rules, at initial generation,
	*    we do not accept 1 or 2 hp die-rolls.
	*/
	void addNewHitPoints (int newLevel, boolean initBoost) {
		int hpBonus = character.getAbilityBonus(Ability.Con);

		if (newLevel == 0) {  // Roll d6 for any class
			Dice newDice = new Dice(1, LEVEL_ZERO_HIT_DIE, hpBonus);
			if (initBoost)
				newDice = boostInitHitDice(newDice);
			hitPoints += newDice.boundRoll(1);
		}
		else if (newLevel == 1) {  // Pro-rate to new class die
			int revisedRoll = (int) Math.round((hitPoints - hpBonus) 
				* (double) classType.getHitDiceType() / LEVEL_ZERO_HIT_DIE);
			hitPoints = Math.max(1, revisedRoll + hpBonus);
		}
		else if (newLevel > 1) {
			Dice newDice = classType.getHitDiceInc(newLevel);
			if (newDice.getNum() > 0) {  // Roll new die
				newDice.modifyAdd(hpBonus);
				if (initBoost)
					newDice = boostInitHitDice(newDice);
				hitPoints += newDice.boundRoll(1);
			}
			else {  // Add constant for high level
				hitPoints += newDice.getAdd();			
			}
		}		
	}

	/**
	*  Convert hit dice for init boost (no 1's or 2's).
	*/
	private Dice boostInitHitDice (Dice hitDice) {
		Dice boostDice = new Dice(hitDice);	
		boostDice.setSides(boostDice.getSides() - 2);
		boostDice.modifyAdd(boostDice.getNum() * 2);
		return boostDice;	
	}

	/**
	*  Handle a Constitution change to hit points.
	*/
	public void handleConChange (int oldCon) {
		int oldBonus = Ability.getBonus(oldCon);
		int newBonus = character.getAbilityBonus(Ability.Con);
		int diffBonus = newBonus - oldBonus;
		int numDice = classType.getHitDiceTotal(level).getNum();
		hitPoints = Math.max(numDice, hitPoints + diffBonus * numDice);
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		return classType.getName() 
		 + "(" + level + ", " + hitPoints + ", " + XP + ")";
	}	
}

