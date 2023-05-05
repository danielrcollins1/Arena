/**
	Ability scores enumeration and methods.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2014-05-21
*/

public enum Ability {

	Strength, Intelligence, Wisdom, Dexterity, Constitution, Charisma;
	
	/** Constant size record. */
	private static final int SIZE = Ability.values().length;

	/** Abbreviations for values. */
	private static final String[] ABBREVIATION = {
		"Str", "Int", "Wis", "Dex", "Con", "Cha"
	};

	//--------------------------------------------------------------------------
	//  Inner Enumeration
	//--------------------------------------------------------------------------

	/** Available ability bonus rules. */
	private enum BonusRule { BX, OED };

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Constant switch for bonus formula. */
	private static final BonusRule BONUS_RULE = BonusRule.OED;

	/** Array of B/X style ability bonuses (for performance). */
	private static final int[] BONUS_VALUE_BX =
		{-5, -4, -3, -3, -2, -2, -1, -1, -1, 0, 0, 0, 0,
		1, 1, 1, 2, 2, 3, 4, 4, 5, 5, 5, 6, 6, 6, 6};
	
	/** Array of OED style ability bonuses (for performance). */
	private static final int[] BONUS_VALUE_OED =
	 	{-3, -3, -3, -2, -2, -2, -1, -1, -1, 0, 0, 0, 0, 
		1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5};
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Get size of this enumeration.
	*/
	public static int size() {
		return SIZE;
	}

	/**
		Get ability abbreviation.
	*/
	public String getAbbreviation() {
		return ABBREVIATION[this.ordinal()];
	}

	/**
		Gives the bonus for a given ability score.
	*/
	public static int getBonus(int score) {
		switch (BONUS_RULE) {
			case BX: return getBonusBX(score);
			case OED: return getBonusOED(score);
			default: System.err.println("Unknown ability bonus rule.");
		}
		return 0;
	}

	/**
		BX-style bonus for a given ability score.
	*/
	static int getBonusBX(int score) {
		assert score >= 0;
		if (score < BONUS_VALUE_BX.length) {
			return BONUS_VALUE_BX[score];
		}
		else {
			// See Mentzer's Immortals rules for 
			// further extension of scores up to 100.
			return 7;
		}
	}

	/**
		OED-style bonus for a given ability score.
	*/
	static int getBonusOED(int score) {
		assert score >= 0;
		if (score < BONUS_VALUE_OED.length) {
			return BONUS_VALUE_OED[score];
		}
		else  {
			return (score - 11) / 3;
		}
	}
	
	/**
		Return bonus percent of XP for prime requisite.
	*/
	public static int bonusPercentXP(int score) {
		if (score >= 15) { return 10; }
		else if (score >= 13) { return 5; }
		else if (score >= 9) { return 0; }
		else if (score >= 7) { return -10; }
		else { return -20; }
	}
	
	/**
		Main test function.
	*/
	public static void main(String[] args) {
		System.out.println("Score\tBonus");
		System.out.println("-----\t-----");
		for (int i = 3; i <= 18; i++) {
			System.out.println(i + "\t" + getBonus(i));		
		}
		System.out.println();
	}
}
