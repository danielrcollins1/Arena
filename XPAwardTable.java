import java.io.IOException; 

/******************************************************************************
*  Experience award table from Sup-I.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2015-12-27
******************************************************************************/

public class XPAwardTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with information. */
	final String XP_AWARD_TABLE_FILE = "XPAwardTable.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	private class XPAwardRecord {
		String HDStr;
		int baseValue, specialAward;

		XPAwardRecord (String [] s) {
			HDStr = s[0];
			baseValue = CSVReader.parseInt(s[1]);
			specialAward = CSVReader.parseInt(s[2]);
		}
		
		public String toString() {
			return HDStr + ", " + baseValue + ", " + specialAward;
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
	*  Constructor (read from dedicated file).
	*/
	protected XPAwardTable () throws IOException {
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
	*  Access the singleton class instance.
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
	*  Get total XP for monster as per Sup-I.
	*  Monster xpBonuses give special addition each.
	*/
	public int getXPAward (Monster monster) {
		XPAwardRecord record = getXPAwardRecord(monster.getHitDice());
		return record.baseValue 
			+ record.specialAward * getAwardSteps(monster);
	}

	/**
	*  Compute Sup-I awards to give by EHD.
	*/
	private int getAwardSteps (Monster monster) {
		int EHD = monster.getEquivalentHitDice();
		if (EHD == Monster.UNDEFINED_EHD) {
			return 0;
		}
		else {
			return EHD / monster.getHitDiceNum() - 1;
		}
	}

	/**
	*  Lookup XP record in table, as per Sup-I. 
	*/
	private XPAwardRecord getXPAwardRecord (Dice HD) {

		// If no dice, convert from fixed hp
		int num = HD.getNum();
		int add = HD.getAdd();
		if (HD.getSides() <= 0) {
			num = add/3;
			add = 0;
		}

		// Handle different table cases
		if (num == 0) {
			return getRecordByText("1/2");
		}
		else if (num == 1 && HD.getMul() < 0) {
			return getRecordByText("1/2");
		}
		else if (num == 1 && add == -1) {
			return getRecordByText("1-1");
		}
		else if (num < 9) {
			String HDStr;
			if (add <= 0) HDStr = num + "";
			else if (add == 1) HDStr = num + "+1";
			else HDStr = (num + 1) + "";
			return getRecordByText(HDStr);
		}
		else {
			if (add > 2) num++;
			return getRecordByNum(num);
		}
	}

	/**
	*  Get record by hit dice text.
	*/
	private XPAwardRecord getRecordByText (String text) {
		for (XPAwardRecord record: xpAwardRecordArray) {
			if (record.HDStr.equals(text))
				return record;		
		}			
		return null;	
	}

	/**
	*  Get record by high hit die number.
	*/
	private XPAwardRecord getRecordByNum (int num) {
		assert (num >= 9);
		for (int i = xpAwardRecordArray.length - 1; i > 0; i--) {
			XPAwardRecord record = xpAwardRecordArray[i];
			if (num >= Integer.parseInt(record.HDStr)) return record;
		}
		return null;	
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
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

