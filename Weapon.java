/******************************************************************************
*  Weapon on a character.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-17
*  @version  1.0
******************************************************************************/

public class Weapon extends Equipment {
	enum Material {Steel, Silver}

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** One-third of a stone weight. */
	static final float ONE_THIRD = Equipment.ONE_THIRD;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Material. */
	Material material;

	/** Base damage. */
	Dice baseDamage;

	/** Hands used. */
	int handsUsed;
	
	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (all fields).
	*/
	Weapon (String name, Dice baseDamage, float weight, int handsUsed, 
			int magicBonus, Material material) {
		super(name, weight, magicBonus); 
		this.baseDamage = baseDamage;
		this.handsUsed = handsUsed;
		this.material = material;
	}

	/**
	*  Constructor (no material).
	*/
	Weapon (String name, Dice baseDamage, float weight, int handsUsed, 
			int magicBonus) {
		this(name, baseDamage, weight, handsUsed, magicBonus, Material.Steel);
	}

	/**
	*  Constructor (name, damage, weight, hands).
	*/
	Weapon (String name, Dice baseDamage, float weight, int handsUsed) {
		this(name, baseDamage, weight, handsUsed, 0, Material.Steel);
	}

	/**
	*  Constructor (copy)
	*/
	Weapon (Weapon w) {
		this(w.name, new Dice(w.baseDamage), w.weight, w.handsUsed, 
			w.magicBonus, w.material);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public Dice getBaseDamage () { return baseDamage; }
	public Material getMaterial () { return material; }
	public int getHandsUsed () { return handsUsed; }
	
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
		return new Weapon("Dagger", new Dice(4), 0, 1, 0, Material.Steel);
	}
	
	/**
	*  Make a silver dagger.
	*/
	static public Weapon silverDagger () {
		return new Weapon("Dagger", new Dice(4), 0, 1, 0, Material.Silver);
	}

	/**
	*  Make a possibly-magic sword.
	*/
	static public Weapon sword (int bonus) {
		return new Weapon("Sword", new Dice(8), ONE_THIRD, 1, bonus, Material.Steel);
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		String s = getName();
		if (material != Material.Steel) {
			s = material + " " + s;		
		}
		if (magicBonus != 0) {
			s += " " + Dice.formatBonus(magicBonus);
		}
		return s;
	}
	
}

