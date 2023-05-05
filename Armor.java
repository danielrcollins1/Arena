/**
	Armor on a character.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2016-01-17
*/

public class Armor extends Equipment {

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	/** Class of armor. */
	public enum Type { Shield, Leather, Chain, Plate };

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Type of armor. */
	private Type armorType;

	/** Base armor points added. */
	private int baseArmor;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
		Full constructor.
	*/
	Armor(Type armorType, Equipment.Material material, 
		int baseArmor, float weight, int magicBonus)
	{
		super(armorType.toString(), material, weight, magicBonus);
		this.armorType = armorType;
		this.baseArmor = baseArmor;
	}

	/**
		Copy constructor.
	*/
	Armor(Armor a) {
		this(a.armorType, a.material, a.baseArmor, a.weight, a.magicBonus);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Get the type of armor.
	*/
	public Type getArmorType() { 
		return armorType; 
	}
	
	/**
		Get the base protective points for this armor.
	*/
	public int getBaseArmor() { 
		return baseArmor; 
	}

	/**
		Is this armor made of metal?
	*/
	public boolean isMetal() {
		return armorType == Type.Chain || armorType == Type.Plate;
	}
	
	/**
		Create a new armor of a given type.
	*/
	public static Armor makeType(Type type) {
		if (type != null) {
			switch (type) {
				case Shield: return new Armor(type, Material.Wood, 1, 1, 0);
				case Leather: return new Armor(type, Material.Leather, 2, 1, 0);
				case Chain: return new Armor(type, Material.Steel, 4, 2, 0);
				case Plate: return new Armor(type, Material.Steel, 6, 4, 0);
				default: System.err.println("Armor type unknown value: " + type);
			}	
		}
		return null;
	}
}
