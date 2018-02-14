import java.io.*; 
import java.util.*;

/******************************************************************************
*  Treasure based on monster type association (Vol-2, p. 22).
*  Note that this should be used for wilderness encounters only.
*  (For clarification of that point, see AD&D MM, p. 5.)
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-10-15
*  @version  1.1
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
	private ArrayList<TreasureType> treasureTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected MonsterTreasureTable () throws IOException {
		treasureTable = new ArrayList<TreasureType>();
		String[][] table = CSVReader.readFile(MONSTER_TREASURE_FILE);
		for (int i = 1; i < table.length; i++) {
			treasureTable.add(new TreasureType(table[i]));
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
			if (tt.letterCode == code) {
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
		if (tt != null) {		
			int total = 0;
			Dice pct = new Dice(100);
			if (pct.roll() <= tt.copperPct)
				total += tt.copperDice.roll() * 1000 / 50;
			if (pct.roll() <= tt.silverPct)
				total += tt.silverDice.roll() * 1000 / 10;
			if (pct.roll() <= tt.goldPct)
				total += tt.goldDice.roll() * 1000;
			if (pct.roll() <= tt.gemsPct)
				total += tt.gemsDice.roll() 
					* GemsAndJewelry.randomGemValue();
			if (pct.roll() <= tt.jewelryPct) {
				int number = tt.jewelryDice.roll();
				Dice valueClass = 
					GemsAndJewelry.randomJewelryClass(); 
				for (int i = 0; i < number; i++) {
					total += valueClass.roll();			
				}
			}
			return total;
		}
		else return 0;
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
			char code = tt.letterCode;
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
			char code = tt.letterCode;
			for (int j = 0; j < SAMPLE_SIZE; j++) {
				total += table.randomValueByCode(code);
			}
			System.out.println(code + ": " + total/SAMPLE_SIZE);
		}
		System.out.println();
	}
}

