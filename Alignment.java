/******************************************************************************
*  Alignment enumeration.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-19
*  @version  1.0
******************************************************************************/

public enum Alignment {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	Lawful, Neutral, Chaotic;
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Convert a letter to alignment.
	*/
	public static Alignment getFromChar (char c) {
		switch (c) {
			case 'L': return Lawful;
			case 'N': return Neutral;
			case 'C': return Chaotic;
			default: return null;
		}
	}

	/**
	*  Return a random alignment.
	*/
	public static Alignment random () {
		switch (Dice.roll(3)) {
			case 1: return Lawful;
			case 2: return Neutral;
			case 3: return Chaotic;
			default: return null;
		}
	}
}

