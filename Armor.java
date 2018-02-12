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

	/** Max movement allowed. */
	int maxMovement;
	
	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (all fields)
	*/
	Armor (Type armorType, int baseArmor, int maxMove, int magicBonus) {
		super(armorType.toString(), magicBonus); 
		this.armorType = armorType;
		this.baseArmor = baseArmor;
		this.maxMovement = maxMove;
	}

	/**
	*  Constructor (copy)
	*/
	Armor (Armor a) {
		this(a.armorType, a.baseArmor, a.maxMovement, a.magicBonus);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Basic accessors.
	public Type getArmorType () { return armorType; }
	public int getBaseArmor () { return baseArmor; }
	public int getMaxMove () { return maxMovement; }

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
			case Plate: return new Armor(type, 6, 6, 0);
			case Chain: return new Armor(type, 4, 9, 0);
			case Leather: return new Armor(type, 2, 12, 0);
			case Shield: return new Armor(type, 1, 0, 0);
			default: return null;			
		}	
	}
}

