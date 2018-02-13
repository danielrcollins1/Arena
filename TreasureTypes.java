import java.io.*; 
import java.util.*;

/******************************************************************************
*  List of available treasure types.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-10-15
*  @version  1.0
******************************************************************************/

public class TreasureTypes {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	final String TREASURE_TYPES_FILE = "TreasureTypes.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	public class TreasureType {
		char code;
		Dice copperDice, silverDice, goldDice, gemsDice, jewelryDice;
		int copperPct, silverPct, goldPct, gemsPct, jewelryPct;
	
		TreasureType (String[] s) {
			code = s[0].charAt(0);
			copperDice = parseDice(s[1]);
			copperPct = CSVReader.parseInt(s[2]);
			silverDice = parseDice(s[3]);
			silverPct = CSVReader.parseInt(s[4]);
			goldDice = parseDice(s[5]);
			goldPct = CSVReader.parseInt(s[6]);
			gemsDice = parseDice(s[7]);
			gemsPct = CSVReader.parseInt(s[8]);
			jewelryDice = parseDice(s[9]);
			jewelryPct = CSVReader.parseInt(s[10]);
		}	
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static TreasureTypes instance = null;
	
	/** Array of TreasureType records. */
	ArrayList<TreasureType> treasureTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected TreasureTypes () throws IOException {
		treasureTable = new ArrayList<TreasureType>();
		String[][] table = CSVReader.readFile(TREASURE_TYPES_FILE);
		for (int i = 1; i < table.length; i++) {
			treasureTable.add(new TreasureType(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public int size() { return treasureTable.size(); }
	public TreasureType get (int index) { return treasureTable.get(index); }

	/**
	*  Access the singleton class instance.
	*/
	public static TreasureTypes getInstance() {
		if (instance == null) {
			try {
				instance = new TreasureTypes();
			}
			catch (IOException e) {
				System.out.println("Failed to read the TreasureTypes file.");
			}
		}
		return instance;
	}

	/**
	*  Parse a value dice description.
	*/
	private Dice parseDice (String s) {
		return s.equals("-") ? null : new Dice(s);
	}

	/**
	*  Get a treasure type by matching its character code.
	*/
	public TreasureType getByCode (char code) {
		for (int i = 0; i < size(); i++) {
			TreasureType tt = get(i);
			if (tt.code == code) {
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
		TreasureTypes tts = TreasureTypes.getInstance();

		// Random sample values
		System.out.println("Treasure Sample Values");
		for (int i = 0; i < tts.size(); i++) {
			char code = tts.get(i).code;
			System.out.print(code + ": ");
			for (int j = 0; j < 5; j++) {
				System.out.print(tts.randomValueByCode(code) + " ");			
			}
			System.out.println();
		}
		System.out.println();
		
		// Estimated average values
		System.out.println("Estimated Average Values");
		final int SAMPLE_SIZE = 10000;
		for (int i = 0; i < tts.size(); i++) {
			char code = tts.get(i).code;
			long total = 0;
			for (int j = 0; j < SAMPLE_SIZE; j++) {
				total += tts.randomValueByCode(code);
			}
			System.out.println(code + ": " + total/SAMPLE_SIZE);
		}
		System.out.println();
	}
}

