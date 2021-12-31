import java.io.IOException; 

/******************************************************************************
*  Name generator for characters.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-12
******************************************************************************/

public class NameGenerator {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with names. */
	final String NAMES_FILE = "Names.csv";

	/** Base chance for female name. */
	final double BASE_CHANCE_FEMALE = 0.125;

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	class NameData {
		String name;
		boolean isMale;	

		NameData (String[] s) {
			name = s[0];
			isMale = s[1].equals("M");
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static NameGenerator instance = null;

	/** Table of saving throw targets. */
	NameData[] nameList;

	/** Chance for female name. */
	double chanceFemale;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected NameGenerator () throws IOException {
		chanceFemale = BASE_CHANCE_FEMALE;
		String[][] table = CSVReader.readFile(NAMES_FILE);
		nameList = new NameData[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			nameList[i - 1] = new NameData(table[i]);
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static NameGenerator getInstance() {
		if (instance == null) {
			try {
				instance = new NameGenerator();
			}
			catch (IOException e) {
				System.out.println("Failed to read the Names file.");
			}
		}
		return instance;
	}

	/**
	*  Set a new chance for female names.
	*/
	public void setChanceFemale (double chance) {
		chanceFemale = chance;	
	}

	/**
	*  Get a random name.
	*/
	public String getRandom () {
		boolean randMale = (Math.random() > chanceFemale);
		while (true) {
			int index = Dice.roll(nameList.length) - 1;
			NameData nameData = nameList[index];
			if (nameData.isMale == randMale) {
				return nameData.name;			
			}
		}
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		NameGenerator nameGen = NameGenerator.getInstance();
		System.out.println("Random Names:");
		for (int i = 0; i <= 20; i++) {
			System.out.println(nameGen.getRandom());
		}
		System.out.println();
	}
}

