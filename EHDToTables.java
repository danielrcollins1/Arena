import java.io.IOException; 

/******************************************************************************
*  Table of conversions from EHD to Monster Level Table.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-11-19
******************************************************************************/

public class EHDToTables {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with treasure information. */
	final String EHD_TO_TABLE_FILE = "EHDToTable.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	private class EHDTableRecord {
		int EHD, tableIdx;
	
		EHDTableRecord (String[] s) {
			EHD = Integer.parseInt(s[0]);
			tableIdx = Integer.parseInt(s[1]);
		}	
	
		public String toString () {
			return "EHD " + EHD + ", Table " + tableIdx;
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static EHDToTables instance = null;
	
	/** Array of EHDTableRecords. */
	EHDTableRecord[] convertMatrix;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected EHDToTables () throws IOException {
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
	*  Access the singleton class instance.
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
	*  Map an EHD to a monster level table.
	*/
	int mapEHDToTable (int EHD) {
		for (int i = convertMatrix.length - 1; i > -1; i--) {
			if (EHD >= convertMatrix[i].EHD)
				return convertMatrix[i].tableIdx;
		}	
		return -1;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		EHDToTables matrix = EHDToTables.getInstance();
		System.out.println("EHD To Tables:");
		for (int ehd = 0; ehd <= 20; ehd++) {
			System.out.println(ehd + "\t" 
				+ matrix.mapEHDToTable(ehd));
		}
		System.out.println();
	}
}

