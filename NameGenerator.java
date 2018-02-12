import java.io.*; 
import java.util.Scanner;
import java.util.ArrayList;

/******************************************************************************
*  Name generator for characters.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-12
*  @version  1.0
******************************************************************************/

public class NameGenerator {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with names. */
	final String NAMES_FILE = "Names.csv";

	/** Chance for female name. */
	final double CHANCE_FEMALE = 0.125;

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
	ArrayList<NameData> nameList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected NameGenerator () throws IOException {
		String[][] table = CSVReader.readFile(NAMES_FILE);
		nameList = new ArrayList<NameData>();
		for (int i = 1; i < table.length; i++) {
			nameList.add(new NameData(table[i]));
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
	*  Get a random name.
	*/
	public String getRandom () {
		boolean randMale = (Math.random() > CHANCE_FEMALE);
		while (true) {
			int randName = Dice.roll(nameList.size()) - 1;
			NameData nameData = nameList.get(randName);
			if (nameData.isMale == randMale) {
				return nameData.name;			
			}
		}
	}
}

