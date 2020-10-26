package net.superdan.athena;

/******************************************************************************
 *  net.superdan.athena.Ability scores enumeration and methods.
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2014-05-21
 ******************************************************************************/

public enum Ability {
	Str, Int, Wis, Dex, Con, Cha;

	/**
	 * Total number of ability scores available.
	 */
	public static final int length = Ability.values().length;

	//--------------------------------------------------------------------------
	//  Inner Enumeration
	//--------------------------------------------------------------------------

	/**
	 * Available bonus formula methods.
	 */
	enum BonusType {Bonus_BX, Bonus_OED}

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/**
	 * Constant switch for bonus formula.
	 */
	final static BonusType BONUS_TYPE = BonusType.Bonus_OED;

	/**
	 * Prioritized preference for any class based on prime requisite.
	 */
	final static Ability[][] abilityPriority =
			{{Str, Dex, Con, Int, Wis, Cha}, {Int, Dex, Con, Cha, Wis, Str},
					{Wis, Con, Str, Int, Cha, Dex}, {Dex, Str, Con, Int, Cha, Wis},
					{Con, Str, Dex, Wis, Cha, Int}, {Cha, Dex, Int, Wis, Con, Str}};

	/**
	 * Long-form names for abilities.
	 */
	final static String[] fullName =
			{"Strength", "Intelligence", "Wisdom", "Dexterity", "Constitution", "Charisma"};

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	 * Get long-form name for an ability.
	 */
	public static String getFullName(Ability ability) {
		return fullName[ability.ordinal()];
	}

	/**
	 * Gives the bonus for a given ability score.
	 */
	public static int getBonus(int score) {
		return switch (BONUS_TYPE) {
			case Bonus_BX -> getBonus_BX(score);
			case Bonus_OED -> getBonus_OED(score);
		};
	}

	/**
	 * BX-style bonus for a given ability score.
	 */
	static int getBonus_BX(int score) {
		// Note: Mentzer's Immortals rules have a
		// lunatic system for ability scores 1-100;
		// we ignore that here.
		return switch (score) {
			case 3 -> -3;
			case 4, 5 -> -2;
			case 6, 7, 8 -> -1;
			case 9, 10, 11, 12 -> 0;
			case 13, 14, 15 -> +1;
			case 16, 17 -> +2;
			case 18 -> +3;
			default -> Integer.MIN_VALUE;
		};
	}

	/**
	 *  OED-style bonus for a given ability score.
	 */
	static int getBonus_OED (int score) {
		return score > 10 ? (score - 10) / 3 : (score - 11) /3;
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

