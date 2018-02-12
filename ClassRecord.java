/******************************************************************************
*  Record of one class gained by a character (XP, level, hit points, etc.).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-22
*  @version  1.0
******************************************************************************/

public class ClassRecord {

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
	*/
	public void addLevel () {
		level++;	
		XP = Math.max(XP, classType.getXpReq(level));
		rollNewHitPoints(level);
		if (isFeatLevel(level)) {
			character.addFeat();
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
				character.loseFeat();
			Dice diceInc = classType.getHitDiceInc(level);
			if (diceInc.getAdd() > 0)
				hitPoints -= diceInc.getAdd();
			else
				hitPoints = hitPoints * (level - 1)/level;
			level--;
			XP = Math.min(XP, getMidpointXP());
		}
	}

	/**
	*  Choose all feats for new character.
	*/
	void addAllFeats () {
		for (int i = 1; i <= level; i++) {
			if (isFeatLevel(i)) {
				character.addFeat();			
			}
		}
	}

	/**
	*  Is this a level where we gain a feat?
	*/
	boolean isFeatLevel (int newLevel) {
		return Character.useFeats()
			&& classType.getName().equals("Fighter")
			&& newLevel % 4 == 0;	
	}

	/**
	*  Get midpoint XP for current level.
	*/
	int getMidpointXP () {
		int low = classType.getXpReq(level);
		int high = classType.getXpReqNext(level);
		return (low + high)/2;
	}

	/**
	*  Add hit points for a given level.
	*/
	void rollNewHitPoints (int newLevel) {
		int hpBonus = character.getAbilityBonus(Ability.Con);

		// From level 0 to 1st
		if (newLevel == 1) {
			hitPoints += (classType.getHitDiceType() - 6)/2;
			hitPoints = Math.max(1, hitPoints);
		}
		
		// Levels above 1st
		else if (newLevel > 1) {
			Dice newDice = classType.getHitDiceInc(newLevel);
			hitPoints += (newDice.getNum() > 0 ?
				Math.max(1, newDice.rollPlus(hpBonus)) : newDice.getAdd());
		}		
	}

	/**
	*  Roll full hit points from start to current level.
	*/
	void rollFullHitPoints () {
		int hpBonus = character.getAbilityBonus(Ability.Con);
		hitPoints = Math.max(1, new Dice(1, 6).rollPlus(hpBonus)); // Level 0
		for (int newLevel = 1; newLevel <= level; newLevel++) {
			rollNewHitPoints(newLevel);
		}
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

