import java.util.*;
import java.io.IOException; 

/******************************************************************************
*  Database of available monster types (singleton pattern).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-07-18
******************************************************************************/

public class MonsterDatabase implements Iterable<Monster> {

 	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Default file with monster information. */
	final static String MONSTER_FILE_DEFAULT = "MonsterDatabase.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of file with monster information. */
	static String monsterFile = MONSTER_FILE_DEFAULT;

	/** The singleton class instance. */
	static MonsterDatabase instance = null;
	
	/** List of Monster records. */
	List<Monster> monsterList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected MonsterDatabase () throws IOException {
		String[][] table = CSVReader.readFile(monsterFile);
		monsterList = new ArrayList<Monster>(table.length - 1);
		for (int i = 1; i < table.length; i++) {
			monsterList.add(new Monster(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static MonsterDatabase getInstance() {
		if (instance == null) {
			try {
				instance = new MonsterDatabase();
			}
			catch (IOException e) {
				System.err.println("Failed to read monster database file.");
			}
		}
		return instance;
	}

	/**
	*	Implement the iterable interface.
	*/
	public Iterator<Monster> iterator() {        
		return monsterList.iterator();
	}

	/**
	*  Get a monster by matching its race.
	*/
	public Monster getByRace (String race) {
		for (Monster m: monsterList) {
			if (m.getRace().equalsIgnoreCase(race)) {
				return m;
			}
		}
		System.err.println("Failed to find monster in database: " + race);
		return null;
	}

	/**
	*  Get a random monster from the database.
	*/
	public Monster getRandom () {
		int index = Dice.roll(monsterList.size()) - 1;
		return monsterList.get(index);
	}

	/**
	*  Set an alternate monster database filename.
	*/
	public static void setDatabaseFilename (String filename) {
		monsterFile = filename;	
	}

	/**
	*  Main test method.
	*
	*  Prints stat blocks for all monsters in database.
	*  Takes alternate database file from command line.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		if (args.length > 0) {
			setDatabaseFilename(args[0]);
		}
		MonsterDatabase db = getInstance();
		for (Monster m: db) {
			System.out.println(m);
		}
		System.out.println();
	}
}
