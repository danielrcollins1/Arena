/******************************************************************************
*  Attack routine specification (sword, claw, rock, etc.).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-20
******************************************************************************/

public class Attack {

	//--------------------------------------------------------------------------
	//  Constant
	//--------------------------------------------------------------------------

	/** Base damage die type. */
	private static final int BASE_DIE = 6;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name descriptor of this attack form. */
	String name;
	
	/** Attack bonus added to d20 hit rolls. */
	int bonus;
	
	/** Rate of attacks per round. */
	int rate;
	
	/** Damage dice on successful hit. */
	Dice damage;

	/** Energy type applicable to this attack. */
	EnergyType energy;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**	
	*  Constructor (full fields).
	*/
	Attack (String name, int rate, int bonus, Dice damage, EnergyType energy) {
		this.name = name;
		this.rate = rate;
		this.bonus = bonus;
		this.damage = damage;
		this.energy = energy;
	}

	/**	
	*  Constructor (name, rate, bonus, damage).
	*/
	Attack (String name, int rate, int bonus, Dice damage) {
		this(name, rate, bonus, damage, null);
	}

	/**	
	*  Constructor (rate, bonus, damage dice).
	*/
	Attack (int rate, int bonus, int damDice) {
		this(null, rate, bonus, new Dice(damDice, BASE_DIE), null);
	}

	/**	
	*  Constructor (bonus, damage dice).
	*/
	Attack (int bonus, int damDice) {
		this(null, 1, bonus, new Dice(damDice, BASE_DIE), null);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public String getName() { return name; }
	public int getBonus() { return bonus; }
	public int getRate() { return rate; }
	public Dice getDamage() { return damage; }
	public EnergyType getEnergy() { return energy; }
	public void setBonus (int bonus)  { this.bonus = bonus; }
	public void setRate (int rate)  { this.rate = rate; }

	/**
	*  Roll damage for successful hit.
	*/
	public int rollDamage () {

		// Some attacks do 0 damage (e.g., carrion crawler)
		if (damage.getNum() <= 0)
			return damage.boundRoll(0);

		// Everything else does minimum 1 point (even w/penalties)
		else
			return damage.boundRoll(1);
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString () {
		return (rate == 1 ? "" : rate + " ")
			+ (name == null ? "Attack" : name) + " "
			+ Dice.formatBonus(bonus)
			+ " (" + damage + ")";
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {
		Attack atk = new Attack(2, 1);
		System.out.println(atk);
		atk = new Attack("Claw", 2, 6, new Dice(1, 6));
		System.out.println(atk);
	}
}
