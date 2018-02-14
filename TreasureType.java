import java.io.*; 
import java.util.*;

/******************************************************************************
*  Treasure type associated with a monster (in the wilderness).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-02-13
*  @version  1.0
******************************************************************************/

public class TreasureType {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	char letterCode;
	Dice copperDice, silverDice, goldDice, gemsDice, jewelryDice;
	int copperPct, silverPct, goldPct, gemsPct, jewelryPct;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (from string array).
	*/
	TreasureType (String[] s) {
		letterCode = s[0].charAt(0);
		copperDice = parseDice(s[1]);
		copperPct = CSVReader.parseInt(s[2]);
		silverDice = parseDice(s[3]);
		silverPct = CSVReader.parseInt(s[4]);
		goldDice = parseDice(s[5]);
		goldPct = CSVReader.parseInt(s[6]);
		gemsDice = parseDice(s[7]);
		gemsPct = CSVReader.parseInt(s[8]);
		jewelryDice = parseDice(s[9]);
		jewelryPct = CSVReader.parseInt(s[10]);
	}	
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	
	/**
	*  Parse a value dice description.
	*/
	private Dice parseDice (String s) {
		return s.equals("-") ? null : new Dice(s);
	}
	
	/**
	* Identify this object as a string.
	*/
	public String toString() {
		return letterCode + ": "
			+ "CP " + copperDice + ":" + copperPct + "%, "
			+ "SP " + silverDice + ":" + silverPct + "%, "
			+ "GP " + goldDice + ":" + goldPct + "%, "
			+ "Gems " + gemsDice + ":" + gemsPct + "%, "
			+ "Jewelry " + jewelryDice + ":" + jewelryPct + "%, ";
	}
}

