import java.io.*; 
import java.util.*;

/******************************************************************************
*  List of treasure by level (Vol-3, p. 7).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-11-18
*  @version  1.0
******************************************************************************/

public class TreasureByLevel {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	final String TREASURE_LEVEL_FILE = "TreasureByLevel.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	public class TreasureRecord {
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
	static TreasureByLevel instance = null;
	
	/** Array of TreasureType records. */
	ArrayList<TreasureRecord> treasureTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected TreasureByLevel () throws IOException {
		treasureTable = new ArrayList<TreasureRecord>();
		String[][] table = CSVReader.readFile(TREASURE_LEVEL_FILE);
		for (int i = 1; i < table.length; i++) {
			treasureTable.add(new TreasureRecord(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public int size() { return treasureTable.size(); }
	public TreasureRecord get (int index) { return treasureTable.get(index); }

	/**
	*  Access the singleton class instance.
	*/
	public static TreasureByLevel getInstance() {
		if (instance == null) {
			try {
				instance = new TreasureByLevel();
			}
			catch (IOException e) {
				System.out.println("Failed to read the TreasureByLevel file.");
			}
		}
		return instance;
	}

	/**
	*  Get a treasure record by matching its level code.
	*/
	public TreasureRecord getRecordByLevel (int level) {
		for (int i = size() - 1; i > -1; i--) {
			TreasureRecord tr = treasureTable.get(i);
			if (level >= tr.startLevel) {
				return tr;
			}
		}
		return null;
	}

	/**
	*  Get random value for one treasure record.
	*/
	public int randomValueByRecord (TreasureRecord tr) {
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
		TreasureByLevel tbl = TreasureByLevel.getInstance();

		// Print treasure types table
		System.out.println("Treasure By Level Table");
		for (int i = 0; i < tbl.size(); i++) {
			System.out.println(tbl.get(i));
		}
		System.out.println();

		// Random sample values
		System.out.println("Treasure Sample Values");
		for (int level = 1; level < 16; level++) {
			int value = tbl.randomValueByLevel(level);
			System.out.println("Level " + level + ": " + value + " gp");			
		}
		System.out.println();
		
		// Estimated average values
		System.out.println("Estimated Average Values");
		final int SAMPLE_SIZE = 10000;
		for (int level = 1; level < 16; level++) {
			long total = 0;
			for (int j = 0; j < SAMPLE_SIZE; j++) {
				total += tbl.randomValueByLevel(level);
			}
			System.out.println("Level " + level + ": " + total/SAMPLE_SIZE);
		}
		System.out.println();
	}
}

