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
				System.out.println("Failed to read the MonsterDatabase file.");
			}
		}
		return instance;
	}

	/**
	*	Return iterator for the iterable interface.
	*/
	public Iterator<Monster> iterator() {        
		return monsterList.iterator();
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
	*  Set an alternate monster database filename.
	*/
	public static void setDatabaseFilename (String filename) {
		monsterFile = filename;	
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();		
		MonsterDatabase db = MonsterDatabase.getInstance();
		for (Monster m: db) {
			System.out.println(m);
		}
		System.out.println();
	}
}

