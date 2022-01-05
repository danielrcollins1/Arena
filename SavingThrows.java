import java.io.IOException; 

/******************************************************************************
*  Saving throws table (singleton pattern).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-20
******************************************************************************/

public class SavingThrows {

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** Saving throw types. */
	public enum Type {
		Death, Wands, Stone, Breath, Spells;

		/** Total number of save types available. */
		public static final int length = Type.values().length;
	};

	/** Available saving throw rules. */
	private enum SaveRule {ODD, OED};

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Rule to use for adjudications. */
	static final SaveRule SAVE_RULE = SaveRule.OED;

	/** Name of file for ODD-style saves table. */
	static final String SAVING_THROWS_FILE = "SavingThrows.csv";

	/** OED-style adjustments per save type. */
	static final int[] OED_ADJUST = {4, 3, 2, 1, 0};

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Class to store save targets at one class level. */
	class SaveRecord {
		String className;
		int minLevel;
		int[] saveScore;

		/** Constructor */
		SaveRecord (String[] s) {
			className = s[0];
			minLevel = Integer.parseInt(s[1]);
			saveScore = new int[Type.length];
			for (int i = 0; i < Type.length; i++) {
				saveScore[i] = Integer.parseInt(s[i + 2]);
			}
		}

		/** String representation */
		public String toString() {
			String s = className + ", " + minLevel;
			for (int i = 0; i < Type.length; i++) {
				s += ", " + saveScore[i];			
			}
			return s;			
		}	
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SavingThrows instance = null;

	/** Table of saving throw targets. */
	SaveRecord[] targetsTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected SavingThrows () throws IOException {
		String[][] table = CSVReader.readFile(SAVING_THROWS_FILE);
		targetsTable = new SaveRecord[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			targetsTable[i - 1] = new SaveRecord(table[i]);
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SavingThrows getInstance() {
		if (instance == null) {
			try {
				instance = new SavingThrows();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Saving Throws file.");
			}
		}
		return instance;
	}

	/**
	*  Roll a saving throw with a modifier.
	*  @return True if the save was successful.
	*/
	public boolean rollSave (Type saveType, 
			String asClass, int level, int modifier) {

		int natRoll = Dice.roll(20);
		int total = natRoll + modifier;
		int target = getSaveTarget(saveType, asClass, level);
		return (natRoll > 1) && (total >= target);
	}

	/**
	*  Roll a saving throw without a modifier.
	*  @return True if the save was successful.
	*/
	public boolean rollSave (Type saveType, String asClass, int level) {
		return rollSave(saveType, asClass, level, 0);	
	}

	/**
	*  Get the target score for a saving throw.
	*/
	public int getSaveTarget (Type saveType, String asClass, int level) {

		// Use the OD&D book table
		if (SAVE_RULE == SaveRule.ODD) {
			SaveRecord record = getSaveRecord(asClass, level);
			return record.saveScore[saveType.ordinal()];
		}
		
		// Use the OED formula
		else if (SAVE_RULE == SaveRule.OED) {
			return 20 - level - OED_ADJUST[saveType.ordinal()];
		}

		// Handle error
		else {
			System.err.println("Unknown saving throw rule type.");
			return 20;
		}
	}

	/**
	*  Find the correct SaveRecord for this class/level.
	*/
	private SaveRecord getSaveRecord (String asClass, int level) {
		level = Math.max(1, level);
		for (int i = targetsTable.length - 1; i > -1; i--) {
			SaveRecord record = targetsTable[i];
			if (record.className.equals(asClass) && level >= record.minLevel)
				return record;
		}
		return null;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		SavingThrows st = SavingThrows.getInstance();

		// Print saving throws table
		System.out.println("Saving Throws Table");
		for (SaveRecord record: st.targetsTable) {
			System.out.println(record);
		}
		System.out.println();

		// Variables for testing
		int numRolls = 10000;
		int success;
		double ratio;
		
		// Test Ftr1 vs. Poison
		System.out.println("Test Ftr1 vs. Poison (45%?)");
		success = 0;
		for (int i = 0; i < numRolls; i++) {
			if (st.rollSave(Type.Death, "Fighter", 1))
				success++;		
		}
		ratio = (double) success / numRolls;
		System.out.println("Success ratio: " + ratio + "\n");
		
		// Test Wiz20 vs. Spells
		System.out.println("Test Wiz20 vs. Spells (90%?)");
		success = 0;
		for (int i = 0; i < numRolls; i++) {
			if (st.rollSave(Type.Spells, "Wizard", 20))
				success++;		
		}
		ratio = (double) success / numRolls;
		System.out.println("Success ratio: " + ratio + "\n");
	}
}
