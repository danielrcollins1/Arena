/******************************************************************************
*  Marshals given types of men, including leaders.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-15
*  @version  1.1
******************************************************************************/

public class Marshal {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Dice for standard number of men. */
	static final Dice NUMBER_APPEARING_DICE = new Dice(3, 10, 10, 0);

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Type of men to construct. */
	MenType menType;

	/** Number of men in force. */
	int menTotal;

	/** Force all leaders to carry swords. */
	boolean swordsOnly;

	/** Use revised XP awards (per Sup-I). */
	boolean useRevisedXPAwards;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	public Marshal () {
		Dice.initialize();
		menType = null;
		menTotal = 0;
		swordsOnly = false;
		useRevisedXPAwards = false;
		exitAfterArgs = false;		
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print usage.
	*/
	public void printUsage () {
		System.out.println("Usage: Marshal [options] menType [number]");
		System.out.println("  menType from those listed in data file MenTypes.csv");
		System.out.println("  where options include:");
		System.out.println("\t-f include OED feats");
		System.out.println("\t-s swords only (strict Vol-2 rule)");
		System.out.println("\t-x use XP table as per Sup-I/BX");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	public void parseArgs (String[] args) {
		for (String s: args) {
			if (s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'f': Character.setFeatUsage(true); break;
					case 's': swordsOnly = true; break;
					case 'x': useRevisedXPAwards = true; break;
					default: exitAfterArgs = true; break;
				}
			}
			else {
				if (menType == null) {
					menType = MenTypeList.getInstance().
						getCategory(s);
				}
				else {
					menTotal = Integer.parseInt(s);
				}
			}
		}
	}

	/**
	*  Should we exit after parsing arguments?
	*/
	public boolean exitAfterArgs () {
		return exitAfterArgs || menType == null;
	}

	/**
	*  Main method.
	*/
	public void assembleMen () {
		if (menTotal == 0) {
			menTotal = NUMBER_APPEARING_DICE.roll();
		}
		reportHeader();
		reportGrunts();
		reportLeaders();
		reportNotes();
	}

	/**
	*  Generate and report unit header.
	*/
	void reportHeader () {
		String header = menType + ", " 
			+ menType.determineAlignment() + ", "
			+ menTotal + " Total ";
		System.out.println(header);
		System.out.println();
	}

	/**
	*  Generate and report on grunt-types.
	*/
	void reportGrunts () {
		MenType.Component[] comp = menType.createComponents(menTotal);
		for (MenType.Component c: comp) {
			System.out.println(c.number + " " + c.description);			
		}
		System.out.println();
	}

	/**
	*  Generate and report on leader-types.
	*/
	void reportLeaders () {
		Arena arena = new Arena(false, menTotal);
		arena.setBaseArmor(menType.getLeaderArmor());
		arena.setUseRevisedXPAwards(useRevisedXPAwards);
		if (swordsOnly) {
			arena.setBaseWeapon(new Weapon("Sword", new Dice(8), 1));		
		}
		while (!isLeaderReqMet(arena)) {
			arena.runOneCycle();		
		}
		arena.printMenAboveLevel(4);
	}

	/**
	*  Check if we have required leaders.
	*/
	boolean isLeaderReqMet (Arena arena) {
		if (menTotal < 50)
			return arena.countMenAboveLevel(4) >= 1;
		else if (menTotal < 100)
			return arena.countMenAboveLevel(5) >= 1;
		else
			return arena.countMenAboveLevel(8) >= menTotal/100;
	}

	/**
	*  Generate and report miscellaneous notes.
	*/
	void reportNotes () {
		String notes = "";
		if (menType.getNotes().length() > 1)
			notes += menType.getNotes() + " ";
		String casterString = casterString();
		if (casterString.length() > 1)
			notes += casterString;
		if (notes.length() > 1)
			System.out.println("Notes: " + notes);
		System.out.println();
	}

	/**
	*  String descriptor of spellcasters.
	*/
	String casterString () {
		if (menTotal < 200 || !menType.hasCasters()) 
			return "";
		else if (menTotal < 300)
			return "50% for Magic-User (10th-11th), 25% for Cleric (8th).";
		else
			return "100% for Magic-User (10th-11th), 50% for Cleric (8th).";
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Marshal marshal = new Marshal();
		marshal.parseArgs(args);
		if (marshal.exitAfterArgs()) {
			marshal.printUsage();
		}
		else {
			marshal.assembleMen();
		}
	}
}

