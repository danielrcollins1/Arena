import java.io.IOException; 

/******************************************************************************
*  Matrix of how many spells can be memorized daily.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-05
******************************************************************************/

public class SpellsDaily {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with daily spells memorizable. */
	final String SPELLS_DAILY_FILE = "SpellsDaily.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SpellsDaily instance = null;

	/** Table of daily spells memorizible. */
	int[][] spellsDailyData;

	/** Maximum class level in matrix. */
	int maxDataClassLevel;
	
	/** Maximum spell level. */
	int maxSpellLevel;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected SpellsDaily () throws IOException {
		String[][] table = CSVReader.readFile(SPELLS_DAILY_FILE);
		maxDataClassLevel = table.length - 1;
		maxSpellLevel = table[0].length - 1;
		spellsDailyData = new int[maxDataClassLevel][maxSpellLevel];
		for (int i = 1; i <= maxDataClassLevel; i++) {
			for (int j = 1; j <= maxSpellLevel; j++) {
				spellsDailyData[i-1][j-1] = CSVReader.parseInt(table[i][j]);
			}
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SpellsDaily getInstance() {
		if (instance == null) {
			try {
				instance = new SpellsDaily();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Spells Daily file.");
			}
		}
		return instance;
	}

	/**
	*  Get maximum spell level.
	*/
	public int getMaxSpellLevel () {
		return maxSpellLevel;
	}

	/**
	*  Get spells memorizable at class and spell level.
	*/
	public int getSpellsDaily (int classLevel, int spellLevel) {
		if (spellLevel < 1 || maxSpellLevel < spellLevel) // Error
			return 0;
		else if (classLevel < 1) // Before matrix
			return 0;
		else if (classLevel <= maxDataClassLevel) // In matrix
			return spellsDailyData[classLevel - 1][spellLevel - 1];
		else // After matrix (as per Vol-1, p. 19). 
			return getSpellsDaily(classLevel - 2, spellLevel) + 1;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		SpellsDaily matrix = SpellsDaily.getInstance();
		System.out.println("Spells Daily:");
		for (int i = 1; i <= 30; i++) {
			System.out.print("Level " + i + ": ");
			for (int j = 1; j <= matrix.getMaxSpellLevel(); j++) {
				System.out.print(matrix.getSpellsDaily(i, j) + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
}
