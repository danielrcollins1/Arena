import java.io.IOException;
import java.util.ArrayList;

/******************************************************************************
*  Index of supported character class types (singleton pattern).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-22
*  @version  1.1
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
	ArrayList<ClassType> classTypeList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected ClassIndex () throws IOException {
		String[][] index = CSVReader.readFile(CLASS_INDEX_FILE);
		classTypeList = new ArrayList<ClassType>();
		for (int i = 1; i < index.length; i++) {
			String className = index[i][0];
			String filename = "Class-" + className + ".csv";
			String[][] levelData = CSVReader.readFile(filename);
			classTypeList.add(new ClassType(index[i], levelData));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	public int size() { return classTypeList.size(); }
	public ClassType get (int index) { return classTypeList.get(index); }

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

		// Print basics of entire list
		for (int i = 0; i < cl.size(); i++) {
			System.out.println(cl.get(i));
		}
		System.out.println();
		
		// Print specifics for Fighter class
		ClassType type = cl.getTypeFromName("Fighter");
		System.out.println(type);
		for (int i = 0; i <= 20; i++) {
			System.out.println(i + "|" + type.getTitleFromLevel(i) + "|" 
				+ type.getXpReq(i) + "|" + type.getAttackBonus(i));
		}
	}
}

