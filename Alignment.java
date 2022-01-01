/******************************************************************************
*  Alignment enumeration.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-19
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
	*  Convert a string to an alignment.
	*/
	public static Alignment getFromString (String s) {
		return (s == null || s.length() == 0) ?
			null : getFromChar(s.charAt(0));
	}

	/**
	*  Randomize a normally-distributed alignment. 
	*/
	public static Alignment randomNormal () {
		switch (Dice.roll(6)) {
			case 1: return Lawful;
			default: return Neutral;
			case 6: return Chaotic;
		}
	}

	/**
	*  Randomize a uniformly-distributed alignment. 
	*/
	public static Alignment randomUniform () {
		switch (Dice.roll(6)) {
			case 1: case 2: return Lawful;
			default:        return Neutral;
			case 5: case 6: return Chaotic;
		}
	}
	
	/**
	*  Randomize a Lawful-biased alignment. 
	*/
	public static Alignment randomLawfulBias () {
		switch (Dice.roll(6)) {
			case 1: case 2: return Neutral;
			default: return Lawful;		
		}	
	}

	/**
	*  Randomize a Chaotic-biased alignment. 
	*/
	public static Alignment randomChaoticBias () {
		switch (Dice.roll(6)) {
			case 1: case 2: return Neutral;
			default: return Chaotic;
		}	
	}
	
	/**
	*  Randomize an alignment biased toward a given one.
	*/
	public static Alignment randomBias (Alignment align) {
		switch (align) {
			case Lawful: return randomLawfulBias();
			case Chaotic: return randomChaoticBias();		
			default: return randomNormal();
		}
	}
	
}

