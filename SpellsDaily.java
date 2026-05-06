import java.io.IOException;
import java.util.ArrayList;

/**
	Matrix of how many wizard spells can be memorized daily.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2018-12-05
*/

public class SpellsDaily {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with daily spells memorizable. */
	private static final String SPELLS_DAILY_FILE = "SpellsDaily.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static SpellsDaily instance = null;

	/** Table of daily spells memorizible. */
	private int[][] spellsDailyData;

	/** Maximum class level in matrix. */
	private int maxDataClassLevel;
	
	/** Maximum spell level. */
	private int maxSpellLevel;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read error
	*/
	protected SpellsDaily() throws IOException {
		String[][] table = CSVReader.readFile(SPELLS_DAILY_FILE);
		maxDataClassLevel = table.length - 1;
		maxSpellLevel = table[0].length - 1;
		spellsDailyData = new int[maxDataClassLevel][maxSpellLevel];
		for (int i = 1; i <= maxDataClassLevel; i++) {
			for (int j = 1; j <= maxSpellLevel; j++) {
				spellsDailyData[i - 1][j - 1] = CSVReader.parseInt(table[i][j]);
			}
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
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
		Get maximum spell level.
	*/
	public int getMaxSpellLevel() {
		return maxSpellLevel;
	}

	/**
		Get spells memorizable at class and spell level.
	*/
	public int getSpellsAtLevel(int wizLevel, int spellLevel) {
		assert 1 <= spellLevel && spellLevel <= maxSpellLevel;

		// Before matrix data
		if (wizLevel < 1) {
			return 0;
		}

		// Inside matrix data
		else if (wizLevel <= maxDataClassLevel) {
			return spellsDailyData[wizLevel - 1][spellLevel - 1];
		}

		// After matrix data (pattern per Vol-1, p. 19)
		else { 
			int spellsAtMatrixEnd = 
				spellsDailyData[maxDataClassLevel - 1][spellLevel - 1];
			int levelsPastMatrix = wizLevel - maxDataClassLevel;
			int spells = spellsAtMatrixEnd + levelsPastMatrix / 2;
			if (wizLevel % 2 == 1 && spellLevel <= maxSpellLevel / 2) {
				spells++;
			}
			return spells;			
		}
	}

	/**
		Get a list of memorizable spell counts.
	*/
	public ArrayList<Integer> getSpellCounts(int wizLevel) {
		ArrayList<Integer> spells = new ArrayList<Integer>();
		for (int level = 1; level <= maxSpellLevel; level++) {
			int count = getSpellsAtLevel(wizLevel, level);
			if (count > 0) {
				spells.add(count);			
			}
		}
		return spells;
	}
	
	/**
		Get a list of memorizable spell counts as a string.
	*/
	public String getSpellCountString(int wizLevel) {
		ArrayList<Integer> spells = getSpellCounts(wizLevel);
		String s = "" + spells.get(0);
		for (int i = 1; i < spells.size(); i++) {
			s += "/" + spells.get(i);
		}
		return s;
	}
	
	/**
		Main test function.
	*/
	public static void main(String[] args) {	
		Dice.initialize();
		SpellsDaily matrix = SpellsDaily.getInstance();
		System.out.println("Spells Daily:");
		for (int level = 1; level <= 30; level++) {
			System.out.println("Level " + level
				+ ": " + matrix.getSpellCountString(level));
		}
		System.out.println();
	}
}
