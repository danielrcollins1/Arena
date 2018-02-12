import java.io.*; 
import java.util.ArrayList;

/******************************************************************************
*  List of NPC men encounters.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-14
*  @version  1.0
******************************************************************************/

public class MenTypeList {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with information. */
	final String MENTYPES_FILE = "MenTypes.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static MenTypeList instance = null;
	
	/** List of MenTypes. */
	ArrayList<MenType> typeList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected MenTypeList () throws IOException {
		String[][] table = CSVReader.readFile(MENTYPES_FILE);
		typeList = new ArrayList<MenType>();
		for (int i = 1; i < table.length; i++) {
			typeList.add(new MenType(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static MenTypeList getInstance() {
		if (instance == null) {
			try {
				instance = new MenTypeList();
			}
			catch (IOException e) {
				System.out.println("Failed to read the MenTypes file.");
			}
		}
		return instance;
	}

	/**
	*  Get a MenType from string description.
	*/
	MenType getCategory (String category) {
		for (MenType m: typeList) {
			if (m.category.equalsIgnoreCase(category)) {
				return m;
			}		
		}	
		return null;
	}

	/**
	*	Identify	this object	as	a string.
	*/
	public String toString () {
		String s = "";
		for (int i = 0; i < typeList.size(); i++) {
			s += typeList.get(i).getCategory();
			if (i < typeList.size() - 1) s += ", ";		
		}	
		return s;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		MenTypeList mt = MenTypeList.getInstance();
		for (MenType m: mt.typeList) {
			System.out.println(m + ", " + m.determineAlignment());
			MenType.Component[] comp = m.createComponents(80);
			for (MenType.Component c: comp) {
				System.out.println("- " + c.number + " " + c.description);			
			}
			System.out.println();
		}
	}
}

