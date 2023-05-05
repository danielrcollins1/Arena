import java.io.IOException; 

/**
	Name generator for characters.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2016-02-12
*/

public class NameGenerator {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with names. */
	private static final String NAMES_FILE = "Names.csv";

	/** Array of available race codes. */
	private static final char[] RACE_CODES = {'D', 'E', 'H', 'M'};
	
	/** Array of available sex codes. */
	private static final char[] SEX_CODES = {'F', 'M', 'U'};	

	/** Base percent chance for female name. */
	private static final int BASE_PERCENT_FEMALE = 15;

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Class to store information on one name. */
	class NameData {

		/** Name in question. */
		private String name;
		
		/** Racial identifier. */
		private char race;
		
		/** Gender identifier. */
		private char sex;

		/** Constructor. */
		NameData(String[] s) {
			name = s[0];
			race = s[1].charAt(0);
			sex = s[2].charAt(0);
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static NameGenerator instance = null;

	/** The name list data. */
	private NameData[] nameList;

	/** Chance for female name. */
	private int percentFemale;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected NameGenerator() throws IOException {
		percentFemale = BASE_PERCENT_FEMALE;
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
		Access the singleton class instance.
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
		Set a new chance for female names.
	*/
	public void setChanceFemale(int percent) {
		percentFemale = percent;	
	}

	/**
		Get a random name, given race and sex.
	*/
	public String getRandom(char race, char sex) {
		assert isValidRace(race);
		assert isValidSex(sex);
		NameData nameData;
		do {
			int index = Dice.roll(nameList.length) - 1;
			nameData = nameList[index];
		} while (!(nameData.race == race && nameData.sex == sex));
		return nameData.name;
	}

	/**
		Get a random name, given the race.
	*/
	public String getRandom(char race) {
		assert isValidRace(race);
		char sex = (Dice.rollPct() <= percentFemale) ? 'F' : 'M';
		return getRandom(race, sex);		
	}

	/**
		Get a random name, given race by string.
	*/
	public String getRandom(String race) {
		char raceCode = getRaceCode(race);
		assert isValidRace(raceCode);
		return getRandom(raceCode);		
	}

	/**
		Get a random race identifier.
	*/
	private char getRandomRace() {
		char race;
		switch (Dice.roll(6)) {
			case 1: race = 'D'; break;
			case 2: race = 'E'; break;
			case 3: race = 'H'; break;
			default: race = 'M'; break;		
		}	
		return race;	
	}

	/**
		Get a random name of any type.
	*/
	public String getRandom() {
		char race = getRandomRace();
		return getRandom(race);
	}

	/**
		Is this a valid race code?
	*/
	private boolean isValidRace(char race) {
		for (char code: RACE_CODES) {
			if (code == race) {
				return true;
			}
		}	
		return false;	
	}

	/**
		Is this a valid sex code?
	*/
	private boolean isValidSex(char sex) {
		for (char code: SEX_CODES) {
			if (code == sex) {
				return true;
			}
		}	
		return false;	
	}

	/**
		Convert race string to character code.
	*/
	private char getRaceCode(String s) {
		assert s != null && s.length() > 0;
		return s.equals("Human") ? 'M' : s.charAt(0);
	}
	
	/**
		Main test function.
	*/
	public static void main(String[] args) {	
		Dice.initialize();
		NameGenerator nameGen = NameGenerator.getInstance();
		System.out.println("Random Names:");
		for (int i = 0; i <= 20; i++) {
			System.out.println(nameGen.getRandom());
		}
		System.out.println();
	}
}
