import java.util.function.Function;

/******************************************************************************
*  Application to measure monster power levels.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-10
*  @version  1.03
******************************************************************************/

public class MonsterMetrics {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	final int MAX_LEVEL = 12;
	final int MAX_ENEMIES = 64;
	final int DEFAULT_FIGHTS_GENERAL = 100;
	final int DEFAULT_FIGHTS_SPOTLIGHT = 1000;
	final int DEFAULT_MAGIC_PER_LEVEL_PCT = 15;
	final Armor.Type DEFAULT_ARMOR = Armor.Type.Chain;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Single monster option for measurement. */
	Monster spotlightMonster;

	/** Number of fights to run per search space point. */
	int numberOfFights;

	/** Percent chance of magic boost per level. */
	int magicPerLevelPct;

	/** Armor type for battling fighters. */
	Armor.Type armorType;

	/** Flag to display unknown special abilities. */
	boolean displayUnknownSpecials;

	/** Flag to display only revised EHD values. */
	boolean displayOnlyRevisions; 

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
		magicPerLevelPct = DEFAULT_MAGIC_PER_LEVEL_PCT;
		armorType = DEFAULT_ARMOR;
		displayUnknownSpecials = false;
		displayOnlyRevisions = false;
		exitAfterArgs = false;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print usage.
	*/
	public void printUsage () {
		System.out.println();
		System.out.println("Usage: MonsterMetrics [monster] [options]");
		System.out.println("  By default, measures all monsters in MonsterDatabase file.");
		System.out.println("  Skips any monsters marked as having undefinable EHD (*)");
		System.out.println("  If monster is named, measures that monster at increased fidelity.");
		System.out.println("  Options include:");
		System.out.println("\t-a armor worn by opposing fighters: =l, c, or p "
			+ "(default " + DEFAULT_ARMOR + ")");
		System.out.println("\t-b chance for magic weapon bonus per level " 
			+ "(default =" + DEFAULT_MAGIC_PER_LEVEL_PCT + ")");
		System.out.println("\t-f number of fights per point in search space " 
			+ "(default =" + DEFAULT_FIGHTS_GENERAL + ")");
		System.out.println("\t-r display only monsters with revised EHD from database");
		System.out.println("\t-u display any unknown special abilities in database");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	public void parseArgs (String[] args) {
		for (String s: args) {
			if (s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'a': armorType = getArmorType(s); break;
					case 'b': magicPerLevelPct = getParamInt(s); break;
					case 'f': numberOfFights = getParamInt(s); break;
					case 'r': displayOnlyRevisions = true; break;
					case 'u': displayUnknownSpecials = true; break;
					default: exitAfterArgs = true; break;
				}
			}
			else {
				if (spotlightMonster == null) {
					spotlightMonster = MonsterDatabase.getInstance().getByRace(s);
					if (spotlightMonster == null) {
						exitAfterArgs = true;     
					}
					numberOfFights = DEFAULT_FIGHTS_SPOTLIGHT;
				}
			}
		}
	}

	/**
	*  Should we exit after parsing arguments?
	*/
	public boolean exitAfterArgs () {
		return exitAfterArgs;
	}

	/**
	*  Get integer following equals sign in command parameter.
	*/
	int getParamInt (String s) {
		if (s.charAt(2) == '=') {
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
	*  Get armor type from command parameter.
	*/
	Armor.Type getArmorType (String s) {
		if (s.charAt(2) == '=') {
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

		// Header
		System.out.print("\nMonster");
		for (int level = 0; level <= MAX_LEVEL; level++) {
			System.out.print("\t" + level);
		}
		System.out.println("\tEHD");

		// Body
		for (Monster m: db) {
			if (!m.hasUndefinedEHD()) {
				reportOneMonster(m);
			}
		}
		System.out.println();
	}

	/**
	*  Report number of fighters at each level to match one monster.
	*/
	void reportOneMonster (Monster monster) {
		int[] monsterLevels = createMonsterLevelsArray(monster);
		int newEHD = computeEHD(monsterLevels);
		boolean revised = !isEHDClose(newEHD, monster.getEHD());
		if (revised || !displayOnlyRevisions || spotlightMonster == monster) {
			System.out.print(monster.getRace());
			for (int level = 1; level <= MAX_LEVEL; level++) {
				int val = monsterLevels[level - 1];
				System.out.print("\t" + (val > 0 ? val : "1/" + (-val)));
			}
			System.out.println("\t" + newEHD);
		}
	}

	/**
	*  Create an array of fighters at each level that match this monster.
	*/
	int[] createMonsterLevelsArray (Monster monster) {
		int[] array = new int[MAX_LEVEL];
		for (int level = 1; level <= MAX_LEVEL; level++) {
			array[level - 1] = matchMonsterToFighters(monster, level);
		}  
		return array;
	}

	/**
	*  Compute the recommended EHD from monster levels array.
	*/
	int computeEHD (int[] monsterLevels) {
		double sumVal = 0.0;
		for (int level = 1; level <= MAX_LEVEL; level++) {
			double val = monsterLevels[level - 1];
			sumVal += (val > 0 ? level * val : level / (-val));
		}
		return (int) Math.round(sumVal / MAX_LEVEL);
	}

	/**
	*  Determine if EHDs are relatively close.
	*  Simulates percentage margin-of-error on integers.
	*/
	boolean isEHDClose (int a, int b) {
		if (a <= 5)
			return a == b;
		else {
			int dist = Math.abs(a - b);
			return dist <= 1;
		}
	} 

	/**
	*  Match fighters of given level to monster of one type.
	*  @param monster Type of monster.
	*  @param level Level of fighter.
	*  @return If positive, one monster to many fighters; 
	*     if negative, one fighter to many monsters. 
	*/
	int matchMonsterToFighters (Monster monster, int level) {

		// Consider one monster to many fighters
		int numFighters = matchFight(
			n -> ratioMonstersBeatFighters(monster, 1, level, n, true)); 
		if (numFighters > 1) return numFighters;

		// Consider one fighter to many monsters.
		int numMonsters = matchFight(
			n -> ratioMonstersBeatFighters(monster, n, level, 1, false));
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
	*/
	Character newFighter (int level) {
		Character f = new Character(null, "Human", "Fighter", level, null); 
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
			if (Dice.roll(100) <= magicPerLevelPct) {
				bonus++;
			}
		}
		return new Weapon("Sword", new Dice(8), 1, bonus);
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
		metrics.parseArgs(args);
		if (metrics.exitAfterArgs()) {
			metrics.printUsage();
		}
		else {
			metrics.displayUnknownSpecials();
			metrics.reportMonsters();
		}
	}
}
