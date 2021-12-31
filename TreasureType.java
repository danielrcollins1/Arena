/******************************************************************************
*  Treasure type associated with a monster (in the wilderness).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-02-13
******************************************************************************/

public class TreasureType {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	private char key;
	private TreasureCategory copper, silver, gold, gems, jewelry;

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	private class TreasureCategory {
		Dice dice;
		int percent;
		
		TreasureCategory (String sDice, String sPercent) {
			if (sDice.equals("-")) {
				dice = null;
				percent = 0;
			}
			else {
				dice = new Dice(sDice);
				percent = Integer.parseInt(sPercent);
			}
		}
	}

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (from string array).
	*/
	TreasureType (String[] s) {
		key = s[0].charAt(0);
		copper = new TreasureCategory(s[1], s[2]);
		silver = new TreasureCategory(s[3], s[4]);
		gold = new TreasureCategory(s[5], s[6]);
		gems = new TreasureCategory(s[7], s[8]);
		jewelry = new TreasureCategory(s[9], s[10]);
	}	
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Get the letter key code.
	*/
	public char getKey () {
		return key;
	}
	
	/**
	* Identify this object as a string.
	*/
	public String toString() {
		return key + ": "
			+ "CP " + copper.dice + ":" + copper.percent + "%, "
			+ "SP " + silver.dice + ":" + silver.percent + "%, "
			+ "GP " + gold.dice + ":" + gold.percent + "%, "
			+ "Gems " + gems.dice + ":" + gems.percent + "%, "
			+ "Jewelry " + jewelry.dice + ":" + jewelry.percent + "%, ";
	}
	
	/**
	*  Get random treasure value.
	*/
	public int randomValue () {
		int total = 0;
		Dice pct = new Dice(100);
		if (pct.roll() <= copper.percent)
			total += copper.dice.roll() * 1000 / 50;
		if (pct.roll() <= silver.percent)
			total += silver.dice.roll() * 1000 / 10;
		if (pct.roll() <= gold.percent)
			total += gold.dice.roll() * 1000;
		if (pct.roll() <= gems.percent)
			total += gems.dice.roll() * GemsAndJewelry.randomGemValue();
		if (pct.roll() <= jewelry.percent) {
			int number = jewelry.dice.roll();
			Dice valueClass = GemsAndJewelry.randomJewelryClass(); 
			for (int i = 0; i < number; i++) {
				total += valueClass.roll();
			}
		}
		return total;
	}
}

