import java.io.IOException; 

/******************************************************************************
*  Index of available spells.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-05
******************************************************************************/

public class SpellsIndex {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with spells. */
	final String SPELLS_FILE = "SpellsIndex.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SpellsIndex instance = null;

	/** Table of spell information. */
	Spell[] spellList;

	/** Number of spells at each level. */
	int[] numAtLevel;

	/** Number of castable spells at each level. */
	int[] numAtLevelCastable;

	/** Maximum spell level. */
	int maxLevel;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected SpellsIndex () throws IOException {
		String[][] table = CSVReader.readFile(SPELLS_FILE);
		spellList = new Spell[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			spellList[i - 1] = new Spell(table[i]);
		}		
		setMaxLevel();
		countNumAtLevels();
		linkSpellsToCastings();
		countNumCastable();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SpellsIndex getInstance() {
		if (instance == null) {
			try {
				instance = new SpellsIndex();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Spells Index file.");
			}
		}
		return instance;
	}

	/**
	*  Link spells to available in-game casting formulae.
	*/
	private void linkSpellsToCastings () {
		for (Spell s: spellList) {
			SpellCasting.linkSpellWithCasting(s);		
		}
	}	

	/**
	*  Get the maximum level.
	*/
	public int getMaxLevel () {
		return maxLevel;
	}

	/**
	*  Set the maximum level.
	*/
	private void setMaxLevel () {
		int max = -1;
		for (Spell s: spellList) {
			if (s.getLevel() > max)
				max = s.getLevel();
		}
		maxLevel = max;
	}

	/**
	*  Get number at a given level.
	*/
	public int getNumAtLevel (int level) {
		return numAtLevel[level - 1];
	}

	/**
	*  Count spells at each level.
	*/
	private void countNumAtLevels () {
		numAtLevel = new int[maxLevel];
		for (Spell s: spellList)
			numAtLevel[s.getLevel() - 1]++;
	}

	/**
	*  Get number castable at a given level.
	*/
	public int getNumAtLevelCastable (int level) {
		return numAtLevelCastable[level - 1];
	}

	/**
	*  Count spells castable by level.
	*/
	private void countNumCastable () {
		numAtLevelCastable = new int[maxLevel];
		for (Spell s: spellList) {
			if (s.isCastable())
				numAtLevelCastable[s.getLevel() - 1]++;
		}
	}

	/**
	*  Get random spell by level.
	*/
	public Spell getRandom (int level) {
		int count = getNumAtLevel(level);
		if (count > 0) {
			int roll = Dice.roll(count);
			for (Spell s: spellList) {
				if (s.getLevel() == level) {
					if (--roll == 0)
						return s;
				}
			}
		}
		return null;
	}

	/**
	*  Get random castable spell by level.
	*/
	public Spell getRandomCastable (int level) {
		int count = getNumAtLevelCastable(level);
		if (count > 0) {
			int roll = Dice.roll(count);
			for (Spell s: spellList) {
				if (s.getLevel() == level
						&& s.isCastable()) 
				{
					if (--roll == 0)
						return s;
				}
			}
		}
		return null;
	}

	/**
	*  Find a spell by name.
	*/
	public Spell findByName (String name) {
		for (Spell s: spellList) {
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}

	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		SpellsIndex index = SpellsIndex.getInstance();

		// Print random spell at each level
		System.out.println("Random Spells:");
		for (int i = 1; i <= index.getMaxLevel(); i++) {
			System.out.println("Level " + i + ": " + index.getRandom(i));
		}
		System.out.println();
		
		// Print random castable spell at each level
		System.out.println("Random Castable Spells:");
		for (int i = 1; i <= index.getMaxLevel(); i++) {
			System.out.println("Level " + i + ": " + index.getRandomCastable(i));
		}
		System.out.println();
		
		// Print spells with in-game castings
		System.out.println("Spells Castable In-Game:");
		for (Spell s: index.spellList) {
			if (s.isCastable()) {
				System.out.println(s.getName());			
			}		
		}
		System.out.println();
	}
}
