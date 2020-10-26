package net.superdan.athena;

import java.io.IOException;

/******************************************************************************
 *  Table of available spells.
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2018-12-05
 *  @version 1.0
 ******************************************************************************/

public class SpellsTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with spells. */
	final String SPELLS_FILE = "SpellsTable.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SpellsTable instance = null;

	/** Table of spell information. */
	Spell[] spellList;

	/** Number of spells at each level. */
	int[] numAtLevel;

	/** Maximum spell level. */
	int maxLevel;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected SpellsTable () throws IOException {
		String[][] table = CSVReader.readFile(SPELLS_FILE);
		spellList = new Spell[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			spellList[i - 1] = new Spell(table[i]);
		}		
		setMaxLevel();
		countNumAtLevels();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SpellsTable getInstance() {
		if (instance == null) {
			try {
				instance = new SpellsTable();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Spells Table file.");
			}
		}
		return instance;
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
	*  Get random spell by level and usage.
	*/
	public Spell getRandom (int level, Spell.Usage usage) {
		Spell s;
		do {
			s = getRandom(level);
		} while (s.getUsage() != usage);
		return s;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		SpellsTable table = SpellsTable.getInstance();
		System.out.println("Random Spells:");
		for (int i = 1; i <= table.getMaxLevel(); i++) {
			System.out.println("Level " + i + ": " + table.getRandom(i));
		}
		System.out.println();
	}
}

