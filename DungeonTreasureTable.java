import java.io.IOException; 

/**
	Treasure based on underworld level beneath surface (Vol-3, p. 7).

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2017-11-18
*/

public class DungeonTreasureTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	private static final String DUNGEON_TREASURE_FILE = "DungeonTreasure.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/**
		Parameters for treasure in one row of the table.
		In some cases these are base amounts, in others chance of appearing.
	*/
	private class TreasureParams {
	
		/** Starting dungeon level for this row. */
		private int startLevel;
		
		/** Base amount of silver. */
		private int silverAmount;

		/** Base amount of gold. */
		private int goldAmount;
		
		/** Chance of gems. */
		private int gemsChance;

		/** Chance of jewelry. */
		private int jewelryChance;

		/** Chance of magic. */
		private int magicChance;

		/** Constructor. */
		private TreasureParams(String[] s) {
			startLevel = Integer.parseInt(s[0]);
			silverAmount = Integer.parseInt(s[1]);
			goldAmount = Integer.parseInt(s[2]);
			gemsChance = Integer.parseInt(s[3]);
			jewelryChance = Integer.parseInt(s[4]);
			magicChance = Integer.parseInt(s[5]);
		}
		
		/** String representation. */
		public String toString() {
			return startLevel + ", " + silverAmount + ", " + goldAmount + ", "
				+ gemsChance + ", " + jewelryChance + ", " + magicChance;
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static DungeonTreasureTable instance = null;
	
	/** Array of TreasureType records. */
	private TreasureParams[] treasureTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected DungeonTreasureTable() throws IOException {
		String[][] table = CSVReader.readFile(DUNGEON_TREASURE_FILE);
		treasureTable = new TreasureParams[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			treasureTable[i - 1] = new TreasureParams(table[i]);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
	*/
	public static DungeonTreasureTable getInstance() {
		if (instance == null) {
			try {
				instance = new DungeonTreasureTable();
			}
			catch (IOException e) {
				System.out.println("Failed to read the Dungeon Treasure file.");
			}
		}
		return instance;
	}

	/**
		Get a treasure record by matching its level code.
	*/
	private TreasureParams getRecordByLevel(int level) {
		for (int i = treasureTable.length - 1; i > -1; i--) {
			TreasureParams tr = treasureTable[i];
			if (level >= tr.startLevel) {
				return tr;
			}
		}
		return null;
	}

	/**
		Get random value for one treasure record.
	*/
	private int randomValueByRecord(TreasureParams params) {
		int total = 0;
		total += params.silverAmount * Dice.roll(6) / 10;
		if (Dice.roll(100) <= 50) {
			total += params.goldAmount * Dice.roll(6);
		}
		Dice gemJewelryDice = params.gemsChance < 40 
			? new Dice(1, 6) : new Dice(2, 6);
		if (Dice.roll(100) <= params.gemsChance) {
			int num = gemJewelryDice.roll();
			total += num * GemsAndJewelry.randomGemValue();
		}
		if (Dice.roll(100) <= params.jewelryChance) {
			int num = gemJewelryDice.roll();
			Dice valueClass = 
				GemsAndJewelry.randomJewelryClass(); 
			for (int i = 0; i < num; i++) {
				total += valueClass.roll();
			}
		}
		return total;
	}

	/**
		Generate random treasure based on level indicator.
		@param level Level beneath surface of dungeon
	*/
	public int randomValueByLevel(int level) {
		TreasureParams tr = getRecordByLevel(level);
		return randomValueByRecord(tr);
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
		Dice.initialize();		
		DungeonTreasureTable table = DungeonTreasureTable.getInstance();

		// Print treasure table
		System.out.println("Dungeon Treasure Table");
		for (TreasureParams record: table.treasureTable) {
			System.out.println(record);		
		}
		System.out.println();

		// Random sample values
		System.out.println("Treasure Sample Values");
		for (TreasureParams record: table.treasureTable) {
			int level = record.startLevel;
			System.out.print(level + ": ");
			for (int j = 0; j < 6; j++) {
				int value = table.randomValueByLevel(level);
				System.out.print(value + " ");
			}
			System.out.println();
		}
		System.out.println();

		// Estimated average values
		System.out.println("Estimated Average Values");
		final int sampleSize = 10000;
		for (TreasureParams record: table.treasureTable) {
			long total = 0;
			int level = record.startLevel;
			for (int j = 0; j < sampleSize; j++) {
				total += table.randomValueByLevel(level);
			}
			System.out.println("Level " + level + ": " + total / sampleSize);
		}
		System.out.println();
	}
}
