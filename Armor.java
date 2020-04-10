/******************************************************************************
*  Armor on a character.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-17
*  @version  1.0
******************************************************************************/

public class Armor extends Equipment {

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	public enum Type {Plate, Chain, Leather, Shield};

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Type of armor. */
	Type armorType;

	/** Base armor points added. */
	int baseArmor;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (all fields)
	*/
	Armor (Type armorType, int baseArmor, float weight, int magicBonus) {
		super(armorType.toString(), weight, magicBonus); 
		this.armorType = armorType;
		this.baseArmor = baseArmor;
	}

	/**
	*  Constructor (no magic)
	*/
	Armor (Type armorType, int baseArmor, float weight) {
		this(armorType, baseArmor, weight, 0);
	}

	/**
	*  Constructor (copy)
	*/
	Armor (Armor a) {
		this(a.armorType, a.baseArmor, a.weight, a.magicBonus);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Basic accessors.
	public Type getArmorType () { return armorType; }
	public int getBaseArmor () { return baseArmor; }

	/**
	*  Is this armor made of metal?
	*/
	public boolean isMetal () {
		return armorType == Type.Chain || armorType == Type.Plate;
	}
	
	/**
	*  Create a new armor of a given type.
	*/
	static public Armor makeType (Type type) {
		switch (type) {
			case Plate: return new Armor(type, 6, 4);
			case Chain: return new Armor(type, 4, 2);
			case Leather: return new Armor(type, 2, 1);
			case Shield: return new Armor(type, 1, 1);
			default: 
				System.err.println("Armor type has undefined values.");
				return null;			
		}	
	}
}

