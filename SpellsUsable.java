import java.io.IOException; 

/******************************************************************************
*  Matrix of how many spells can be memorized.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-05
*  @version  1.0
******************************************************************************/

public class SpellsUsable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with spells usable. */
	final String SPELLS_USABLE_FILE = "SpellsUsable.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SpellsUsable instance = null;

	/** Table of spells usable. */
	int[][] spellsUsableData;

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
	protected SpellsUsable () throws IOException {
		String[][] table = CSVReader.readFile(SPELLS_USABLE_FILE);
		maxDataClassLevel = table.length - 1;
		maxSpellLevel = table[0].length - 1;
		spellsUsableData = new int[maxDataClassLevel][maxSpellLevel];
		for (int i = 1; i <= maxDataClassLevel; i++) {
			for (int j = 1; j <= maxSpellLevel; j++) {
				spellsUsableData[i-1][j-1] = CSVReader.parseInt(table[i][j]);
			}
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SpellsUsable getInstance() {
		if (instance == null) {
			try {
				instance = new SpellsUsable();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Spells Usable file.");
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
	*  Get spells usable at class and spell level.
	*/
	public int getSpellsUsable (int classLevel, int spellLevel) {
		if (spellLevel < 1 || maxSpellLevel < spellLevel) // Error
			return 0;
		else if (classLevel < 1) // Before matrix
			return 0;
		else if (classLevel <= maxDataClassLevel) // In matrix
			return spellsUsableData[classLevel - 1][spellLevel - 1];
		else // After matrix (as per Vol-1, p. 19). 
			return getSpellsUsable(classLevel - 2, spellLevel) + 1;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		SpellsUsable matrix = SpellsUsable.getInstance();
		System.out.println("Spells Usable:");
		for (int i = 1; i <= 30; i++) {
			System.out.print("Level " + i + ": ");
			for (int j = 1; j <= matrix.getMaxSpellLevel(); j++) {
				System.out.print(matrix.getSpellsUsable(i, j) + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
}

