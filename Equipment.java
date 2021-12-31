/******************************************************************************
*  One piece of equipment on a character.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-17
******************************************************************************/

public class Equipment {

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	/** Material types enumeration. */
	enum Material {Unknown, Wood, Leather, Steel, Silver}

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

	/** Material type. */
	Material material;

	/** Encumbrance in stone units. */
	float weight;

	/** Magic bonus value. */
	int magicBonus;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor
	*/
	Equipment (String name, Material material, float weight, int magic) {
		this.name = name;
		this.material = material;
		this.weight = weight;
		setMagicBonus(magic);
	}
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public String getName () { return name; }
	public Material getMaterial () { return material; }
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
	*  Is this piece of equipment made of metal?
	*/
	public boolean isMetallic () {
		switch (material) {
			case Steel: case Silver: return true;
		}
		return false;
	}

	/**
	*  Roll a saving throw for this equipment.
	*  Roughly equal to that seen on OD&D Vol-2, p. 38.
	*/
	public boolean rollSave () {
		return Dice.roll(6) <= 1 + magicBonus;
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

