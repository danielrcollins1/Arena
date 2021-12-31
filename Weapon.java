/******************************************************************************
*  Weapon on a character.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-17
******************************************************************************/

public class Weapon extends Equipment {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** One-third of a stone weight. */
	static final float ONE_THIRD = Equipment.ONE_THIRD;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

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
	Weapon (String name, Material material, float weight, int magic,
		Dice damage, EnergyType energy, int hands)
	{
		super(name, material, weight, magic); 
		this.damage = damage;
		this.energy = energy;
		this.hands = hands;
	}

	/**
	*  Constructor (name, damage, weight, hands).
	*/
	Weapon (String name, Dice damage, float weight, int hands) {
		this(name, Material.Steel, weight, 0, damage, null, hands);
	}

	/**
	*  Constructor (copy)
	*/
	Weapon (Weapon w) {
		this(w.name, w.material, w.weight, w.magicBonus,
			w.damage, w.energy, w.hands);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public Dice getBaseDamage () { return damage; }
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
		return new Weapon("Dagger", Material.Silver, 0, 0, 
			new Dice(4), null, 1);
	}

	/**
	*  Make a possibly-magic sword.
	*/
	static public Weapon sword (int bonus) {
		return new Weapon("Sword", Material.Steel, ONE_THIRD, bonus,
			new Dice(8), null, 1);
	}

	/**
	*  Make a torch.
	*/
	static public Weapon torch () {
		return new Weapon("Torch", Material.Wood, ONE_THIRD, 0,
			new Dice(3), EnergyType.Fire, 1);
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
