/******************************************************************************
*  Ability scores enumeration and methods.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-21
*  @version  1.0
******************************************************************************/

public enum Ability {
	Str, Int, Wis, Dex, Con, Cha;

	/** Total number of ability scores available. */
	public static final int length = Ability.values().length;

	//--------------------------------------------------------------------------
	//  Inner Enumeration
	//--------------------------------------------------------------------------

	/** Available bonus formula methods. */
	enum BonusType {Bonus_BX, Bonus_OED};

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Constant switch for bonus formula. */
	final static BonusType BONUS_TYPE = BonusType.Bonus_OED;

	/** Prioritized preference for any class based on prime requisite. */
	private final static Ability[][] abilityPriority = 
		{{Str, Dex, Con, Int, Wis, Cha}, {Int, Dex, Con, Cha, Wis, Str},
		 {Wis, Con, Str, Int, Cha, Dex}, {Dex, Str, Con, Int, Cha, Wis},
		 {Con, Str, Dex, Wis, Cha, Int}, {Cha, Dex, Int, Wis, Con, Str}};
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	
	/**
	*  Gives the bonus for a given ability score.
	*/
	public static int getBonus (int score) {
		switch (BONUS_TYPE) {
			case Bonus_BX: return getBonus_BX(score); 
			case Bonus_OED: return getBonus_OED(score);
			default: return 0;
		}
	}

	/**
	*  BX-style bonus for a given ability score.
	*/
	private static int getBonus_BX (int score) {
		switch (score) {
			case 3: return -3;
			case 4: case 5: return -2;
			case 6: case 7: case 8: return -1;
			case 9: case 10: case 11: case 12: return 0;
			case 13: case 14: case 15: return +1;
			case 16: case 17: return +2;
			case 18: return +3;

			// Note: Mentzer's Immortals rules have a 
			// lunatic system for ability scores 1-100;
			// we ignore that here.
			default: return Integer.MIN_VALUE;
		}
	}

	/**
	*  OED-style bonus for a given ability score.
	*/
	private static int getBonus_OED (int score) {
		if (score <= 8) return (score - 11)/3;
		else if (score >= 13) return (score - 10)/3;
		else return 0;
	}
	
	/**
	*  Return bonus percent of XP for prime requisite.
	*/
	public static int bonusPercentXP (int score) {
		if (score >= 15) return 10;
		else if (score >= 13) return 5;
		else if (score >=9) return 0;
		else if (score >= 7) return -10;
		else return -20;
	}
	
	/**
	*  Get ability priority list based on prime requisite.
	*/
	public static Ability[] getPriorityList(Ability primeReq) {
		return abilityPriority[primeReq.ordinal()];
	}
}

