/******************************************************************************
*  One piece of equipment on a character.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-17
*  @version  1.0
******************************************************************************/

public class Equipment {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Maximum magic bonus allowed. */
	static final int MAX_MAGIC_BONUS = 5;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of this piece of equipment. */
	String name;

	/** Magic bonus value. */
	int magicBonus;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Basic constructor
	*/
	Equipment (String name, int magicBonus) {
		this.name = name;
		setMagicBonus(magicBonus);
	}
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public String getName () { return name; }
	public int getMagicBonus () { return magicBonus; }

	/**
	*  Set the magic bonus.
	*/
	public void setMagicBonus (int bonus) {
		magicBonus = Math.min(bonus, MAX_MAGIC_BONUS);
	}

	/**
	*  Increment the magic bonus.
	*/
	public void incMagicBonus () {
		setMagicBonus(magicBonus + 1);
	}

	/**
	*  Format magic bonus.
	*/
	String formatBonus (int bonus) {
		if (bonus > 0) return "+" + bonus;
		if (bonus < 0) return "" + bonus;
		return "";	
	}
	
	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		String s = getName();
		if (magicBonus != 0) {
			s += " " + formatBonus(magicBonus);
		}
		return s;
	}
}

