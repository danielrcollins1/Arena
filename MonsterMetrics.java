import java.util.*;
import java.util.regex.*;
import java.util.function.*;

/******************************************************************************
*  Application to measure monster power levels.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-10
******************************************************************************/

public class MonsterMetrics {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/*
	* A 12th level fighter with sweep attacks can beat around
	* 80 orcs, 150 kobolds, or 240 rats (1 hp). The MAX_ENEMIES
	* value below is set to handle numbers like these.
	*/
	final int MAX_LEVEL = 12;
	final int MAX_ENEMIES = 256;
	final int DEFAULT_FIGHTS_GENERAL = 100;
	final int DEFAULT_FIGHTS_SPOTLIGHT = 1000;
	final int DEFAULT_MAGIC_PER_LEVEL_PCT = 15;
	final int DEFAULT_WIZARD_RATIO = 4;
	final int GRAPH_Y_INTERVAL = 5;
	final Armor.Type DEFAULT_ARMOR = Armor.Type.Chain;

	/*
	* Constants for best level and number matches.
	*/
	final int MAX_OPP_LEVEL = 200;
	final int MAX_MON_NUMBER = 500;
	final int DEFAULT_PARTY_SIZE = 5;

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Statistics for battles at a given search point. */
	class BattleStats {

		// Fields
		double winRatio;
		double avgTurns;	
		boolean okMatchup;

		// Methods
		BattleStats (double wins, double turns, boolean accept) {
			winRatio = wins;
			avgTurns = turns;
			okMatchup = accept;
		}
	};

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Single monster option for measurement. */
	Monster spotlightMonster;

	/** Number of fights to run per search space point. */
	int numberOfFights;

	/** Armor type for battling fighters. */
	Armor.Type armorType;

	/** Chance per level for magic sword. */
	int pctMagicPerLevel;

	/** Fraction of party who are wizards. */
	int wizardFrequency;

	/** PC level for sample fight run. */
	int commandPartyLevel;

	/** PC expected party size. */
	int expectedPartySize;
	
	/** Monster number for single matchup. */
	int commandMonsterNumber;

	/** Flag to display unknown special abilities. */
	boolean displayUnknownSpecials;

	/** Flag to display only revised EHD values. */
	boolean displayOnlyRevisions; 

	/** Flag to display equated fighters per level. */
	boolean displayEquatedFighters; 

	/** Flag to display equated fighters HD per level. */
	boolean displayEquatedFightersHD; 

	/** Flag to graph equated fighters HD. */
	boolean graphEquatedFightersHD; 

	/** Flag to show parity win ratios. */
	boolean showParityWinRatios;

	/** Flag to show quick battle stats exclusively. */
	boolean showQuickBattleStats;
	
	/** Flag to show suggested best level match. */
	boolean showBestLevelMatch;	

	/** Flag to show suggested best number matches. */
	boolean showBestNumberMatch;

	/** Did we print anything on this run? */
	boolean printedSomeMonster;

	/** Show one sample fight at parity numbers. */
	boolean doShowSampleFight;

	/** Assess matchup at specified party level & monster number. */
	boolean doAssessSingleMatchup;
	
	/** Flag to make a table of best-number-match values. */
	boolean makeBNMTable;

	/** Should we wait for a keypress to start (for profiler)? */
	boolean waitForKeypress;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	/** Process start time. */
	long timeStart;

