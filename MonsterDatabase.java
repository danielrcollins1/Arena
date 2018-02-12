import java.io.*; 
import java.util.*;

/******************************************************************************
*  Database of available monster types (singleton pattern).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-07-18
*  @version  1.0
******************************************************************************/

public class MonsterDatabase implements Iterable<Monster> {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with monster information. */
	final String MONSTER_FILE = "MonsterDatabase.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static MonsterDatabase instance = null;
	
	/** Array of Monster records. */
	ArrayList<Monster> monsterList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected MonsterDatabase () throws IOException {
		monsterList = new ArrayList<Monster>();
		String[][] table = CSVReader.readFile(MONSTER_FILE);
		for (int i = 1; i < table.length; i++) {
			monsterList.add(new Monster(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public int size() { return monsterList.size(); }
	public Monster get (int index) { return monsterList.get(index); }

	/**
	*  Access the singleton class instance.
	*/
	public static MonsterDatabase getInstance() {
		if (instance == null) {
			try {
				instance = new MonsterDatabase();
			}
			catch (IOException e) {
				System.out.println("Failed to read the MonsterDatabase file.");
			}
		}
		return instance;
	}

	/**
	*  Get a monster by matching its race.
	*/
	public Monster getByRace (String race) {
		for (Monster m: this) {
			if (m.getRace().equalsIgnoreCase(race)) {
				return m;
			}
		}
		return null;
	}

	/**
	*	Return iterator for the iterable interface.
	*/
	public Iterator<Monster> iterator() {        
		return monsterList.iterator();
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();		
		MonsterDatabase db = MonsterDatabase.getInstance();
		System.out.println();
		for (Monster m: db) {
			System.out.println(m);
		}
		System.out.println();
	}
}

