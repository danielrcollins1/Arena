import java.io.IOException; 

/**
	Experience award table from Sup-I.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2015-12-27
*/

public class XPAwardTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with information. */
	private static final String XP_AWARD_TABLE_FILE = "XPAwardTable.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** One row of the XP table. */
	private class XPAwardRecord {

		/** Hit dice descriptor in table. */
		private String hitDiceStr;
		
		/** Base XP value. */
		private int baseValue;
		
		/** Extra XP award for special abilities. */
		private int specialAward;

		/** Constructor. */
		private XPAwardRecord(String [] s) {
			hitDiceStr = s[0];
			baseValue = CSVReader.parseInt(s[1]);
			specialAward = CSVReader.parseInt(s[2]);
		}

		/** String representation. */		
		public String toString() {
			return hitDiceStr + ", " + baseValue + ", " + specialAward;
		}	
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	private static XPAwardTable instance = null;
	
	/** Array of XPRecords. */
	private XPAwardRecord[] xpAwardRecordArray;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor (read from dedicated file).
		@throws IOException if file open/read fails
	*/
	protected XPAwardTable() throws IOException {
		String[][] table = CSVReader.readFile(XP_AWARD_TABLE_FILE);
		xpAwardRecordArray = new XPAwardRecord[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			xpAwardRecordArray[i - 1] = new XPAwardRecord(table[i]);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Access the singleton class instance.
	*/
	public static XPAwardTable getInstance() {
		if (instance == null) {
			try {
				instance = new XPAwardTable();
			}
			catch (IOException e) {
				System.err.println("Failed to read the XPAwardTable file.");
			}
		}
		return instance;
	}

	/**
		Get total XP for monster as per Sup-I.
		Monster xpBonuses give special addition each.
	*/
	public int getXPAward(Monster monster) {
		XPAwardRecord record = getXPAwardRecord(monster.getHitDice());
		return record.baseValue 
			+ record.specialAward * getAwardSteps(monster);
	}

	/**
		Compute Sup-I awards to give by EHD.
	*/
	private int getAwardSteps(Monster monster) {
		int ehd = monster.getEquivalentHitDice();
		if (ehd == Monster.UNDEFINED_EHD) {
			return 0;
		}
		else {
			return ehd / monster.getHitDiceNum() - 1;
		}
	}

	/**
		Lookup XP record in table, as per Sup-I.
	*/
	private XPAwardRecord getXPAwardRecord(Dice hitDice) {

		// If no dice, convert from fixed hp
		int num = hitDice.getNum();
		int add = hitDice.getAdd();
		if (hitDice.getSides() <= 0) {
			num = add / 3;
			add = 0;
		}

		// Handle different table cases
		if (num == 0) {
			return getRecordByText("1/2");
		}
		else if (num == 1 && hitDice.getMul() < 0) {
			return getRecordByText("1/2");
		}
		else if (num == 1 && add == -1) {
			return getRecordByText("1-1");
		}
		else if (num < 9) {
			String hitDiceStr;
			if (add <= 0) {
				hitDiceStr = num + "";
			}
			else if (add == 1) {
				hitDiceStr = num + "+1";
			}
			else {
				hitDiceStr = (num + 1) + "";
			}
			return getRecordByText(hitDiceStr);
		}
		else {
			if (add > 2) {
				num++;
			}
			return getRecordByNum(num);
		}
	}

	/**
		Get record by hit dice text.
	*/
	private XPAwardRecord getRecordByText(String text) {
		for (XPAwardRecord record: xpAwardRecordArray) {
			if (record.hitDiceStr.equals(text)) {
				return record;
			}
		}			
		return null;	
	}

	/**
		Get record by high hit die number.
	*/
	private XPAwardRecord getRecordByNum(int num) {
		assert (num >= 9);
		for (int i = xpAwardRecordArray.length - 1; i > 0; i--) {
			XPAwardRecord record = xpAwardRecordArray[i];
			if (num >= Integer.parseInt(record.hitDiceStr)) {
				return record;
			}
		}
		return null;	
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
		Dice.initialize();
		XPAwardTable table = XPAwardTable.getInstance();

		// Print XP award table
		System.out.println("XP Award Table");
		for (XPAwardRecord record: table.xpAwardRecordArray) {
			System.out.println(record);		
		}
		System.out.println();
		
		// Print monster XP awards
		System.out.println("Monster XP Awards");
		for (Monster m: MonsterDatabase.getInstance()) {
			System.out.println(m.getRace() + ": " + m.getXPAward());
		}
		System.out.println();
	}
}
