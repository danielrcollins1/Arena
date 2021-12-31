import java.io.IOException;

/******************************************************************************
*  Index of supported character class types (singleton pattern).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-22
******************************************************************************/

public class ClassIndex {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with class information. */
	final String CLASS_INDEX_FILE = "ClassIndex.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static ClassIndex instance = null;
	
	/** Array of ClassInfo records. */
	ClassType[] classTypeList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected ClassIndex () throws IOException {
		String[][] index = CSVReader.readFile(CLASS_INDEX_FILE);
		classTypeList = new ClassType[index.length - 1];
		for (int i = 1; i < index.length; i++) {
			String className = index[i][0];
			String filename = "Class-" + className + ".csv";
			String[][] levelData = CSVReader.readFile(filename);
			classTypeList[i - 1] = new ClassType(index[i], levelData);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static ClassIndex getInstance() {
		if (instance == null) {
			try {
				instance = new ClassIndex();
			}
			catch (IOException e) {
				System.out.println("Failed to read the Classes file.");
			}
		}
		return instance;
	}

	/**
	*  Get ClassType for a given class name.
	*/
	public ClassType getTypeFromName (String name) {
		for (ClassType type: classTypeList) {
			if (type.getName().equals(name)) {
				return type;
			}		
		}
		return null;
	}

	/**
	*  Get ClassType for a given class title.
	*/
	public ClassType getTypeFromTitle (String title) {
		for (ClassType type: classTypeList) {
			if (type.getLevelFromTitle(title) > -1) {
				return type;			
			}
		}
		return null;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		ClassIndex cl = ClassIndex.getInstance();

		// Print table for each class
		for (ClassType type: cl.classTypeList) {
			System.out.println(type);
			for (int i = 0; i <= 16; i++) {
				System.out.println(i + ", "
					+ type.getTitleFromLevel(i) + ", "
					+ type.getXpReq(i) + ", "
					+ type.getHitDiceTotal(i) + ", +"
					+ type.getAttackBonus(i));
			}
			System.out.println();
		}
	}
}

