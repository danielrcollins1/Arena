import java.io.IOException; 

/**
	Table of conversions from EHD to Monster Level Table.
	
	We use this to create monster encounter tables dynamically
	for any monsters present in our database.
	EHD: Equivalent Hit Dice record for each monster.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2017-11-19
*/

public class EHDToTables {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	private static final String EHD_TO_TABLE_FILE = "EHDToTable.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** One EHD to Monster Level Table key. */
	private class EHDTableRecord {

		/** Minimum EHD for this key. */
		private int equivHD;

		/** Associated monster level table. */		
		private int tableIdx;
	
		/** Constructor. */
		private EHDTableRecord(String[] s) {
			equivHD = Integer.parseInt(s[0]);
			tableIdx = Integer.parseInt(s[1]);
		}	
	
		/** String representation. */
		public String toString() {
			return "EHD " + equivHD + ", Table " + tableIdx;
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static EHDToTables instance = null;
	
	/** Array of EHDTableRecords. */
	private EHDTableRecord[] convertMatrix;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected EHDToTables() throws IOException {
		String[][] table = CSVReader.readFile(EHD_TO_TABLE_FILE);
		convertMatrix = new EHDTableRecord[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			convertMatrix[i - 1] = new EHDTableRecord(table[i]);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
	*/
	public static EHDToTables getInstance() {
		if (instance == null) {
			try {
				instance = new EHDToTables();
			}
			catch (IOException e) {
				System.out.println("Failed to read the EHDToTables file.");
			}
		}
		return instance;
	}

	/**
		Map an EHD to a monster level table.
	*/
	public int mapEHDToTable(int equivHD) {
		for (int i = convertMatrix.length - 1; i > -1; i--) {
			if (equivHD >= convertMatrix[i].equivHD) {
				return convertMatrix[i].tableIdx;
			}
		}	
		return -1;
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
		EHDToTables matrix = EHDToTables.getInstance();
		System.out.println("EHD To Tables:");
		for (int ehd = 0; ehd <= 20; ehd++) {
			System.out.println(ehd + "\t" 
				+ matrix.mapEHDToTable(ehd));
		}
		System.out.println();
	}
}
