/******************************************************************************
*  Ability scores enumeration and methods.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-21
******************************************************************************/

public enum Ability {
	Str, Int, Wis, Dex, Con, Cha;

	//--------------------------------------------------------------------------
	//  Inner Enumeration
	//--------------------------------------------------------------------------

	/** Available ability bonus rules. */
	private enum BonusRule {BX, OED};

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Constant switch for bonus formula. */
	private static final BonusRule BONUS_RULE = BonusRule.OED;

	/** Prioritized preference for any class based on prime requisite. */
	private static final Ability[][] abilityPriority = 
		{{Str, Dex, Con, Int, Wis, Cha}, {Int, Dex, Con, Cha, Wis, Str},
		 {Wis, Con, Str, Int, Cha, Dex}, {Dex, Str, Con, Int, Cha, Wis},
		 {Con, Str, Dex, Wis, Cha, Int}, {Cha, Dex, Int, Wis, Con, Str}};

	/** Long-form names for abilities. */
	private static final String[] fullName =
		{"Strength", "Intelligence", "Wisdom", "Dexterity", "Constitution", "Charisma"};

	/** Array of B/X style ability bonuses (for performance.) */
	private static final int[] BonusValueBX =
		{-5, -4, -3, -3, -2, -2, -1, -1, -1, 0, 0, 0, 0,
		1, 1, 1, 2, 2, 3, 4, 4, 5, 5, 5, 6, 6, 6, 6};
	
	/** Array of OED style ability bonuses (for performance). */
	private static final int[] BonusValueOED =
	 	{-3, -3, -3, -2, -2, -2, -1, -1, -1, 0, 0, 0, 0, 
		1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5};
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Get long-form name for an ability.
	*/
	public static String getFullName (Ability ability) {
		return fullName[ability.ordinal()];
	}
	
	/**
	*  Gives the bonus for a given ability score.
	*/
	public static int getBonus (int score) {
		if (BONUS_RULE == BonusRule.OED)
			return getBonusOED(score);
		else
			return getBonusBX(score);
	}

	/**
	*  BX-style bonus for a given ability score.
	*/
	static int getBonusBX (int score) {
		assert(score >= 0);
		if (score < BonusValueBX.length)
			return BonusValueBX[score];
		else
			// See Mentzer's Immortals rules for 
			// further extension of scores up to 100.
			return 7;
	}

	/**
	*  OED-style bonus for a given ability score.
	*/
	static int getBonusOED (int score) {
		assert(score >= 0);
		if (score < BonusValueOED.length)
			return BonusValueOED[score];
		else 
			return (score - 11) / 3;
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
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {
		System.out.println("Score\tBonus");
		System.out.println("-----\t-----");
		for (int i = 3; i <= 18; i++) {
			System.out.println(i + "\t" + getBonus(i));		
		}
		System.out.println();
	}
}

