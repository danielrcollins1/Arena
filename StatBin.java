import java.util.ArrayList;

/******************************************************************************
*  Statistical bin for a given group of characters.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-07-01
*  @version  1.02
******************************************************************************/

public class StatBin {
	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Array of characters included. */
	ArrayList<Character> bin; 

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	public StatBin () {
		bin = new ArrayList<Character>();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Add a new character into these stats.
	*/
	public void addCharacter (Character c) {
		bin.add(c);
	}

	/**
	*  Get size of the bin.
	*/
	public int size () { 
		return bin.size(); 
	}

	/**
	*  Square an integer.
	*/
	static long square (long i) {
		return i * i;	
	}

	/**
	*  Compute a mean from num and sum.
	*/
	double mean (int num, long sum) {
		return (double) sum/num;	
	}

	/**
	*  Compute mean hit points.
	*/
	int getMeanHp () {
		long sum = 0;
		for (Character c: bin)
			sum += c.getHitPoints();
		return (int)(sum/size());
	}

	/**
	*  Compute mean age.
	*/
	int getMeanAge () {
		long sum = 0;
		for (Character c: bin)
			sum += c.getAge();
		return (int)(sum/size());
	}

	/**
	*  Compute mean ability score.
	*/
	int getMeanAbility (Ability a) {
		long sum = 0;
		for (Character c: bin)
			sum += c.getAbilityScore(a);
		return (int)(sum/size());
	}
}

