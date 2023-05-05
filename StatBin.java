import java.util.List;
import java.util.ArrayList;

/**
	Statistical bin for a given group of characters.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2014-07-01
*/

public class StatBin {
	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** List of characters included. */
	private List<Character> bin; 

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor.
	*/
	public StatBin() {
		bin = new ArrayList<Character>();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Add a new character into these stats.
	*/
	public void addCharacter(Character c) {
		bin.add(c);
	}

	/**
		Get size of the bin.
	*/
	public int size() { 
		return bin.size(); 
	}

	/**
		Compute mean hit points.
	*/
	public double getMeanHp() {
		long sum = 0;
		for (Character c: bin) {
			sum += c.getHitPoints();
		}
		return (double) sum / size();
	}

	/**
		Compute mean hit points rolled.
	*/
	public double getMeanHpRoll() {
		long sum = 0;
		for (Character c: bin) {
			sum += c.getHitPoints() 
				- c.getAbilityBonus(Ability.Constitution) * c.getLevel();
		}
		return (double) sum / size();
	}

	/**
		Compute mean age.
	*/
	public double getMeanAge() {
		long sum = 0;
		for (Character c: bin) {
			sum += c.getAge();
		}
		return (double) sum / size();
	}

	/**
		Compute mean ability score.
	*/
	public double getMeanAbility(Ability a) {
		long sum = 0;
		for (Character c: bin) {
			sum += c.getAbilityScore(a);
		}
		return (double) sum / size();
	}
}