	/** Process stop time. */
	long timeStop;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Basic constructor.
	*/
	MonsterMetrics () {
		Dice.initialize();
		armorType = DEFAULT_ARMOR;
		pctMagicPerLevel = DEFAULT_MAGIC_PER_LEVEL_PCT;
		wizardFrequency = DEFAULT_WIZARD_RATIO;
		expectedPartySize = DEFAULT_PARTY_SIZE;
		SpellMemory.setPreferCastableSpells(true);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print program banner.
	*/
	private void printBanner () {
		System.out.println("OED Monster Metrics");
		System.out.println("-------------------");
	}

	/**
	*  Print usage.
	*/
	private void printUsage () {
		System.out.println("Usage: MonsterMetrics [monster] [options]");
		System.out.println("  By default, measures all monsters in MonsterDatabase file.");
		System.out.println("  Skips any monsters marked as having undefinable EHD (?)");
		System.out.println("  If monster is named, measures that monster at increased fidelity.");
		System.out.println("  Options include:");
		System.out.println("\t-a armor worn by fighters (=n, l, c, p; "
			+ "default " + DEFAULT_ARMOR + ")");
		System.out.println("\t-b set filename for an alternate monster database");
		System.out.println("\t-d display equated fighter hit dice per level");
		System.out.println("\t-e display equated fighters per level");
		System.out.println("\t-f number of fights per point in search space " 
			+ "(default =" + DEFAULT_FIGHTS_GENERAL + ")");
		System.out.println("\t-g graph power per level for each monster");
		System.out.println("\t-h assess matchup given monster number, party level (-h:#:#)");
		System.out.println("\t-k wait for keypress to start processing");		
		System.out.println("\t-l show suggested best level match for expected-size party");
		System.out.println("\t-m chance for magic weapon bonus per level " 
			+ "(default =" + DEFAULT_MAGIC_PER_LEVEL_PCT + ")");
		System.out.println("\t-n show suggested best number matches for various levels");
		System.out.println("\t-p show EHD-parity win ratios vs. expected-size party");
		System.out.println("\t-q show only quick key stats in table form");
		System.out.println("\t-r display only monsters with revised EHD from database");
		System.out.println("\t-s show a single sample fight (optional party level, -s:#)");
		System.out.println("\t-t make a table of best-number-match values");
		System.out.println("\t-u display any unknown special abilities in database");
		System.out.println("\t-w use fighter sweep attacks (by level vs. 1 HD)");
		System.out.println("\t-x set expected size of PC party "
			+ "(default =" + DEFAULT_PARTY_SIZE + ")");
		System.out.println("\t-z fraction of wizards in party "
			+ "(default =" + DEFAULT_WIZARD_RATIO + ")");
		System.out.println();
	}

	/**
	*  Process arguments, including pre- and post-processing
	*/
	private void processArgs (String[] args) {
		parseArgs(args);

		// Set number of fights if not done yet
		if (numberOfFights <= 0) {
			numberOfFights = (spotlightMonster == null) ? 
				DEFAULT_FIGHTS_GENERAL : DEFAULT_FIGHTS_SPOTLIGHT;
		}			
		
		// Check that sample fight run has specified monster
		if (doShowSampleFight && (spotlightMonster == null)) {
			System.err.println("Sample fight run requires spotlight monster specified.");
			exitAfterArgs = true;
		}
		
		// Check that single matchup option has specified monster
		if (doAssessSingleMatchup && (spotlightMonster == null)) {
			System.err.println("Matchup assessment requires spotlight monster specified.");
			exitAfterArgs = true;
		}
	}

	/**
	*  Parse arguments.
	*/
	private void parseArgs (String[] args) {
		for (String s: args) {
			if (s.length() > 1 && s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'a': armorType = getArmorType(s); break;
					case 'b': MonsterDatabase.setDatabaseFilename(
									getParamString(s)); break;
					case 'd': displayEquatedFightersHD = true; break;
					case 'e': displayEquatedFighters = true; break;
					case 'f': numberOfFights = getParamInt(s); break;
					case 'g': graphEquatedFightersHD = true; break;
					case 'h': parseSingleMatchupOption(s); break;
					case 'k': waitForKeypress = true; break;
					case 'l': showBestLevelMatch = true; break;
					case 'm': pctMagicPerLevel = getParamInt(s); break;
					case 'n': showBestNumberMatch = true; break;
					case 'p': showParityWinRatios = true; break;
					case 'q': showQuickBattleStats = true; break;
					case 'r': displayOnlyRevisions = true; break;
					case 's': parseSampleFightOption(s); break;
					case 't': makeBNMTable = true; break;
					case 'u': displayUnknownSpecials = true; break;
					case 'w': Character.setSweepAttacks(true); break;
					case 'x': expectedPartySize = getParamInt(s); break;
					case 'z': wizardFrequency = getParamInt(s); break;
					default: exitAfterArgs = true; break;
				}
			}
			else {
				if (spotlightMonster == null) {
					spotlightMonster = MonsterDatabase.getInstance().getByRace(s);
					if (spotlightMonster == null) {
						System.err.println("Monster not found in database.");
						exitAfterArgs = true;     
					}
				}
			}
		}
	}

