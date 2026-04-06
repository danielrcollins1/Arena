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
	
	/** Parameters for magic. */
	private TreasureParams magic;

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
		magic = new TreasureParams(s[11], s[12]);
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
			+ "Jewelry " + jewelry.dice + ":" + jewelry.percent + "%, "
			+ "Magic " + magic.dice + ":" + magic.percent + "%";
	}
	
	/**
		Get random treasure for this type.
	*/
	public Treasure rollTreasure() {
		Treasure treas = new Treasure();
		if (Dice.rollPct() <= copper.percent) {
			treas.set(Treasure.Category.Copper, copper.dice.roll() * 1000);
		}
		if (Dice.rollPct() <= silver.percent) {
			treas.set(Treasure.Category.Silver, silver.dice.roll() * 1000);
		}
		if (Dice.rollPct() <= gold.percent) {
			treas.set(Treasure.Category.Gold, gold.dice.roll() * 1000);
		}
		if (Dice.rollPct() <= gems.percent) {
			treas.set(Treasure.Category.Gems, 
				gems.dice.roll() * GemsAndJewelry.randomGemValue());
		}
		if (Dice.rollPct() <= jewelry.percent) {
			treas.set(Treasure.Category.Jewelry, 
				jewelry.dice.roll() * GemsAndJewelry.randomJewelryValue());
		}
		if (Dice.rollPct() <= magic.percent) {
			treas.set(Treasure.Category.Magic, magic.dice.roll());
		}
		return treas;
	}
}
