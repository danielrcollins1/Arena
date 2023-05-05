/**
	Treasure type associated with a monster (in the wilderness).

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2018-02-13
*/

public class TreasureType {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Treasure type letter code. */
	private char typeCode;

	/** Parameters for copper. */
	private TreasureParams copper;

	/** Parameters for silver. */
	private TreasureParams silver;

	/** Parameters for gold. */
	private TreasureParams gold;

	/** Parameters for gems. */
	private TreasureParams gems;

	/** Parameters for jewelry. */
	private TreasureParams jewelry;

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Parameters for one category of treasure. */
	private class TreasureParams {

		/** Percent chance this item is present. */
		private int percent;

		/** Dice to roll for amount. */
		private Dice dice;

		/** Constructor. */		
		private TreasureParams(String sDice, String sPercent) {
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
		Constructor (from string array).
	*/
	public TreasureType(String[] s) {
		typeCode = s[0].charAt(0);
		copper = new TreasureParams(s[1], s[2]);
		silver = new TreasureParams(s[3], s[4]);
		gold = new TreasureParams(s[5], s[6]);
		gems = new TreasureParams(s[7], s[8]);
		jewelry = new TreasureParams(s[9], s[10]);
	}	
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Get the letter type code.
	*/
	public char getCode() {
		return typeCode;
	}
	
	/**
		Identify this object as a string.
	*/
	public String toString() {
		return typeCode + ": "
			+ "CP " + copper.dice + ":" + copper.percent + "%, "
			+ "SP " + silver.dice + ":" + silver.percent + "%, "
			+ "GP " + gold.dice + ":" + gold.percent + "%, "
			+ "Gems " + gems.dice + ":" + gems.percent + "%, "
			+ "Jewelry " + jewelry.dice + ":" + jewelry.percent + "%, ";
	}
	
	/**
		Get random treasure value.
		@return total treasure value in gold pieces
	*/
	public int randomValue() {
		int total = 0;
		Dice pct = new Dice(100);
		if (pct.roll() <= copper.percent) {
			total += copper.dice.roll() * 1000 / 50;
		}
		if (pct.roll() <= silver.percent) {
			total += silver.dice.roll() * 1000 / 10;
		}
		if (pct.roll() <= gold.percent) {
			total += gold.dice.roll() * 1000;
		}
		if (pct.roll() <= gems.percent) {
			total += gems.dice.roll() * GemsAndJewelry.randomGemValue();
		}
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
