import java.io.*; 
import java.util.Scanner;
import java.util.ArrayList;

/******************************************************************************
*  Saving throws table (singleton pattern).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-20
*  @version  1.1
******************************************************************************/

public class SavingThrows {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with saving throw scores. */
	final String SAVING_THROWS_FILE = "SavingThrows.csv";

	/** Allow a save versus energy drain? */
	public static final boolean ENERGY_DRAIN_SAVE = true;

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** Saving throw types. */
	public enum SaveType {
		Death, Wands, Stone, Breath, Spells;

		/** Total number of save types available. */
		public static final int length = SaveType.values().length;
	};

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	class SaveRecord {
		String className;
		int minLevel;
		int[] saveScore;

		SaveRecord (String[] s) {
			className = s[0];
			minLevel = Integer.parseInt(s[1]);
			saveScore = new int[SaveType.length];
			for (int i = 0; i < SaveType.length; i++) {
				saveScore[i] = Integer.parseInt(s[i + 2]);
			}
		}

		public String toString() {
			String s = className + ", " + minLevel;
			for (int i = 0; i < SaveType.length; i++) {
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
	ArrayList<SaveRecord> targetsTable;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected SavingThrows () throws IOException {
		String[][] table = CSVReader.readFile(SAVING_THROWS_FILE);
		targetsTable = new ArrayList<SaveRecord>();
		for (int i = 1; i < table.length; i++) {
			targetsTable.add(new SaveRecord(table[i]));		
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
	public boolean rollSave (SaveType saveType, 
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
	public boolean rollSave (SaveType saveType, String asClass, int level) {
		return rollSave(saveType, asClass, level, 0);	
	}

	/**
	*  Get the target score for a saving throw.
	*/
	public int getSaveTarget (SaveType saveType, String asClass, int level) {
		SaveRecord record = getSaveRecord(asClass, level);
		return record.saveScore[saveType.ordinal()];
	}

	/**
	*  Find the correct SaveRecord for this class/level.
	*/
	SaveRecord getSaveRecord (String asClass, int level) {
		level = Math.max(1, level);
		for (int i = targetsTable.size() - 1; i > -1; i--) {
			SaveRecord record = targetsTable.get(i);
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
			if (st.rollSave(SaveType.Death, "Fighter", 1))
				success++;		
		}
		ratio = (double) success / numRolls;
		System.out.println("Success ratio: " + ratio + "\n");
		
		// Test Wiz20 vs. Spells
		System.out.println("Test Wiz20 vs. Spells (90%?)");
		success = 0;
		for (int i = 0; i < numRolls; i++) {
			if (st.rollSave(SaveType.Spells, "Wizard", 20))
				success++;		
		}
		ratio = (double) success / numRolls;
		System.out.println("Success ratio: " + ratio + "\n");
	}
}