	/**
	*  Get integer following equals sign in command parameter.
	*/
	private int getParamInt (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			try {
				return Integer.parseInt(s.substring(3));
			}
			catch (NumberFormatException e) {
				System.err.println("Error: Could not read integer argument: " + s);
			}
		}
		exitAfterArgs = true;
		return -1;
	}

	/**
	*  Get string following equals sign in command parameter.
	*/
	private String getParamString (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			return s.substring(3);		
		}	
		exitAfterArgs = true;
		return "";
	}

	/**
	*  Get armor type from command parameter.
	*/
	private Armor.Type getArmorType (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (s.charAt(3)) {
				case 'n': return null; 
				case 'l': return Armor.Type.Leather;
				case 'c': return Armor.Type.Chain;
				case 'p': return Armor.Type.Plate;
			} 
		}
		exitAfterArgs = true;
		return null;
	}

	/**
	*  Parse option for a sample fight.
	*/
	private void parseSampleFightOption (String s) {
		doShowSampleFight = true;
		Pattern p = Pattern.compile("(:\\d+)?");
		Matcher m = p.matcher(s.substring(2));
		if (m.matches()) {
			if (m.group(1) != null) {
				commandPartyLevel = Integer.parseInt(
					m.group(1).substring(1));
			}
		}
		else {
			System.err.println("Could not parse sample-fight command.");
			exitAfterArgs = true;
		}
	}

	/**
	*  Parse option for a single matchup.
	*/
	private void parseSingleMatchupOption(String s) {
		doAssessSingleMatchup = true;
		Pattern p = Pattern.compile("(:\\d+)(:\\d+)");
		Matcher m = p.matcher(s.substring(2));
		if (m.matches()) {
			commandMonsterNumber = Integer.parseInt(
					m.group(1).substring(1));
			commandPartyLevel = Integer.parseInt(
					m.group(2).substring(1));
		}
		else {
			System.err.println("Could not parse single-matchup command.");
			exitAfterArgs = true;
		}
	}

	/**
	*  Report monster metrics as commanded.
	*/
	private void reportMonsters () {
		if (spotlightMonster == null)
			reportAllMonsters();  
		else
			reportOneMonster(spotlightMonster);
	}

	/**
	*  Report number of fighters at each level to match all monsters.
	*/
	private void reportAllMonsters () {

		// Analyze each monster
		for (Monster m: MonsterDatabase.getInstance()) {
			if (!m.hasUndefinedEHD()) {
				reportOneMonster(m);
			}
		}
		
		// Give notice if no monsters reported
		if (!printedSomeMonster) {
			System.out.println(displayOnlyRevisions ?
				"All EHDs in database verified as valid." :
				"No measurable monsters found in database.");
		}
		System.out.println();
	}

	/**
	*  Report equated fighters and estimated EHD for one monster.
	*/
	private void reportOneMonster (Monster monster) {

		// Compute EHD values
		double[] eqFighters = getEquatedFighters(monster);
		double[] eqFightersHD = getEquatedFightersHD(eqFighters);
		double estEHD = getDblArrayHarmonicMean(eqFightersHD);
		boolean reviseEHD = !isEHDClose(monster.getEHD(), estEHD);

		// Print stats as requested
		if (reviseEHD || !displayOnlyRevisions || spotlightMonster == monster) {
			System.out.println(monster.getRace() + ": "
				+ "Old EHD " + monster.getEHD() + ", "
				+ "New EHD " + Math.round(estEHD)
				+ " (" + roundDbl(estEHD, 2) + ")");
			if (displayEquatedFighters)
				System.out.println("\tEF " + toString(eqFighters, 1));
			if (displayEquatedFightersHD)
				System.out.println("\tEFHD " + toString(eqFightersHD, 1));
 			if (graphEquatedFightersHD)
 				graphDblArray(eqFightersHD);
			if (showParityWinRatios) {
				double[] parityWins = getParityWinRatios(monster);
				System.out.println("\tPWR " + toString(parityWins, 2));
			}
			if (showBestNumberMatch) {
				int[] bestNumbers = getBestNumberArray(monster);
				System.out.println("\tBNM " + Arrays.toString(bestNumbers));
			}
			if (showBestLevelMatch) {
				int bestLevelMatch = getBestLevelMatch(monster);
				System.out.println("\tBest level match: " + bestLevelMatch);
			}								
			if (anySpecialPrinting())
				System.out.println();
			printedSomeMonster = true;
		}
	}

	/**
	*  Any special printing done per monster?
	*/
	private boolean anySpecialPrinting () {
		return displayEquatedFighters	|| displayEquatedFightersHD 
			|| graphEquatedFightersHD || showParityWinRatios
			|| showBestLevelMatch || showBestNumberMatch;
	}

	/**
	*  Determine if EHDs are relatively close.
	*
	*  Note the -r switch is used for regression testing of the whole Arena suite.
	*  (Gives visibility if a code modification has unexpected side effects.)
	*
	*  There's natural variation in estimated EHD due to random sampling, 
	*  and more at higher levels, which we don't want to spuriously flag revised values. 
	*  So we implement an exponential error bar before triggering a report.
	*  
	*  Also, there are some low-level monsters with EHDs right around the halfway point
	*  between two integers that would trigger a report half the time, if we compared
	*  integer values, and had an error bar below 1.
	*  
	*  Therefore, we compare decimal values, and only trigger a report if there's
	*  at least a 2/3 unit difference. For the monsters in question, we made the manual
	*  choice in the database of leaning toward the actual HD. In some cases this may
	*  mask the fact that the true EHD is under the halfway point by a tiny fraction.
	*
	*  E.g.: Zombie, Caveman, Gnoll, Bugbear, Ogre, Hill Giant.
	*/
	private boolean isEHDClose (double oldEHD, double newEHD) {
		final double ERRBAR_MIN = 2/3.;
		final double ERRBAR_COEFF = 0.7;
		double errBar = ERRBAR_COEFF * Math.sqrt(oldEHD);
		errBar = Math.max(errBar, ERRBAR_MIN);
		return Math.abs(oldEHD - newEHD) <= errBar;
	} 

	/**
	*  Get equated fighters per level for a monster.
	*/
	private double[] getEquatedFighters (Monster monster) {
		double[] array = new double[MAX_LEVEL];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			int match = matchMonsterToFighters(monster, level);
			array[level - 1] = (match > 0 ? match : 1./(-match));
		}  
		return array;
	}

	/**
	*  Get equated fighter hit dice for a monster.
	*/
	private double[] getEquatedFightersHD (double[] equatedFighters) {
		double[] array = new double[MAX_LEVEL];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			array[level - 1] = level * equatedFighters[level - 1];
		}
		return array;
	}

	/**
	*  Create string from a double array, to given precision.
	*/
	private String toString(double[] array, int precision) {
		String s = "[";
		for (int i = 0; i < array.length; i++) {
			s += roundDbl(array[i], precision);
			if (i < array.length - 1) {
				s += ", ";
			}
		}
		s += "]";
		return s;		
	}

	/**
	*  Round a double to an indicated precision.
	*/
	private double roundDbl (double val, int precision) {
		double div = Math.pow(10, precision);
		return Math.round(val * div) / div;
	}
	
	/**
	*  Get the maximum of a double array.
	*/
	private double getDblArrayMax (double[] array) {
		double max = Double.MIN_VALUE;
		for (double val: array) {
			if (val > max)
				max = val;
		}
		return max;
	}

	/**
	*  Get the minimum of a double array.
	*/
	private double getDblArrayMin (double[] array) {
		double min = Double.MAX_VALUE;
		for (double val: array) {
			if (val < min)
				min = val;
		}
		return min;
	}

	/**
	*  Get the mean of a double array.
	*/
	private double getDblArrayMean (double[] array) {
		double sum = 0.0;
		for (double val: array) {
			sum += val;
		}
		return sum / array.length;
	}

	/**
	*  Get the harmonic mean of a double array.
	*/
	private double getDblArrayHarmonicMean (double[] array) {
		double sum = 0.0;
		for (double val: array) {
			sum += 1/val;
		}
		return array.length / sum;
	}

	/**
	*  Print a graph of a double array.
	*/
	private void graphDblArray (double[] array) {
		System.out.println();
		double maxVal = getDblArrayMax(array);
		long maxStepY = Math.round(maxVal / GRAPH_Y_INTERVAL);

		// Graph body
		for (long ystep = maxStepY; ystep >= 0; ystep--) {
			System.out.print("|");
			for (int x = 1; x <= MAX_LEVEL; x++) {
				double val = array[x - 1];
				long valStep = Math.round(val / GRAPH_Y_INTERVAL);
				boolean atThisHeight = (valStep == ystep);
				System.out.print(atThisHeight ? "*" : " ");
			}
			System.out.println();
		}
		
		// X-axis
		System.out.print("+");
		for (int x = 1; x <= MAX_LEVEL; x++) {
			System.out.print("-");
		}
		System.out.println("\n");
	}

	/**
	*  Match fighters of given level to monster of one type.
	*  @param monster Type of monster.
	*  @param fighterLevel Level of fighter.
	*  @return If positive, one monster to many fighters; 
	*     if negative, one fighter to many monsters. 
	*/
	private int matchMonsterToFighters (Monster monster, int fighterLevel) {

		// Consider one monster to many fighters
		int numFighters = matchFight(
			n -> ratioMonstersBeatFighters(monster, 1, fighterLevel, n, true)); 
		if (numFighters > 1) return numFighters;

		// Consider one fighter to many monsters.
		int numMonsters = matchFight(
			n -> ratioMonstersBeatFighters(monster, n, fighterLevel, 1, false));
		if (numMonsters > 1) return -numMonsters;

		// One monster to one fighter
		return 1;
	}

	/**
	*  Search for a matched fight based on some parameter.
	*  @param winRatioFunc Must be increasing in parameter.
	*/
	private int matchFight (Function<Integer, Double> winRatioFunc) {
		int low = 0;
		int high = MAX_ENEMIES;

		// Binary search on parameter
		while (high - low > 1) {
			int mid = (low + high) / 2;
			double midVal = winRatioFunc.apply(mid);
			if (midVal < 0.5)
				low = mid;
			else
				high = mid;
		}

		// Choose from adjacent values
		double lowVal = winRatioFunc.apply(low);
		double highVal = winRatioFunc.apply(high);
		return isCloserToHalf(lowVal, highVal) ? low : high;
	}

	/**
	*  Is the first number closer to one half than the second number?
	*  @return true if val1 is at least as close to 0.5 as val2
	*/
	private boolean isCloserToHalf (double val1, double val2) {
		double diffVal1 = Math.abs(0.5 - val1);
		double diffVal2 = Math.abs(0.5 - val2);  
		return diffVal1 <= diffVal2;	
	}

	/**
	*  Find the probability that these monsters beat these fighters.
	*  @param invert If true, returns chance of fighters beating monsters.
	*/
	private double ratioMonstersBeatFighters (
		Monster monsterType, int monsterNumber, 
		int fighterLevel, int fighterNumber, 
		boolean invert) 
	{
		assert (monsterType != null); 
		if (fighterLevel < 0) return 1.0;

		int wins = 0;
		for (int fight = 1; fight <= numberOfFights; fight++)   {

			// Fight & track if monster wins
			Party ftrParty = makeFighterParty(fighterLevel, fighterNumber);
			Party monParty = new Party(monsterType, monsterNumber);
			FightManager manager = new FightManager(ftrParty, monParty);
			if (manager.fight() == monParty) {
				wins++;
			}			
			
			// Shortcut a lopsided matchup.
			// Tell if ratio over 0.5 at 2-sigma (97.7%) confidence
			// See Weiss Introductory Statistics:
			// Procedure 12.2, handicap enemy 10 fights.
			double z = Math.sqrt(fight + 10) 
				* ((double) 2 * wins/(fight + 10) - 1);
			if (z >= 2.0)
 				return computeWinRatio(wins, fight, invert);
		}

		return computeWinRatio(wins, numberOfFights, invert);
	}

	/**
	*  Create a specified party of fighters.
	*/
	private Party makeFighterParty (int level, int number) {
		Party party = new Party();
		for (int i = 0; i < number; i++) {
			Character character;
			if (wizardFrequency > 0 
					&& Dice.roll(wizardFrequency) == 1)
				character = newWizard(level);
			else
				character = newFighter(level);
			party.add(character);
		}
		return party;
	}

	/**
	*  Compute the winRatio at end of fight sequence.
	*/
	private double computeWinRatio (int wins, int fights, boolean invert) {
		double winRatio = (double) wins / fights;
		return invert ? 1 - winRatio : winRatio;
	}

	/**
	*  Create a new fighter of the indicated level.
	*  Equipment is kept to fixed baseline, with only
	*  magic swords to hit monsters as required.
	*  (So: Do not use standard Character equip or magic.)
	*/
	private Character newFighter (int level) {
		Character f = new Character("Human", "Fighter", level, null); 
		f.setArmor(Armor.makeType(armorType));
		f.setShield(Armor.makeType(Armor.Type.Shield));
		f.addEquipment(newSword(level));
		f.addEquipment(Weapon.silverDagger());
		f.addEquipment(Weapon.torch());
		return f;
	}

	/**
	*  Create a sword for a new fighter.
	*/
	private Weapon newSword (int level) {
		int bonus = 0;
		for (int i = 0; i < level; i++) {
			if (Dice.rollPct() <= pctMagicPerLevel) {
				bonus++;
			}
		}
		return Weapon.sword(bonus);
	}

	/**
	*  Create a new wizard of the indicated level.
	*  Equipment is kept to fixed baseline.
	*  (So: Do not use standard Character equip or magic.)
	*/
	private Character newWizard (int level) {
		Character f = new Character("Human", "Wizard", level, null); 
		f.addEquipment(Weapon.silverDagger());
		f.addEquipment(Weapon.torch());
		return f;
	}

	/**
	*  Display unknown special abilities if desired.
	*/
	private  void displayUnknownSpecials () {
		if (displayUnknownSpecials) {
			MonsterDatabase db = MonsterDatabase.getInstance();
			SpecialUnknownList list = SpecialUnknownList.getInstance();
			System.out.println("Unknown specials: " + list + "\n");
		}
	}

	/**
	*  Compute stats for monster vs. standard-sized party at a given level.
	*  Here we assume a linear matching function based on database EHD
	*  (similar to rough idea on Vol-3, p. 11)
	*/
	private BattleStats getBattleStats (Monster monster, int ftrLevel) {

		// Check for 0-EHD monster
		if (monster.getEHD() <= 0)
			return new BattleStats(-1, -1, false);

		// Compute fair numbers
		int monNumber = getBalancedMonsterNumbers(
			monster, ftrLevel, expectedPartySize);
		if (monNumber <= 0)
			return new BattleStats(-1, -1, false);

		// Run fights
		long monWins = 0;
		long sumTurns = 0;
		for (int fight = 0; fight < numberOfFights; fight++) {
			Party ftrParty = makeFighterParty(ftrLevel, expectedPartySize);
			Party monParty = new Party(monster, monNumber);
			FightManager manager = new FightManager(ftrParty, monParty);
			if (manager.fight() == monParty) monWins++;
			sumTurns += manager.getTurnCount();
		}
		
		// Return result
		double winRatio = (double) monWins / numberOfFights;
		double avgTurns = (double) sumTurns / numberOfFights;
		return new BattleStats(winRatio, avgTurns, true);
	}

	/**
	*  Compute fair monster numbers by EHD.
	*  Assumes fixed party size and level.
	*  @return fair number of monsters (possibly 0)
	*/
	private int getBalancedMonsterNumbers (Monster monster, 
		int ftrLevel, int partySize) 
	{
		return (int) Math.round((double) 
			ftrLevel * partySize / monster.getEHD());
	}

	/**
	*  Print simple report of parity battle stats.
	*/
	private void printQuickBattleStats () {
		System.out.println("Monster\tEHD\tBLM\tWin Ratio\tAvg Turns");
		if (spotlightMonster == null) {
			for (Monster m: MonsterDatabase.getInstance()) {
				if (!m.hasUndefinedEHD())
					printQuickBattleStats(m);
			}
		}
		else {
			printQuickBattleStats(spotlightMonster);
		}
		System.out.println();
	}

	/**
	*  Print simple parity battle stats for one monster.
	*/
	private void printQuickBattleStats (Monster monster) {

		// Get stats
		int ftrLevel = Math.min(monster.getEHD(), MAX_LEVEL);
		BattleStats stats = getBattleStats(monster, ftrLevel);
		int bestLevelMatch = getBestLevelMatch(monster);

		// Print stats
		System.out.println(monster.getRace()
			+ "\t" + monster.getEHD()
			+ "\t" + bestLevelMatch
			+ "\t" + stats.winRatio
			+ "\t" + stats.avgTurns);
	}

	/**
	*  Compute an array of win ratios for monster at 
	*  linear-EHD-parity vs. standard party at various levels.
	*/
	private double[] getParityWinRatios (Monster monster) {
		double array[] = new double[MAX_LEVEL];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			BattleStats stats = getBattleStats(monster, level);
			array[level - 1] = stats.winRatio;
		}	
		return array;
	}

	/**
	*  Get monster win ratio for same-size parties.
	*/
	private double ratioMonstersBeatFighters(Monster monster, int ftrLevel) {
		return ratioMonstersBeatFighters(monster, expectedPartySize, 
			ftrLevel, expectedPartySize, false);	
	}

	/**
	*  Get the best level match for a given monster.
	*  Assumes monster numbers fixed at standard party size.
	*  Searches for level where they're a match for same-size party of PCs
	*  (i.e., closest to 50% chance to win against each other)
	*  @return level at which monsters & PCs are closest to 50% win ratio
	*/
	private int getBestLevelMatch (Monster monster) {
		int lowLevel = 1;
		int highLevel = monster.getHD();

		// Raise the high-level bound until the monster loses
		double maxRatio = ratioMonstersBeatFighters(monster, highLevel);
		while (maxRatio > 0.5) {
			highLevel *= 2;
			maxRatio = ratioMonstersBeatFighters(monster, highLevel);
			if (highLevel > MAX_OPP_LEVEL) {
				highLevel = MAX_OPP_LEVEL;
				break;
			}
		}

		// Binary search on level
		while (highLevel - lowLevel > 1) {
			int midLevel = (lowLevel + highLevel) / 2;
			double midRatio = ratioMonstersBeatFighters(monster, midLevel);
			if (midRatio > 0.5)
				lowLevel = midLevel;
			else
				highLevel = midLevel;
		}

		// Choose from adjacent values
		double lowRatio = ratioMonstersBeatFighters(monster, lowLevel);
		double highRatio = ratioMonstersBeatFighters(monster, highLevel);
		return isCloserToHalf(lowRatio, highRatio) ? lowLevel : highLevel;
	} 

	/**
	*  Get monster win ratio for variable number of monsters.
	*/
	private double ratioMonstersBeatFighters(Monster monster, int monNumber, int ftrLevel) {
		return ratioMonstersBeatFighters(monster, monNumber, 
			ftrLevel, expectedPartySize, false);	
	}

	/**
	*  Get the best number-appearing match for a given monster.
	*  Assumes opposing PC party levels and size are fixed.
	*  Searches for number where they're a match for same-size party of PCs
	*  (i.e., closest to 50% chance to win against each other)
	*  @return level at which monsters & PCs are closest to 50% win ratio
	*/
	private int getBestNumberMatch (Monster monster, int ftrLevel) {
		int lowNumber = 0;
		int highNumber = MAX_MON_NUMBER;

		// Binary search on number
		while (highNumber - lowNumber > 1) {
			int midNumber = (lowNumber + highNumber) / 2;
			double midRatio = ratioMonstersBeatFighters(monster, midNumber, ftrLevel);
			if (midRatio < 0.5)
				lowNumber = midNumber;
			else
				highNumber = midNumber;
		}

		// Choose from adjacent values
		double lowRatio = ratioMonstersBeatFighters(monster, lowNumber);
		double highRatio = ratioMonstersBeatFighters(monster, highNumber);
		return isCloserToHalf(lowRatio, highRatio) ? lowNumber : highNumber;
	} 

	/**
	*  Construct an array of best number matches per level.
	*/
	private int[] getBestNumberArray (Monster monster) {
		int array[] = new int[MAX_LEVEL];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			array[level - 1] = getBestNumberMatch(monster, level);
		}
		return array;
	}

	/**
	*  Print a table of BNM values.
	*/
	private void printBNMTable () {
		System.out.println("Best Numerical Matches by Party Level");
		if (spotlightMonster == null) {
			for (Monster m: MonsterDatabase.getInstance()) {
				if (!m.hasUndefinedEHD())
					printBNMRow(m);
			}
		}
		else {
			printBNMRow(spotlightMonster);
		}
		System.out.println();
	}

	/**
	*  Print one monster in a BNM table.
	*/
	private void printBNMRow (Monster monster) {
		int[] array = getBestNumberArray(monster);
		System.out.print(monster.getRace());
		for (int val: array) {
			System.out.print("\t" + val);
		}	
		System.out.println();
	}

	/**
	*  Run a sample fight.
	*  Assumes standard party size, PC level 1 to MAX_LEVEL,
	*  and monster numbers at parity total EHD.
	*/
	private void showSampleFight () {
		assert(spotlightMonster != null);
		FightManager.setPlayByPlayReporting(true);
		Monster monster = spotlightMonster;

		// Check valid EHD
		if (monster.getEHD() <= 0) {
			System.out.println("Cannot compute number appearing for EHD 0 monster.\n");
			return;
		}		

		// Set up parties to fight
		int ftrLevel = (commandPartyLevel != 0) ? 
			commandPartyLevel : Math.min(monster.getEHD(), MAX_LEVEL);
		int monNumber = getBalancedMonsterNumbers(
			monster, ftrLevel, expectedPartySize);
		if (monNumber <= 0) 
			monNumber = 1;
		Party ftrParty = makeFighterParty(ftrLevel, expectedPartySize);
		Party monParty = new Party(monster, monNumber);
		FightManager manager = new FightManager(ftrParty, monParty);
		
		// Report on party composition
		System.out.println(monParty + " (EHD " + monster.getEHD() + ")\n");
		for (Monster c: ftrParty) {
			System.out.println(c.shortString());
		}
		System.out.println();

		// Run the fight
		manager.fight();
		System.out.println("Turns elapsed: " + manager.getTurnCount());
		System.out.println();
	}

	/**
	*  Assess a single specified matchup.
	*/
	private void assessSingleMatchup () {
		assert(spotlightMonster != null);
		Monster monster = spotlightMonster;

		// Report situation
		System.out.println("Monster party: " 
			+ commandMonsterNumber + " " + monster.getRace()
			+ (commandMonsterNumber == 1 ? "" : "s")
			+ " (EHD " + monster.getEHD() + ")");
		System.out.println("Character party: "
			+ expectedPartySize + " of level " + commandPartyLevel);

		// Do the assessment		
		double winRatio = ratioMonstersBeatFighters (
			spotlightMonster, commandMonsterNumber, commandPartyLevel);
		double percent = winRatio * 100;			
		System.out.println("Monster win ratio: " + percent + "%\n");
	}

	/**
	*  Start the process timer.
	*/
	private void startClock () {
		timeStart = System.currentTimeMillis();
	}

	/**
	*  Stop the process timer & report.
	*/
	private void stopClock () {
		timeStop = System.currentTimeMillis();
		if (spotlightMonster == null) {
			long secDiff = (timeStop - timeStart) / 1000;
			long minDisplay = secDiff / 60;
			long secDisplay = secDiff % 60;
			System.out.println("Process elapsed time: " 
				+ minDisplay + " min " + secDisplay + " sec\n");
		}
	}

	/**
	*  Main application method.
	*/
	public static void main (String[] args) {
		MonsterMetrics metrics = new MonsterMetrics();
		metrics.printBanner();
		metrics.processArgs(args);
		if (metrics.exitAfterArgs) {
			metrics.printUsage();
		}
		else {
			if (metrics.waitForKeypress) {
				System.out.println("Press Enter to start...");
				try { System.in.read(); } 
				catch (Exception e) {};
			}
			metrics.startClock();
			metrics.displayUnknownSpecials();
			if (metrics.doShowSampleFight)
				metrics.showSampleFight();
			else if (metrics.doAssessSingleMatchup)
				metrics.assessSingleMatchup();	
			else if (metrics.makeBNMTable)
				metrics.printBNMTable();			
			else if (metrics.showQuickBattleStats)
				metrics.printQuickBattleStats();
			else		
				metrics.reportMonsters();
			metrics.stopClock();
		}
	}
}
