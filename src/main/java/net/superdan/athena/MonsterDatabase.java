package net.superdan.athena;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/******************************************************************************
 *  Database of available monster types (singleton pattern).
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2014-07-18
 *  @version 1.0
 ******************************************************************************/

public class MonsterDatabase implements Iterable<Monster> {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/**
	 * Name of file with monster information.
	 */
	final String MONSTER_FILE = "MonsterDatabase.csv";

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/**
	 * The singleton class instance.
	 */
	static MonsterDatabase instance = null;

	/**
	 * List of net.superdan.athena.Monster records.
	 */
	List<Monster> monsterList;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	 * Constructor (read from dedicated file).
	 */
	protected MonsterDatabase() throws IOException {
		String[][] table = CSVReader.readFile(MONSTER_FILE);
		monsterList = new ArrayList<>(table.length - 1);
		for (int i = 1; i < table.length; i++) {
			monsterList.add(new Monster(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static MonsterDatabase getInstance() {
		if (instance == null) {
			try {
				instance = new MonsterDatabase();
			} catch (IOException e) {
				System.out.println("Failed to read the net.superdan.athena.MonsterDatabase file.");
			}
		}
		return instance;
	}

	/**
	*	Return iterator for the iterable interface.
	*/
	public Iterator<Monster> iterator() {        
		return monsterList.iterator();
	}

	/**
	*  Get a monster by matching its race.
	*/
	public Monster getByRace (String race) {
		for (Monster m: this) {
			if (m.getRace().equalsIgnoreCase(race)) {
				return m;
			}
		}
		return null;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();		
		MonsterDatabase db = MonsterDatabase.getInstance();
		for (Monster m: db) {
			System.out.println(m);
		}
		System.out.println();
	}
}

