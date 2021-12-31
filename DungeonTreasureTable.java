import java.io.IOException; 

/******************************************************************************
*  Treasure based on underworld level beneath surface (Vol-3, p. 7). 
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-11-18
******************************************************************************/

public class DungeonTreasureTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	final String DUNGEON_TREASURE_FILE = "DungeonTreasure.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	private class TreasureRecord {
		int startLevel, silver, gold, gems, jewelry, magic;

		TreasureRecord (String[] s) {
			startLevel = Integer.parseInt(s[0]);
			silver = Integer.parseInt(s[1]);
			gold = Integer.parseInt(s[2]);
			gems = Integer.parseInt(s[3]);
			jewelry = Integer.parseInt(s[4]);
			magic = Integer.parseInt(s[5]);
		}
		
		public String toString () {
			return startLevel + ", " + silver + ", " + gold + ", "
				+ gems + ", " + jewelry + ", " + magic;
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static DungeonTreasureTable instance = null;
	
	/** Array of TreasureType records. */
	private TreasureRecord[] treasureTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected DungeonTreasureTable () throws IOException {
		String[][] table = CSVReader.readFile(DUNGEON_TREASURE_FILE);
		treasureTable = new TreasureRecord[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			treasureTable[i - 1] = new TreasureRecord(table[i]);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
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
	*  Get a treasure record by matching its level code.
	*/
	private TreasureRecord getRecordByLevel (int level) {
		for (int i = treasureTable.length - 1; i > -1; i--) {
			TreasureRecord tr = treasureTable[i];
			if (level >= tr.startLevel) {
				return tr;
			}
		}
		return null;
	}

	/**
	*  Get random value for one treasure record.
	*/
	private int randomValueByRecord (TreasureRecord tr) {
		int total = 0;
		total += tr.silver * Dice.roll(6) / 10;
		if (Dice.roll(100) <= 50)
			total += tr.gold * Dice.roll(6);
		Dice gemJewelryDice = (tr.gems < 40 ? 
			new Dice(1, 6) : new Dice(2, 6));
		if (Dice.roll(100) <= tr.gems) {
			int num = gemJewelryDice.roll();
			total += num * GemsAndJewelry.randomGemValue();
		}
		if (Dice.roll(100) <= tr.jewelry) {
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
	*  Generate random treasure based on level indicator.
	*  @param level Level beneath surface of dungeon
	*/
	public int randomValueByLevel (int level) {
		TreasureRecord tr = getRecordByLevel(level);
		return randomValueByRecord(tr);
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();		
		DungeonTreasureTable table = DungeonTreasureTable.getInstance();

		// Print treasure table
		System.out.println("Dungeon Treasure Table");
		for (TreasureRecord record: table.treasureTable) {
			System.out.println(record);		
		}
		System.out.println();

		// Random sample values
		System.out.println("Treasure Sample Values");
		for (TreasureRecord record: table.treasureTable) {
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
		final int SAMPLE_SIZE = 10000;
		for (TreasureRecord record: table.treasureTable) {
			long total = 0;
			int level = record.startLevel;
			for (int j = 0; j < SAMPLE_SIZE; j++) {
				total += table.randomValueByLevel(level);
			}
			System.out.println("Level " + level + ": " + total/SAMPLE_SIZE);
		}
		System.out.println();
	}
}

