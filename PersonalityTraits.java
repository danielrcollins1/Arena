import java.io.IOException; 

/**
	Personality traits for characters.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2018-04-03
*/

public class PersonalityTraits {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with traits. */
	private static final String TRAITS_FILE = "PersonalityTraits.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Class to store one trait descriptor. */
	class PersonalityTrait {

		/** Name of the trait. */
		private String name;
		
		/** Positive or negative value. */
		private int value;

		/** Constructor. */
		PersonalityTrait(String[] s) {
			name = s[0];
			if (s[1].equals("Positive")) { value = +1; }
			else if (s[1].equals("Negative")) { value = -1; }
			else { value = 0; }
		}
		
		/** String descriptor. */
		public String toString() {
			return name;
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static PersonalityTraits instance = null;

	/** Lists of traits. */
	private PersonalityTrait[] traitList;
	
	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected PersonalityTraits() throws IOException {
		String[][] table = CSVReader.readFile(TRAITS_FILE);
		traitList = new PersonalityTrait[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			traitList[i - 1] = new PersonalityTrait(table[i]);
		}		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
	*/
	public static PersonalityTraits getInstance() {
		if (instance == null) {
			try {
				instance = new PersonalityTraits();
			}
			catch (IOException e) {
				System.out.println("Failed to read the Personality Traits file.");
			}
		}
		return instance;
	}

	/**
		Are this alignment and trait value compatible?
	*/
	private boolean alignTraitCompatible(Alignment align, int value) {
		if (align == null) {
			return true;
		}
		switch (align) {
			case Lawful:  return value == +1;
			case Neutral: return value ==  0;
			case Chaotic: return value == -1;
			default: return false;
		}
	}

	/**
		Get a random trait.
	*/
	public PersonalityTrait getRandom(Alignment align) {
		while (true) {
			int randTrait = Dice.roll(traitList.length) - 1;
			PersonalityTrait trait = traitList[randTrait];
			if (alignTraitCompatible(align, trait.value)) {
				return trait;
			}
		}
	}

	/**
		Helper test function.
	*/
	private void printMultiTraits(Alignment align, int num) {
		System.out.println(align + " traits:");
		for (int i = 0; i < num; i++) {
			System.out.println(getRandom(align));
		}	
		System.out.println();
	}
	
	/**
		Main test function.
		@throws IOException if file open/read fails
	*/
	public static void main(String[] args) throws IOException {
		Dice.initialize();
		final int numTraits = 5;
		PersonalityTraits pt = new PersonalityTraits();
		pt.printMultiTraits(Alignment.Lawful,  numTraits);
		pt.printMultiTraits(Alignment.Neutral, numTraits);
		pt.printMultiTraits(Alignment.Chaotic, numTraits);
		pt.printMultiTraits(null, numTraits);
	}
}
