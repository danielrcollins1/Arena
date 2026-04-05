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
		private int amtSilver;

		/** Base amount of gold. */
		private int amtGold;
		
		/** Percent chance of gems & jewelry. */
		private int pctGemsJewelry;

		/** Percent chance of magic. */
		private int pctMagic;

		/** Constructor. */
		private TreasureParams(String[] s) {
			startLevel = Integer.parseInt(s[0]);
			amtSilver = Integer.parseInt(s[1]);
			amtGold = Integer.parseInt(s[2]);
			pctGemsJewelry = Integer.parseInt(s[3]);
			pctMagic = Integer.parseInt(s[4]);
		}
		
		/** String representation. */
		public String toString() {
			return startLevel + ", " + amtSilver + ", " + amtGold + ", "
				+ pctGemsJewelry + ", " + pctMagic;
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
		Get array index from level.
	*/
	private int getIndexFromLevel(int level) {
		for (int i = treasureTable.length - 1; i >= 0; i--) {
			if (level >= treasureTable[i].startLevel) {
				return i;
			}
		}
		return 0;
	}

	/**
		Roll random treasure for given dungeon level.
	*/
	private Treasure pvtRollTreasureForLevel(int level) {
		assert level > 0;
		Treasure treas = new Treasure();
		TreasureParams params = treasureTable[getIndexFromLevel(level)];
		treas.set(Treasure.Category.Silver, params.amtSilver * Dice.roll(6));
		if (Dice.rollPct() <= 50) {
			treas.set(Treasure.Category.Gold, params.amtGold * Dice.roll(6));
		}
		Dice numGemJewelry = params.pctGemsJewelry < 40
			? new Dice(1, 6) : new Dice(1, 12);
		if (Dice.rollPct() <= params.pctGemsJewelry) {
			treas.set(Treasure.Category.Gems, 
				numGemJewelry.roll() * GemsAndJewelry.randomGemValue());
		}
		if (Dice.rollPct() <= params.pctGemsJewelry) {
			treas.set(Treasure.Category.Jewelry, 
				numGemJewelry.roll() * GemsAndJewelry.randomJewelryValue());
		}
		if (Dice.rollPct() <= params.pctMagic) {
			treas.set(Treasure.Category.Magic, 1);
		}
		return treas;
	}

	/**
		Static random treasure for given dungeon level.
	*/
	public static Treasure rollTreasureForLevel(int level) {
		return getInstance().pvtRollTreasureForLevel(level);
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
				Treasure treas = rollTreasureForLevel(level);
				int value = treas.getValue();
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
				Treasure treas = rollTreasureForLevel(level);
				total += treas.getValue();
			}
			System.out.println("Level " + level + ": " + total / sampleSize);
		}
		System.out.println();
	}
}
