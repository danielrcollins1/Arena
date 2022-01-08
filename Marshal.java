import java.util.List;

/******************************************************************************
*  Marshals given types of men, including leaders.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-15
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
	*  Percent chance for wizards magic per level.
	*  Since wizards aren't developed evolutionarily, 
	*  need higher percentage to maintain par.
	*/	
	static final int PCT_WIZARD_MAGIC_PER_LEVEL = 15;
	
	/** 
	*  Number of leaders to print. 
	*  This (a) roughly simulates lieutenants in a cival war-era company, 
	*  and (b) serves to fit a unit on a half digest sized page.
	*/
	static final int NUM_LEADERS_TO_PRINT = 4;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Arena object to develop fighters. */
	Arena arena;

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
	*  Print program banner.
	*/
	void printBanner () {
		System.out.println("OED Marshal Program");
		System.out.println("-------------------");
	}

	/**
	*  Print usage.
	*/
	public void printUsage () {
		System.out.println("Usage: Marshal [options] menType [number]");
		System.out.println("  menType from those listed in data file MenTypes.csv");
		System.out.println("  where options include:");
		System.out.println("\t-f include OED feats");
		System.out.println("\t-w use fighter sweep attacks (by level vs. 1 HD)");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	public void parseArgs (String[] args) {
		for (String s: args) {
			if (s.length() > 1 && s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'f': Character.setFeatUsage(true); break;
					case 'w': Character.setSweepAttacks(true); break;
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
		runArena();
		reportHeader();
		reportWizard();
		reportLeaders();
		reportTroops();
	}

	/**
	*  Run Arena to develop leader figures.
	*/
	void runArena () {
		arena = new Arena(menTotal, false, true);
		arena.setBaseArmor(menType.getLeaderArmor());
		arena.setTypicalAlignment(menType.getAlignment());
		arena.setFightCycles(menTotal * FIGHTS_PER_MAN);
		arena.setPctMagicPerLevel(PCT_MAGIC_PER_LEVEL);
		arena.runSim();
	}

	/**
	*  Report unit header information.
	*/
	void reportHeader () {
		System.out.println(menType + ", " + menTotal + " Total");
		if (menType.getNotes().length() > 1)
			System.out.println("Notes: " + menType.getNotes());
		System.out.println();
	}

	/**
	*  Report on wizard-types.
	*/
	void reportWizard () {
		if (menType.hasCasters() 
			&& (menTotal >= 300
				|| (menTotal >= 200 && Dice.coinFlip())))
		{
			int level = (Dice.roll(6) <= 4 ? 10 : 11);
			Character w = new Character("Human", "Wizard", level, null);
			Character.setPctMagicPerLevel(PCT_WIZARD_MAGIC_PER_LEVEL);
			w.setAlignment(Alignment.randomBias(menType.getAlignment()));
			w.setBasicEquipment();
			w.boostMagicItemsToLevel();
			System.out.println(w);
		}	
	}

	/**
	*  Report on leader-types.
	*/
	void reportLeaders () {
		List<Monster> list = arena.getTopFighters(NUM_LEADERS_TO_PRINT);
		for (Monster leader: list) {
			System.out.println(leader);		
		}
		System.out.println();
	}

	/**
	*  Report on troop-types.
	*/
	void reportTroops () {
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
		marshal.printBanner();
		marshal.parseArgs(args);
		if (marshal.exitAfterArgs()) {
			marshal.printUsage();
		}
		else {
			marshal.assembleMen();
		}
	}
}

