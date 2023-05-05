import java.io.IOException; 

/**
	List of NPC men encounters (per Vol-2).

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2016-02-14
*/

public class MenTypeList {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with information. */
	private static final String MENTYPES_FILE = "MenTypes.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static MenTypeList instance = null;
	
	/** List of MenTypes. */
	private MenType[] typeList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected MenTypeList() throws IOException {
		String[][] table = CSVReader.readFile(MENTYPES_FILE);
		typeList = new MenType[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			typeList[i - 1] = new MenType(table[i]);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
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
		Get a MenType from string description.
	*/
	public MenType getCategory(String name) {
		for (MenType m: typeList) {
			if (m.getName().equalsIgnoreCase(name)) {
				return m;
			}		
		}	
		return null;
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
		Dice.initialize();
		MenTypeList mt = MenTypeList.getInstance();
		for (MenType m: mt.typeList) {
			System.out.println(m + ", " + m.getAlignment());
			MenType.Component[] comp = m.createComponents(80);
			for (MenType.Component c: comp) {
				System.out.println("- " + c.getNumber() + " " + c.getDescription());
			}
			System.out.println();
		}
	}
}
