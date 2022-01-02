import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/******************************************************************************
*  Dice group for random rolls.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-20
******************************************************************************/

public class Dice {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Number of dice. */
	private int number;

	/** Sides on the dice. */
	private int sides;

	/** Multiplier to the dice (negative = divisor). */
	private int multiplier;

	/** Addition to the dice (negative = subtraction). */
	private int addition;

	/** 
	*  Random number generator. 
	*  Maintaining our own generator is more efficient than calls to
	*  the Math routine in a dice-heavy program; (1) we avoid checks
	*  for null random; (2) we can call nextInt directly and avoid 
	*  conversion back-and-forth with double.
	*/
	private static Random random = null;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/** 
	*  Constructor (one die only).
	*/
	Dice (int sides) {
		this(1, sides, 1, 0);
	}

	/** 
	*  Constructor (number, sides).
	*/
	Dice (int num, int sides) {
		this(num, sides, 1, 0);
	}

	/** 
	*  Constructor (number, sides, addition).
	*/
	Dice (int num, int sides, int add) {
		this(num, sides, 1, add);
	}

	/** 
	*  Constructor (all fields).
	*/
	Dice (int number, int sides, int mul, int add) {
		this.number = number;
		this.sides = sides;
		this.multiplier = mul;
		this.addition = add; 
	}

	/** 
	*  Constructor (read from string descriptor).
	*
	*  RegEx code from @user1803551 on StackExchange:
	*    http://stackoverflow.com/questions/35020687/
	*  how-to-parse-dice-notation-with-a-java-regular-expression
	*/
	Dice (String s) {
		this(0, 0, 1, 0);
		Pattern p = Pattern.compile(
			"([1-9]\\d*)?d([1-9]\\d*)([/x][1-9]\\d*)?([+-]\\d+)?");
		Matcher m = p.matcher(s);
		if(m.matches()) {
			number = (m.group(1) != null ? Integer.parseInt(m.group(1)) : 1);
			sides = Integer.parseInt(m.group(2));
			if (m.group(3) != null) {
				boolean positive = m.group(3).startsWith("x");
				int val = Integer.parseInt(m.group(3).substring(1));
				multiplier = positive ? val : -val;
			}
			if (m.group(4) != null) {
				boolean positive = m.group(4).startsWith("+");
				int val = Integer.parseInt(m.group(4).substring(1));
				addition = positive ? val : -val;
			}
		}
		else {
			// Accept a constant number (no "d")
			addition = Integer.parseInt(s);
		}
	}

	/**
	*  Constructor (copy).
	*/
	public Dice (Dice d) {
		this.number = d.number;
		this.sides = d.sides;
		this.multiplier = d.multiplier;
		this.addition = d.addition; 
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Basic accessors
	public int getNum () { return number; }
	public int getSides () { return sides; }
	public int getMul () { return multiplier; }
	public int getAdd () { return addition; }

	// Basic mutators
	public void setNum (int num) { number = num; }
	public void setSides (int sides) { this.sides = sides; }
	public void setMul (int mul) { multiplier = mul; }
	public void setAdd (int add) { addition = add; }

	/** 
	*  Initialize the dice random generator.
	*  Must call this before any roll() methods.
	*/
	public static void initialize () {
		random = new Random();
	}

	/** 
	*  Roll one die from a static context.
	*  @return The die-roll.
	*/
	public static int roll (int sides) {
		return random.nextInt(sides) + 1;
	}

	/** 
	*  Flip a coin from a static context.
	*  @return true if coin is heads
	*/
	public static boolean coinFlip () {
		return random.nextInt(2) == 0;	
	}

	/** 
	*  Roll percentile dice from a static context.
	*  @return The die-roll.
	*/
	public static int rollPct () {
		return random.nextInt(100) + 1;
	}

	/** 
	*  Apply adjustments after raw dice roll.
	*  @return Roll after modifiers.
	*/
	private int adjustRoll (int roll) {
		if (multiplier >= 0)
			roll *= multiplier;
		else {
			roll = (roll - 1)/(-multiplier) + 1;
		}   
		roll += addition;
		return roll;
	}

	/** 
	*  Rolls the dice.
	*  @return The dice-roll.
	*/
	public int roll () {
		int total = 0;
		for (int i = 0; i < number; i++) {
			total += roll(sides);
		}
		return adjustRoll(total);
	}

	/** 
	*  Rolls the dice with specified floor.
	*  @return The bounded dice-roll.
	*/
	public int boundRoll (int floor) {
		return Math.max(roll(), floor);
	}

	/** 
	*  Compute the minimum possible roll.
	*  @return Minimum possible roll.
	*/
	public int minRoll () {
		return adjustRoll(number);
	}

	/** 
	*  Compute the maximum possible roll.
	*  @return Maximum possible roll.
	*/
	public int maxRoll () {
		return adjustRoll(number * sides);
	}

	/** 
	*  Compute average roll.
	*  @return Average roll.
	*/
	public int avgRoll () {
		return (minRoll() + maxRoll())/2;
	}

	/** 
	*  Modify the addition field.
	*/
	public void modifyAdd (int mod) {
		addition += mod;	
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		if (number > 0) {
			String s = number + "d" + sides;
			if (multiplier != 1) {
				s += formatMultiplier(multiplier);
			}
			if (addition != 0) {
				s += formatBonus(addition);
			}
			return s;   
		}
		else {
			return "" + addition;  
		}
	}

	/**
	*  Format additive bonus with sign. 
	*/
	public static String formatBonus (int bonus) {
		return bonus >= 0 ? "+" + bonus : "" + bonus;
	}

	/**
	*  Format multiplicative bonus with sign. 
	*/
	public static String formatMultiplier (int mult) {
		return mult >= 0 ? "x" + mult : "/" + (-mult);
	}

	/**
	*  Test case for one dice object.
	*/
	private void test () {
		System.out.print(this + ": Min " + minRoll() 
			+ ", Max " + maxRoll() + ", Avg " + avgRoll() + ", Sample ");
		for (int i = 0; i < 10; i++) {
			System.out.print(roll() + " ");
		}
		System.out.println();
	}

	/**
	*  Main test function.
	*/
	public static void main (String[] args) {
		Dice.initialize();

		// Test various dice
		System.out.println("Test Various Dice");
		new Dice("d6").test();
		new Dice("2d6").test();
		new Dice("3d6+1").test();
		new Dice("4d10x10").test();
		new Dice("2d6x3-5").test();
		new Dice("d6/2").test();
		new Dice("2").test();
		System.out.println();

		// Test the division operator for bias
		System.out.println("Test d6/2 for Bias");
		Dice d = new Dice("1d6/2");
		int numOptions = 3;
		int[] count = new int[numOptions];
		int numRolls = 10000;
		for (int i = 0; i < numRolls; i++) {
			count[d.roll() - 1]++;
		}
		for (int i = 0; i < numOptions; i++) {
			System.out.println("Ratio of " + (i+1) + "'s: "
				+ (double) count[i]/numRolls);
		}
		System.out.println();
	}
}
