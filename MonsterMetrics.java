import java.util.function.Function;

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

	final int MAX_LEVEL = 12;
	final int MAX_ENEMIES = 256;
	final int GRAPH_Y_INTERVAL = 5;
	final int DEFAULT_FIGHTS_GENERAL = 100;
	final int DEFAULT_FIGHTS_SPOTLIGHT = 1000;
	final int DEFAULT_PCT_MAGIC_SWORD_PER_LEVEL = 15;
	final Armor.Type DEFAULT_ARMOR = Armor.Type.Chain;

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
	int pctMagicSwordPerLevel;

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

	/** Did we print anything on this run? */
	boolean printedSomeMonster;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Basic constructor.
	*/
	MonsterMetrics () {
		Dice.initialize();
		spotlightMonster = null;
		numberOfFights = DEFAULT_FIGHTS_GENERAL;
		armorType = DEFAULT_ARMOR;
		pctMagicSwordPerLevel = DEFAULT_PCT_MAGIC_SWORD_PER_LEVEL;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print program banner.
	*/
	void printBanner () {
		System.out.println("OED Monster Metrics");
		System.out.println("-------------------");
	}

	/**
	*  Print usage.
	*/
	public void printUsage () {
		System.out.println("Usage: MonsterMetrics [monster] [options]");
		System.out.println("  By default, measures all monsters in MonsterDatabase file.");
		System.out.println("  Skips any monsters marked as having undefinable EHD (?)");
		System.out.println("  If monster is named, measures that monster at increased fidelity.");
		System.out.println("  Options include:");
		System.out.println("\t-a armor worn by fighters (=l, c, or p; "
			+ "default " + DEFAULT_ARMOR + ")");
		System.out.println("\t-b set filename for an alternate monster database");
		System.out.println("\t-d display equated fighter hit dice per level");
		System.out.println("\t-e display equated fighters per level");
		System.out.println("\t-f number of fights per point in search space " 
			+ "(default =" + DEFAULT_FIGHTS_GENERAL + ")");
		System.out.println("\t-g graph power per level for each monster");
		System.out.println("\t-m chance for magic weapon bonus per level " 
			+ "(default =" + DEFAULT_PCT_MAGIC_SWORD_PER_LEVEL + ")");
		System.out.println("\t-r display only monsters with revised EHD from database");
		System.out.println("\t-u display any unknown special abilities in database");
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
					case 'a': armorType = getArmorType(s); break;
					case 'b': MonsterDatabase.setDatabaseFilename(
									getParamString(s)); break;
					case 'd': displayEquatedFightersHD = true; break;
					case 'e': displayEquatedFighters = true; break;
					case 'f': numberOfFights = getParamInt(s); break;
					case 'g': graphEquatedFightersHD = true; break;
					case 'm': pctMagicSwordPerLevel = getParamInt(s); break;
					case 'r': displayOnlyRevisions = true; break;
					case 'u': displayUnknownSpecials = true; break;
					case 'w': Character.setSweepAttacks(true); break;
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
					numberOfFights = DEFAULT_FIGHTS_SPOTLIGHT;
				}
			}
		}
	}

	/**
	*  Get integer following equals sign in command parameter.
	*/
	int getParamInt (String s) {
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
	String getParamString (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			return s.substring(3);		
		}	
		exitAfterArgs = true;
		return "";
	}

	/**
	*  Get armor type from command parameter.
	*/
	Armor.Type getArmorType (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (s.charAt(3)) {
				case 'l': return Armor.Type.Leather;
				case 'c': return Armor.Type.Chain;
				case 'p': return Armor.Type.Plate;
			} 
		}
		exitAfterArgs = true;
		return null;
	}

	/**
	*  Report monster metrics as selected.
	*/
	public void reportMonsters () {
		if (spotlightMonster == null)
			reportAllMonsters();  
		else
			reportOneMonster(spotlightMonster);
	}

	/**
	*  Report number of fighters at each level to match all monsters.
	*/
	void reportAllMonsters () {
		MonsterDatabase db = MonsterDatabase.getInstance();
		if (db == null) return;
		for (Monster m: db) {
			if (!m.hasUndefinedEHD()) {
				reportOneMonster(m);
			}
		}
		if (!printedSomeMonster) {
			if (displayOnlyRevisions)
				System.out.println("No revised EHDs found versus database.");
			else
				System.out.println("No measurable monsters found in database.");				
		}
		System.out.println();
	}

	/**
	*  Report equated fighters and estimated EHD for one monster.
	*/
	void reportOneMonster (Monster monster) {

		// Compute stats
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
			if (estEHD * 2 > getDblArrayMin(eqFightersHD) * MAX_LEVEL)
				System.out.println("\tEHD over half of harmonic mean threshold.");
			if (displayEquatedFighters)
				System.out.println("\tEF " + toString(eqFighters, 1));
			if (displayEquatedFightersHD)
				System.out.println("\tEFHD " + toString(eqFightersHD, 1));
 			if (graphEquatedFightersHD)
 				graphDblArray(eqFightersHD);
			if (anySpecialPrinting())
				System.out.println();
			printedSomeMonster = true;
		}
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
	boolean isEHDClose (double oldEHD, double newEHD) {
		final double ERRBAR_MIN = 2/3.;
		final double ERRBAR_COEFF = 0.7;
		double errBar = ERRBAR_COEFF * Math.sqrt(oldEHD);
		errBar = Math.max(errBar, ERRBAR_MIN);
		return Math.abs(oldEHD - newEHD) <= errBar;
	} 

	/**
	*  Any special printing done per monster?
	*/
	boolean anySpecialPrinting () {
		return displayEquatedFighters	|| displayEquatedFightersHD 
			|| graphEquatedFightersHD;
	}

	/**
	*  Get equated fighters per level for a monster.
	*/
	double[] getEquatedFighters (Monster monster) {
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
	double[] getEquatedFightersHD (double[] equatedFighters) {
		double[] array = new double[MAX_LEVEL];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			array[level - 1] = level * equatedFighters[level - 1];
		}
		return array;
	}

	/**
	*  Create string from a double array, to given precision.
	*/
	String toString(double[] array, int precision) {
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
	double roundDbl (double val, int precision) {
		double div = Math.pow(10, precision);
		return Math.round(val * div) / div;
	}
	
	/**
	*  Get the maximum of a double array.
	*/
	double getDblArrayMax (double[] array) {
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
	double getDblArrayMin (double[] array) {
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
	double getDblArrayMean (double[] array) {
		double sum = 0.0;
		for (double val: array) {
			sum += val;
		}
		return sum / array.length;
	}

	/**
	*  Get the harmonic mean of a double array.
	*/
	double getDblArrayHarmonicMean (double[] array) {
		double sum = 0.0;
		for (double val: array) {
			sum += 1/val;
		}
		return array.length / sum;
	}

	/**
	*  Print a graph of a double array.
	*/
	void graphDblArray (double[] array) {
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
	int matchMonsterToFighters (Monster monster, int fighterLevel) {

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
	int matchFight (Function<Integer, Double> winRatioFunc) {
		int low = 0;
		int high = MAX_ENEMIES;

		// Binary search on parameter
		while (low <= high) {
			int mid = (low + high)/2;
			double midVal = winRatioFunc.apply(mid);
			if (midVal < 0.5) {
				low = mid + 1;
			}
			else {
				high = mid - 1;
			}
		}

		// Choose from adjacent values
		double lowDiff = Math.abs(0.5 - winRatioFunc.apply(low));
		double highDiff = Math.abs(0.5 - winRatioFunc.apply(high));  
		return lowDiff < highDiff ? low : high;
	} 

	/**
	*  Find the probability that these monsters beat these fighters.
	*  @param invert If true, returns chance of fighters beating monsters.
	*/
	double ratioMonstersBeatFighters (
			Monster monsterType, int monsterNumber, 
			int fighterLevel, int fighterNumber, 
			boolean invert) {

		assert (monsterType != null); 
		if (fighterLevel < 0) return 1.0;

		int wins = 0;
		for (int i = 0; i < numberOfFights; i++)   {

			// Create monster party
			Party party1 = new Party();
			for (int j = 0; j < monsterNumber; j++) {
				party1.add(monsterType.spawn());
			}

			// Create fighter party
			Party party2 = new Party();
			for (int j = 0; j < fighterNumber; j++) {
				party2.add(newFighter(fighterLevel));
			}

			// Fight & see who wins
			FightManager.fight(party1, party2);
			if (party1.isLive()) {
				wins++;
			}                 
		}
		double winRatio = (double) wins / numberOfFights;
		return invert ? 1 - winRatio : winRatio;
	}

	/**
	*  Create a new fighter of the indicated level.
	*  Equipment is kept to fixed baseline, with only
	*  magic swords to hit monsters as required.
	*  (So: Do not use standard Character equip or magic.)
	*/
	Character newFighter (int level) {
		Character f = new Character("Human", "Fighter", level, null); 
		f.setArmor(Armor.makeType(armorType));
		f.setShield(Armor.makeType(Armor.Type.Shield));
		f.addEquipment(newSword(level));
		f.addEquipment(Weapon.silverDagger());
		return f;
	}

	/**
	*  Create a sword for a new fighter.
	*/
	Weapon newSword (int level) {
		int bonus = 0;
		for (int i = 0; i < level; i++) {
			if (Dice.roll(100) <= pctMagicSwordPerLevel) {
				bonus++;
			}
		}
		return Weapon.sword(bonus);
	}

	/**
	*  Display unknown special abilities if desired.
	*/
	public void displayUnknownSpecials () {
		if (displayUnknownSpecials) {
			MonsterDatabase db = MonsterDatabase.getInstance();
			SpecialUnknownList list = SpecialUnknownList.getInstance();
			System.out.println("Unknown specials: " + list + "\n");
		}
	}

	/**
	*  Main application method.
	*/
	public static void main (String[] args) {
		MonsterMetrics metrics = new MonsterMetrics();
		metrics.printBanner();
		metrics.parseArgs(args);
		if (metrics.exitAfterArgs) {
			metrics.printUsage();
		}
		else {
			metrics.displayUnknownSpecials();
			metrics.reportMonsters();
		}
	}
}
