import java.util.Map;
import java.util.TreeMap;

/******************************************************************************
*  List of special abilities found in database but not recognized.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-07-31
******************************************************************************/

public class SpecialUnknownList {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SpecialUnknownList instance = null;
	
	/** Associated array of item records. */
	Map<String, Integer> unknownArray;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	protected SpecialUnknownList () {
		unknownArray = new TreeMap<String, Integer>();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SpecialUnknownList getInstance() {
		if (instance == null) {
			instance = new SpecialUnknownList();
		}
		return instance;
	}

	/**
	*  Record an unhandled name.
	*/
	public void recordName (String name) {
		Integer count = unknownArray.get(name);
		count = (count == null ? 1 : ++count);
		unknownArray.put(name, count);
	}

	/**
	*  Convert to a string (in decreasing value order)
	*/
	public String toString () {
		return MapUtil.entriesSortedByValues(unknownArray, true).toString();
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();		
		MonsterDatabase db = MonsterDatabase.getInstance();
		SpecialUnknownList list = SpecialUnknownList.getInstance();
		System.out.println(list);
	}
}

