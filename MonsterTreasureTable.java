import java.io.IOException; 

/******************************************************************************
*  Treasure based on monster type association (Vol-2, p. 22).
*  Note that this should be used for wilderness encounters only.
*  (For clarification of that point, see AD&D MM, p. 5.)
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-10-15
******************************************************************************/

public class MonsterTreasureTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	final String MONSTER_TREASURE_FILE = "MonsterTreasure.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static MonsterTreasureTable instance = null;
	
	/** Array of TreasureType records. */
	private TreasureType[] treasureTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected MonsterTreasureTable () throws IOException {
		String[][] table = CSVReader.readFile(MONSTER_TREASURE_FILE);
		treasureTable = new TreasureType[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			treasureTable[i - 1] = new TreasureType(table[i]);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static MonsterTreasureTable getInstance() {
		if (instance == null) {
			try {
				instance = new MonsterTreasureTable();
			}
			catch (IOException e) {
				System.out.println("Failed to read the Monster Treasure file.");
			}
		}
		return instance;
	}


	/**
	*  Get a treasure type by matching its character code.
	*/
	private TreasureType getByCode (char code) {
		for (TreasureType tt: treasureTable) {
			if (tt.getKey() == code) {
				return tt;
			}
		}
		return null;
	}

	/**
	*  Get random treasure value by code.
	*/
	public int randomValueByCode (char code) {
		TreasureType tt = getByCode(code);
		return tt == null ? 0 : tt.randomValue();
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();		
		MonsterTreasureTable table = MonsterTreasureTable.getInstance();

		// Print treasure table
		System.out.println("Monster Treasure Table");
		for (TreasureType tt: table.treasureTable) {
			System.out.println(tt);		
		}
		System.out.println();

		// Random sample values
		System.out.println("Treasure Sample Values");
		for (TreasureType tt: table.treasureTable) {
			char code = tt.getKey();
			System.out.print(code + ": ");
			for (int j = 0; j < 6; j++) {
				System.out.print(table.randomValueByCode(code) + " ");
			}
			System.out.println();
		}
		System.out.println();
		
		// Estimated average values
		System.out.println("Estimated Average Values");
		final int SAMPLE_SIZE = 10000;
		for (TreasureType tt: table.treasureTable) {
			long total = 0;
			char code = tt.getKey();
			for (int j = 0; j < SAMPLE_SIZE; j++) {
				total += table.randomValueByCode(code);
			}
			System.out.println(code + ": " + total/SAMPLE_SIZE);
		}
		System.out.println();
	}
}

