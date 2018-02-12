import java.io.*; 
import java.util.Scanner;
import java.util.ArrayList;

/******************************************************************************
*  Experience award table from Sup-I.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2015-12-27
*  @version  1.02
******************************************************************************/

public class XPTable {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with information. */
	final String XPTABLE_FILE = "XPTable.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	class XPRecord {
		String HDStr;
		int baseValue, specialAward;

		XPRecord (String [] s) {
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
	static XPTable instance = null;
	
	/** Array of XPRecords. */
	ArrayList<XPRecord> xpRecordArray;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected XPTable () throws IOException {
		String[][] table = CSVReader.readFile(XPTABLE_FILE);
		xpRecordArray = new ArrayList<XPRecord>();
		for (int i = 1; i < table.length; i++) {
			xpRecordArray.add(new XPRecord(table[i]));
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static XPTable getInstance() {
		if (instance == null) {
			try {
				instance = new XPTable();
			}
			catch (IOException e) {
				System.err.println("Failed to read the XPTable file.");
			}
		}
		return instance;
	}

	/**
	*  Get total XP for monster as per Sup-I.
	*  Monster xpBonuses give special addition each.
	*/
	int getXPAward (Monster monster) {
		XPRecord record = getXPRecord(monster.getHitDice());
		return record.baseValue 
			+ record.specialAward * getAwardSteps(monster);
	}

	/**
	*  Compute Sup-I awards to give by EHD.
	*/
	int getAwardSteps (Monster monster) {
		return monster.getEquivalentHitDice()
			/ monster.getHitDiceNum() - 1;
	}

	/**
	*  Lookup XP record in table, as per Sup-I. 
	*/
	XPRecord getXPRecord (Dice HD) {

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
	XPRecord getRecordByText (String text) {
		for (XPRecord record: xpRecordArray) {
			if (record.HDStr.equals(text))
				return record;		
		}			
		return null;	
	}

	/**
	*  Get record by high hit die number.
	*/
	XPRecord getRecordByNum (int num) {
		assert (num >= 9);
		for (int i = xpRecordArray.size() - 1; i > 0; i--) {
			XPRecord record = xpRecordArray.get(i);
			if (num >= Integer.parseInt(record.HDStr)) return record;
		}
		return null;	
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		XPTable xpt = XPTable.getInstance();

		// Print XP award table
		System.out.println("XP Award Table");
		for (XPRecord record: xpt.xpRecordArray) {
			System.out.println(record);		
		}
		System.out.println();
		
		// Print monster XP awards
		MonsterDatabase db = MonsterDatabase.getInstance();
		System.out.println("Monster XP Awards");
		for (int i = 0; i < db.size(); i++) {
			Monster m = db.get(i);
			System.out.println(m.getRace() + ": " + m.getXPAward()); 					
		}
	}
}

