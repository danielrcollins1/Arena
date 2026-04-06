import java.util.ArrayList;

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
	public enum Type { Shield, Leather, Chain, Plate, Helmet };

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
		return armorType == Type.Chain || armorType == Type.Plate 
			|| armorType == Type.Helmet;
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
				case Helmet: return new Armor(type, Material.Steel, 0, 0.3f, 0);
				default: System.err.println("Armor type unknown value: " + type);
			}
		}
		return null;
	}
	
	/**
		Make a random set of armor.
		This is a simplified version of the armor table in Sup-I.
		Returns an array because it may return armor & shield set.
		Assumes every armor is plate.
	*/
	public static ArrayList<Armor> randomMagicArmor() {

		// Find bonuses
		int armorBonus = 0, shieldBonus = 0;
		int roll = Dice.rollPct();
		if (roll <= 20)      { shieldBonus = +1; }
		else if (roll <= 40) { armorBonus = +1; }
		else if (roll <= 50) { armorBonus = +1; shieldBonus = +1; }
		else if (roll <= 57) { shieldBonus = +2; }
		else if (roll <= 64) { armorBonus = +2; }
		else if (roll <= 70) { armorBonus = +2; shieldBonus = +2; }
		else if (roll <= 73) { shieldBonus = +3; }
		else if (roll <= 76) { armorBonus = +3; }
		else if (roll <= 78) { armorBonus = +3; shieldBonus = +3; }
		else if (roll <= 80) { shieldBonus = +4; }
		else if (roll <= 82) { armorBonus = +4; }
		else if (roll <= 83) { armorBonus = +4; shieldBonus = +4; }
		else if (roll <= 85) { shieldBonus = +5; }
		else if (roll <= 86) { armorBonus = +5; }
		else if (roll <= 87) { armorBonus = +5; shieldBonus = +5; }
		else if (roll <= 93) { shieldBonus = -1; }
		else                 { armorBonus = -1; }		

		// Make armor items
		ArrayList<Armor> list = new ArrayList<Armor>();
		if (armorBonus != 0) {
			Armor armor = makeType(Type.Plate);
			armor.setMagicBonus(armorBonus);
			list.add(armor);
		}
		if (shieldBonus != 0) {
			Armor shield = makeType(Type.Shield);
			shield.setMagicBonus(shieldBonus);
			list.add(shield);
		}
		return list;
	}
}
