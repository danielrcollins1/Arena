/**
	Gems and jewelry valuations.
	
	Implements tables in OD&D Vol-2, p. 40.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2017-11-18
*/

public class GemsAndJewelry {

	//--------------------------------------------------------------------------
	//  Gem methods
	//--------------------------------------------------------------------------

	/**
		Roll random gem class.
	*/
	static int randomGemClass() {
		int roll = Dice.rollPct();
		if (roll <= 10)      { return 1; }
		else if (roll <= 25) { return 2; }
		else if (roll <= 75) { return 3; }
		else if (roll <= 90) { return 4; }
		else                 { return 5; }
	}

	/**
		Get gem value for class.
	*/
	static int getGemClassValue(int gemClass) {
		switch (gemClass) {
			case 1: return 10;
			case 2: return 50;
			case 3: return 100;
			case 4: return 500;
			case 5: return 1000;
			default: System.err.println("Invalid gem class");
		}
		return 0;
	}

	/**
		Randomize one gem value.
	*/
	static int randomGemValue() {
		return getGemClassValue(randomGemClass());
	}

	//--------------------------------------------------------------------------
	//  Jewelry methods
	//--------------------------------------------------------------------------

	/**
		Roll random jewelry class.
	*/
	static int randomJewelryClass() {
		int roll = Dice.rollPct();
		if (roll <= 20)      { return 1; }
		else if (roll <= 80) { return 2; }
		else                 { return 3; }
	}

	/**
		Get jewelry value dice for class.
	*/
	static Dice getJewelryClassDice(int jewelryClass) {
		switch (jewelryClass) {
			case 1: return new Dice(3, 6, 100, 0);
			case 2: return new Dice(1, 6, 1000, 0);
			case 3: return new Dice(1, 10, 1000, 0);
			default: System.err.println("Invalid jewelry class");
		}
		return null;
	}

	/**
		Randomize jewelry class dice.
	*/
	static Dice randomJewelryClassDice() {
		return getJewelryClassDice(randomJewelryClass());
	}
	
	/**
		Randomize one jewelry value.
	*/
	static int randomJewelryValue() {
		return randomJewelryClassDice().roll();	
	}
}
