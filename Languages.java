import java.io.IOException; 
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
	Random list of languages.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2026-05-07
*/

public class Languages {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with languages. */
	private static final String LANGUAGES_FILE = "Languages.csv";
	
	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Class for info on one language. */
	public class Language {

		/** Name of the language. */
		private String name;
		
		/** Constructor. */
		private Language(String[] s) {
			name = s[0];
		}
		
		/** Identify the name string. */
		public String toString() {
			return name;		
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static Languages instance = null;

	/** Table of language information. */
	private Language[] languageList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected Languages() throws IOException {
		String[][] table = CSVReader.readFile(LANGUAGES_FILE);
		languageList = new Language[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			languageList[i - 1] = new Language(table[i]);
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
	*/
	public static Languages getInstance() {
		if (instance == null) {
			try {
				instance = new Languages();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Languages file.");
			}
		}
		return instance;
	}

	/**
		Get one random language.
	*/
	public Language getRandom() {
		int roll = Dice.roll(languageList.length);
		return languageList[roll - 1];
	}

	/**
		Get a number of distinct random languages.
	*/
	public List<Language> getRandom(int number) {
		assert number <= languageList.length;
		ArrayList<Language> copyList = 
			new ArrayList<Language>(Arrays.asList(languageList));
		Collections.shuffle(copyList);
		return copyList.subList(0, number);
	}

	/**
		Main test function.
	*/
	public static void main(String[] args) {	
		Dice.initialize();
		Languages langList = Languages.getInstance();

		// Print some random languages
		System.out.println("# Random Languages");
		List<Language> list = langList.getRandom(10);
		for (Language lang: list) {
			System.out.println(lang);
		}
		System.out.println();
	}
}
