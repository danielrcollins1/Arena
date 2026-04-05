/**
	Treasure based on Monster & Trasure Assortment system.
	
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
		Get treasure value for one entry in table.
		Type 1 = copper, 2 = silver, 3 = electrum, etc.
		Quantities like TSR1 solo dungeon (MTA adds minor variation).
	*/
	private static int getValueOneEntry(int type, int level) {
		switch (type) {
			case 1: return level * 1000 / 50;
			case 2: return level * 1000 / 10;
			case 3: return level * 750 / 2;
			case 4: return level * 400 * 1;
			case 5: return level * 100 * 5;
			case 6: return Dice.roll(level, 4) * rollGemValue(level);
			case 7: return Dice.roll(level) * rollJewelryValue(level);
			default: System.err.println("Invalid treasure assortment entry");
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
	private static int rollGemValue(int level) {
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
	private static int rollComboValue(int level) {
		assert level >= 6;
		int value = 0;
		int maxEntry = level <= 9 ? level - 4 : 5;
		for (int i = 1; i <= maxEntry; i++) {
			value += getValueOneEntry(i, level);
		}		
		return value;
	}

	/**
		Roll a random treasure.
	*/
	public static int randomValueByLevel(int level) {
		assert level > 0;
		int rollPct = Dice.rollPct();	

		// Copper (may turn to magic)
		if (rollPct <= 25) {
			return Dice.roll(6) <= level 
				? 0 : getValueOneEntry(1, level);
		}
		
		// Silver (may turn to combo type)
		else if (rollPct <= 50) {
			return level >= 6 && Dice.coinFlip() 
				? rollComboValue(level) : getValueOneEntry(2, level);
		}
		
		// Other types
		else if (rollPct <= 65) { return getValueOneEntry(3, level); }
		else if (rollPct <= 80) { return getValueOneEntry(4, level); }
		else if (rollPct <= 90) { return getValueOneEntry(5, level); }
		else if (rollPct <= 94) { return getValueOneEntry(6, level); }
		else if (rollPct <= 97) { return getValueOneEntry(7, level); }
		else { return 0; }
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
				int value = randomValueByLevel(level);
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
				total += randomValueByLevel(level);
			}
			System.out.println("Level " + level + ": " + total / sampleSize);
		}
		System.out.println();
	}
}
