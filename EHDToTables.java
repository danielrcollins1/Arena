import java.io.*; 
import java.util.*;

/******************************************************************************
*  Table of conversions from EHD to Monster Level Table.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-11-19
*  @version  1.0
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
	ArrayList<EHDTableRecord> convertMatrix;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected EHDToTables () throws IOException {
		convertMatrix = new ArrayList<EHDTableRecord>();
		String[][] table = CSVReader.readFile(EHD_TO_TABLE_FILE);
		for (int i = 1; i < table.length; i++) {
			convertMatrix.add(new EHDTableRecord(table[i]));
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
		for (int i = convertMatrix.size()-1; i > -1; i--) {
			if (EHD >= convertMatrix.get(i).EHD)
				return convertMatrix.get(i).tableIdx;
		}	
		return -1;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		EHDToTables matrix = EHDToTables.getInstance();
	
		// Print EHDToTables matrix
		System.out.println("\nEHDToTables");
		for (int ehd = 0; ehd <= 20; ehd++) {
			System.out.println(ehd + "\t" 
				+ matrix.mapEHDToTable(ehd));
		}
	}
}

