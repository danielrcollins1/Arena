/******************************************************************************
*  Attack routine specification (sword, claw, rock, etc.).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-20
*  @version  1.0
******************************************************************************/

public class Attack {
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

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**	
	*  Constructor (full fields).
	*/
	Attack (String name, int rate, int bonus, Dice damage) {
		this.name = name;
		this.rate = rate;
		this.bonus = bonus;
		this.damage = damage;
	}

	/**	
	*  Constructor (rate, bonus, damage dice).
	*/
	Attack (int rate, int bonus, int damDice) {
		this(null, rate, bonus, new Dice(damDice, 6));
	}

	/**	
	*  Constructor (bonus, damage dice).
	*/
	Attack (int bonus, int damDice) {
		this(null, bonus, 1, new Dice(damDice, 6));
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public String getName() { return name; }
	public int getBonus() { return bonus; }
	public int getRate() { return rate; }
	public Dice getDamage() { return damage; }
	public void setBonus (int bonus)  { this.bonus = bonus; }
	public void setRate (int rate)  { this.rate = rate; }

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
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

