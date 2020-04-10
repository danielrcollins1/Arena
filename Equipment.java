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

	/** One-third of a stone. */
	public static final float ONE_THIRD = (float) 1./3;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of this piece of equipment. */
	String name;

	/** Magic bonus value. */
	int magicBonus;

	/** Encumbrance in stone units. */
	float weight;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Basic constructor
	*/
	Equipment (String name, float weight, int magicBonus) {
		this.name = name;
		this.weight = weight;
		setMagicBonus(magicBonus);
	}

	/**
	*  Nonmagic constructor
	*/
	Equipment (String name, float weight) {
		this(name, weight, 0);	
	}
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public String getName () { return name; }
	public float getWeight () { return weight; }
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
	*  Identify this object as a string.
	*/
	public String toString() {
		String s = getName();
		if (magicBonus != 0) {
			s += " " + Dice.formatBonus(magicBonus);
		}
		return s;
	}
}

