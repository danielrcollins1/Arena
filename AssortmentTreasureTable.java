/**
	Treasure based on Monster & Treasure Assortment system.
	
	This system is shown as interpolated by DRC.
	Note official system only goes up to level 9.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2026-04-04
*/

public class AssortmentTreasureTable {

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Roll for a random treasure category.
		Same as table in TSR1 solo dungeon.
	*/
	private static Treasure.Category rollCategory() {
		int roll = Dice.rollPct();
		if (roll <= 25)      { return Treasure.Category.Copper; }
		else if (roll <= 50) { return Treasure.Category.Silver; }
		else if (roll <= 65) { return Treasure.Category.Electrum; }
		else if (roll <= 80) { return Treasure.Category.Gold; }
		else if (roll <= 90) { return Treasure.Category.Platinum; }
		else if (roll <= 94) { return Treasure.Category.Gems; }
		else if (roll <= 97) { return Treasure.Category.Jewelry; }
		else                 { return Treasure.Category.Magic; }		
	}

	/**
		Roll amount for one class in table.
		Quantities like TSR1 solo dungeon (MTA adds minor variation).
	*/
	private static int rollAmount(Treasure.Category cat, int level) {
		switch (cat) {
			case Copper: return level * 1000;
			case Silver: return level * 1000;
			case Electrum: return level * 750;
			case Gold: return level * 400;
			case Platinum: return level * 100;
			case Gems: return Dice.roll(level, 4) * rollGemsValue(level);
			case Jewelry: return Dice.roll(level) * rollJewelryValue(level);
			case Magic: return Dice.roll(1, (level + 1) / 2);
			default: System.err.println("Unhandled treasure category");
		}
		return 0;
	}

	/**
		Roll value of jewelry.
	*/
	private static int rollJewelryValue(int level) {
		return Dice.roll(3, 6) * level * 50;	
	}

	/**
		Roll value of gems.
	*/
	private static int rollGemsValue(int level) {
		int gemClass = Dice.roll(3);

		// Adjust for level
		if (level >= 7) {
			gemClass += 2;
		}
		else if (gemClass >= 4) {
			gemClass += 1;
		}
		
		// Return value
		return GemsAndJewelry.getGemClassValue(gemClass);
	}

	/**
		Roll a combination treasure.
	*/
	private static Treasure rollCombo(int level) {
		assert level >= 6;
		Treasure treasure = new Treasure();
		int maxEntry = level <= 9 ? level - 5 : 4;
		for (int i = 0; i <= maxEntry; i++) {
			Treasure.Category cat = Treasure.Category.VALUES[i];
			treasure.set(cat, rollAmount(cat, level));
		}		
		return treasure;
	}

	/**
		Roll a random treasure.
	*/
	public static Treasure rollTreasureForLevel(int level) {
		assert level > 0;
		Treasure.Category cat = rollCategory();
		
		// Copper may turn to magic
		if (cat == Treasure.Category.Copper && Dice.roll(6) <= level) {
			cat = Treasure.Category.Magic;
		}

		// Silver may become combo treasure
		if (cat == Treasure.Category.Silver && level >= 6 && Dice.coinFlip()) {
			return rollCombo(level);		
		}

		// Standard handler
		Treasure treasure = new Treasure();
		treasure.set(cat, rollAmount(cat, level));
		return treasure;		
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
		Dice.initialize();		

		// Random sample values
		System.out.println("Treasure Sample Values");
		for (int level = 1; level <= 9; level++) {
			System.out.print(level + ": ");
			for (int j = 0; j < 6; j++) {
				Treasure treas = rollTreasureForLevel(level);
				int value = treas.getValue();
				System.out.print(value + " ");
			}
			System.out.println();
		}
		System.out.println();

		// Estimated average values
		System.out.println("Estimated Average Values");
		final int sampleSize = 10000;
		for (int level = 1; level <= 9; level++) {
			long total = 0;
			for (int j = 0; j < sampleSize; j++) {
				Treasure treas = rollTreasureForLevel(level);
				total += treas.getValue();
			}
			System.out.println("Level " + level + ": " + total / sampleSize);
		}
		System.out.println();
	}
}
