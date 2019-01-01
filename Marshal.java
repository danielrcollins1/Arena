/******************************************************************************
*  Marshals given types of men, including leaders.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-15
*  @version  1.2
******************************************************************************/

public class Marshal {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Dice for men number appearing, as per Vol-2. */
	static final Dice NA_DICE = new Dice(3, 10, 10, 0);

	/*
	*  Fight cycles per man to simulate.
	*  This has been chosen after experimentation to generate a leader level
	*  approximately in scale with those specified for men in Vol-2. 
	*/
	static final int FIGHTS_PER_MAN = 4;

	/** 
	*  Percent chance magic per level.
	*  This matches the number in Vol-2; however, note that due to natural 
	*  selection, leaders will be observed with a higher frequency of magic.
	*/	
	static final int PCT_MAGIC_PER_LEVEL = 5;
	
	/** 
	*  Number of leaders to print. 
	*  This (a) roughly simulates lieutenants in a cival war-era company, 
	*  and (b) serves to fit a unit on a half digest sized page.
	*/
	static final int NUM_LEADERS_TO_PRINT = 4;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Type of men to construct. */
	MenType menType;

	/** Number of men in force. */
	int menTotal;

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
		menTotal = NA_DICE.roll();
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
		reportHeader();
		reportNotes();
		reportLeaders();
		reportGrunts();
	}

	/**
	*  Generate and report unit header.
	*/
	void reportHeader () {
		String header = menType + ", " 
			+ menTotal + " Total ";
		System.out.println(header);
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
	*  Generate and report on leader-types.
	*/
	void reportLeaders () {
		Arena arena = new Arena(menTotal, false, true);
		arena.setBaseArmor(menType.getLeaderArmor());
		arena.setTypicalAlignment(menType.getAlignment());
		arena.setFightCycles(menTotal * FIGHTS_PER_MAN);
		arena.setPctMagicPerLevel(PCT_MAGIC_PER_LEVEL);
		arena.runSim();
		arena.printTopFighters(NUM_LEADERS_TO_PRINT);
	}

	/**
	*  Generate and report on grunt-types.
	*/
	void reportGrunts () {
		MenType.Component[] comp = menType.createComponents(menTotal);
		for (MenType.Component c: comp) {
			System.out.println(c.number + " " + c.description + ".");
		}
		System.out.println();
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

