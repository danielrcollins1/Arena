/******************************************************************************
*  Weapon on a character.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-17
*  @version  1.0
******************************************************************************/

public class Weapon extends Equipment {
	enum Material {Wood, Steel, Silver}

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** One-third of a stone weight. */
	static final float ONE_THIRD = Equipment.ONE_THIRD;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Material type. */
	Material material;

	/** Damage basis. */
	Dice damage;

	/** Energy type. */
	EnergyType energy;

	/** Hands used. */
	int hands;
	
	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (all fields).
	*/
	Weapon (String name, Dice damage, float weight, int hands, 
			int magic, Material material, EnergyType energy) {
		super(name, weight, magic); 
		this.damage = damage;
		this.hands = hands;
		this.material = material;
		this.energy = energy;
	}

	/**
	*  Constructor (name, damage, weight, hands, magic).
	*/
	Weapon (String name, Dice damage, float weight, int hands, int magic) {
		this(name, damage, weight, hands, magic, Material.Steel, null);
	}

	/**
	*  Constructor (name, damage, weight, hands, material).
	*/
	Weapon (String name, Dice damage, float weight, int hands, 
			Material material) {
		this(name, damage, weight, hands, 0, material, null);
	}

	/**
	*  Constructor (name, damage, weight, hands).
	*/
	Weapon (String name, Dice damage, float weight, int hands) {
		this(name, damage, weight, hands, 0, Material.Steel, null);
	}

	/**
	*  Constructor (copy)
	*/
	Weapon (Weapon w) {
		this(w.name, new Dice(w.damage), w.weight, w.hands, 
			w.magicBonus, w.material, w.energy);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public Dice getBaseDamage () { return damage; }
	public Material getMaterial () { return material; }
	public EnergyType getEnergy () { return energy; }
	public int getHandsUsed () { return hands; }
	
	/**
	*  Make a random primary melee weapon.
	*/
	static public Weapon randomPrimary () {
		switch (Dice.roll(8)) {
			case 1: return new Weapon("Sword", new Dice(8), ONE_THIRD, 1);
			case 2: return new Weapon("Two-handed sword", new Dice(10), 1, 2);
			case 3: return new Weapon("Battle axe", new Dice(8), 1, 1);
			case 4: return new Weapon("Halberd", new Dice(10), 1, 2);
			case 5: return new Weapon("Morning star", new Dice(8), 1, 1);
			case 6: return new Weapon("Flail", new Dice(8), 1, 2);
			default: return new Weapon("Sword", new Dice(8), ONE_THIRD, 1);
		}
	}
	
	/**
	*  Make a random secondary melee weapon.
	*/
	static public Weapon randomSecondary () {
		switch (Dice.roll(6)) {
			case 1: return new Weapon("Dagger", new Dice(4), 0, 1);
			case 2: return new Weapon("Spear", new Dice(6), ONE_THIRD, 1);
			case 3: return new Weapon("Hand axe", new Dice(6), ONE_THIRD, 1);
			case 4: return new Weapon("Mace", new Dice(6), ONE_THIRD, 1);
			case 5: return new Weapon("Hammer", new Dice(6), ONE_THIRD, 1);
			default: return new Weapon("Dagger", new Dice(4), 0, 1);
		}
	}	

	/**
	*  Make a random melee weapon for a thief.
	*/
	static public Weapon randomThieving () {
		switch (Dice.roll(6)) {
			case 1: return new Weapon("Sword", new Dice(8), ONE_THIRD, 1);
			case 2: return new Weapon("Dagger", new Dice(4), 0, 1);
			case 3: return new Weapon("Spear", new Dice(6), ONE_THIRD, 1);
			case 4: return new Weapon("Hand axe", new Dice(6), ONE_THIRD, 1);
			case 5: return new Weapon("Mace", new Dice(6), ONE_THIRD, 1);
			default: return new Weapon("Sword", new Dice(8), ONE_THIRD, 1);
		}
	}

	/**
	*  Make a normal dagger.
	*/
	static public Weapon dagger () {
		return new Weapon("Dagger", new Dice(4), 0, 1);
	}
	
	/**
	*  Make a silver dagger.
	*/
	static public Weapon silverDagger () {
		return new Weapon("Dagger", new Dice(4), 0, 1, Material.Silver);
	}

	/**
	*  Make a possibly-magic sword.
	*/
	static public Weapon sword (int bonus) {
		return new Weapon("Sword", new Dice(8), ONE_THIRD, 1, bonus);
	}

	/**
	*  Make a torch.
	*/
	static public Weapon torch () {
		return new Weapon("Torch", new Dice(3), ONE_THIRD, 1, 0, 
			Material.Wood, EnergyType.Fire);
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		String s = getName();
		if (material == Material.Silver) {
			s = material + " " + s;		
		}
		if (magicBonus != 0) {
			s += " " + Dice.formatBonus(magicBonus);
		}
		return s;
	}
}
