import java.io.IOException; 
import java.util.*;

/******************************************************************************
*  Tables of monsters organized by level.
*  For use in generating & data-tracking random encounters.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-07-18
******************************************************************************/

public class MonsterTables implements Iterable<List<Monster>> {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with monster information. */
	final String MONSTER_LEVEL_MATRIX_FILE = "MonsterLevelMatrix.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static MonsterTables instance = null;

	/** Matrix of what level monster appears. */
	List<List<Integer>> monsterLevelMatrix;

	/** Tables of monsters at each level. */
	List<List<Monster>> monsterTables;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

 	/**
 	*  Constructor (read from dedicated file).
 	*/
 	protected MonsterTables () throws IOException {
		readMonsterLevelMatrix();
		compileMonsterTables();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static MonsterTables getInstance() {
		if (instance == null) {
			try {
				instance = new MonsterTables();
			}
			catch (IOException e) {
				System.out.println("Failed to read the Monster Level Matrix file.");
			}
		}
		return instance;
	}

	/**
	*	Return iterator for the iterable interface.
	*/
	public Iterator<List<Monster>> iterator() {
		return monsterTables.iterator();
	}

	/**
	*  Read in the monster level matrix.
	*/
	private void readMonsterLevelMatrix () throws IOException {
		String[][] table = CSVReader.readFile(MONSTER_LEVEL_MATRIX_FILE);
		monsterLevelMatrix = new ArrayList<List<Integer>>();
		for (int i = 1; i < table.length; i++) {
			List<Integer> thisRow = new ArrayList<Integer>();
			for (int j = 0; j < table[i].length; j++) {
				thisRow.add(CSVReader.parseInt(table[i][j]));
			}
			monsterLevelMatrix.add(thisRow);
		}
	}

	/**
	*  Compile the table of monsters at each level.
	*/
	private void compileMonsterTables () {

		// Initialize the tables
		int maxLevel = monsterLevelMatrix.get(0).size() - 1;
		monsterTables = new ArrayList<List<Monster>>(maxLevel);
		for (int i = 0; i < maxLevel; i++) {
			 monsterTables.add(new ArrayList<Monster>());		
		}		
		
		// Sort monster database into these tables
		for (Monster monster: MonsterDatabase.getInstance()) {
			if (monster.getEnvironment() == 'D') {
				int tableLevel = EHDToTables.getInstance()
					.mapEHDToTable(monster.getEHD());
				if (1 <= tableLevel && tableLevel <= maxLevel) {
					monsterTables.get(tableLevel - 1).add(monster);
				}		
			}
		}
	}

	/**
	*  Get the number of tables (i.e., max monster level).
	*/
	public int getNumTables () {
		return monsterTables.size();	
	}

	/**
	*  Access one of the monster tables (1-based).
	*/
	public List<Monster> getTable (int level) {
		return monsterTables.get(level - 1);
	}

	/**
	*  Roll a new random monster on a given dungeon level.
	*/
	public Monster randomMonsterByDungeonLevel (int dungeonLevel) {
		int monsterLevel = randomMonsterLevel(dungeonLevel);
		return randomMonsterByTableLevel(monsterLevel);
	}

	/**
	*  Roll a new random monster of a given table level.
	*/
	private Monster randomMonsterByTableLevel (int tableLevel) {
		List<Monster> table = getTable(tableLevel);
		if (table.isEmpty()) {
			System.err.println("Empty monster table! (level " 
				+ tableLevel + ")");		
			return null;		
		}
		else {
			int idx = Dice.roll(table.size()) - 1;
			return table.get(idx).spawn();
		}
	}

	/**
	*  Roll a random monster level for a given dungeon level.
	*/
	private int randomMonsterLevel (int dungeonLevel) {
		int roll = new Dice(6).roll();
		List<Integer> matrixRow = getMatrixRow(dungeonLevel);
		for (int level = matrixRow.size() - 1; level > 0; level--) {
			int minRoll = matrixRow.get(level);
			if (minRoll != 0 && minRoll <= roll) 
				return level;
		}
		return -1;
	}

	/**
	*  Convert dungeon level to row in matrix.
	*/
	private List<Integer> getMatrixRow (int dungeonLevel) {
		for (int row = monsterLevelMatrix.size() - 1; row > -1; row--) {
			if (dungeonLevel >= monsterLevelMatrix.get(row).get(0))
				return monsterLevelMatrix.get(row);
		}
		return null;
	}

	/**
	*  Get total kills for a given monster table.
	*/
	public int getTotalKillsAtLevel (int tableLevel) {
		int total = 0;
		for (Monster m: getTable(tableLevel)) {
			total += m.getKillTally();
		}
		return total;
	}

	/**
	*  Get grand total monster kills.
	*/
	public int getGrandTotalKills () {
		int grandTotal = 0;
		for (int i = 1; i <= getNumTables(); i++)	{
			grandTotal += getTotalKillsAtLevel(i);
		}
		return grandTotal;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		MonsterTables tables = MonsterTables.getInstance();
		for (int level = 1; level <= tables.getNumTables(); level++) {
			System.out.println("Level " + level + " Monsters");
			for (Monster m: tables.getTable(level)) {
				System.out.println(m);
			}
			System.out.println();			
		}
	}
}

