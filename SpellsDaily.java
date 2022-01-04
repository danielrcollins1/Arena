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
		assert(1 <= spellLevel && spellLevel <= maxSpellLevel);

		// Before matrix data
		if (classLevel < 1)
			return 0;

		// Inside matrix data
		else if (classLevel <= maxDataClassLevel)
			return spellsDailyData[classLevel - 1][spellLevel - 1];

		// After matrix data (pattern per Vol-1, p. 19)
		else { 
			int spellsAtMatrixEnd = 
				spellsDailyData[maxDataClassLevel - 1][spellLevel - 1];
			int levelsPastMatrix = classLevel - maxDataClassLevel;
			int spells = spellsAtMatrixEnd + levelsPastMatrix / 2;
			if (classLevel % 2 == 1 && spellLevel <= maxSpellLevel / 2) spells++;
			return spells;			
		}
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
