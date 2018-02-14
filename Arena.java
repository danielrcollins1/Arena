import java.util.ArrayList;
import java.util.Comparator;
import java.awt.Toolkit;
import java.io.*; 

/******************************************************************************
*  Arena of battling gladiators (fighters).
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-05-20
*  @version  1.12
******************************************************************************/

public class Arena {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	final int DEFAULT_NUM_YEARS = 50;
	final int DEFAULT_FIGHTS_PER_YEAR = 24;
	final int DEFAULT_NUM_FIGHTERS = 100;
	final int DEFAULT_PARTY_SIZE = 1;
	final int DEFAULT_PCT_MAGIC_PER_LEVEL = 15;
	final Armor.Type DEFAULT_ARMOR = Armor.Type.Plate;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** List of fighters for the arena. */
	Party fighterList;

	/** Number of years to run simulation. */
	int numYears;

	/** Fights held per simulated year. */
	int fightsPerYear;

	/** Desired size of the fighter list. */
	int numFighters;

	/** Starting level for new recruits. */
	int startLevel;

	/** Percent chance of magic boost per level. */
	int pctMagicPerLevel;

	/** Fighter party size. */
	int fighterPartySize;

	/** Fight man vs. monster? */
	boolean fightManVsMonster;

	/** Treasure award by monster type? */
	boolean useMonsterTreasureType;

	/** XP awards use revised table from Sup-I? */
	boolean useRevisedXPAwards;

	/** Apply aging effects? */
	boolean applyAgingEffects;

	/** Report summary statistics? */
	boolean reportFighterStats;

	/** Report full details on each fighter? */
	boolean reportFighterData;

	/** Report kills achieved by each monster type? */
	boolean reportMonsterKills;

	/** Report total kills by monster level? */
	boolean reportTotalMonsterKills;

	/** Report status at each year-end? */
	boolean reportYearEnd;

	/** Report XP award ratios? */
	boolean reportXPAwards;

	/** Report every encounter? */
	boolean reportEveryEncounter;

	/** Base armor type for fighters. */
	Armor.Type baseArmorType;

	/** Fix primary weapon for fighters. */
	Weapon baseWeaponType;

	/** Age of oldest fighter who ever lived. */
	int supMaxAge;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	/** Total awarded monster XP. */
	long totalMonsterXP;

	/** Total awarded treasure XP. */
	long totalTreasureXP;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	public Arena () {
		Dice.initialize();
		numYears = DEFAULT_NUM_YEARS;
		fightsPerYear = DEFAULT_FIGHTS_PER_YEAR;
		numFighters = DEFAULT_NUM_FIGHTERS;
		fighterPartySize = DEFAULT_PARTY_SIZE;
		pctMagicPerLevel = DEFAULT_PCT_MAGIC_PER_LEVEL;
		baseArmorType = DEFAULT_ARMOR;
		fighterList = new Party(); 
	}

	/**
	*  Constructor (set mode, size).
	*/
	public Arena (boolean fightManVsMonster, int numFighters) {
		this();
		this.fightManVsMonster = fightManVsMonster;
		this.numFighters = numFighters;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print usage.
	*/
	public void printUsage () {
		System.out.println("Usage: Arena [options]");
		System.out.println("  where options include:");
		System.out.println("\t-a apply aging effects");
		System.out.println("\t-b base type of armor (=0-3, default 3)");
		System.out.println("\t-e report every encounter");
		System.out.println("\t-f fights per year (default =" + DEFAULT_FIGHTS_PER_YEAR + ")");
		System.out.println("\t-m magic per level chance (default = " + DEFAULT_PCT_MAGIC_PER_LEVEL + ")");
		System.out.println("\t-n number of men fighting (default =" + DEFAULT_NUM_FIGHTERS + ")");
		System.out.println("\t-p play-by-play reporting");  
		System.out.println("\t-r reporting types");
		System.out.println("\t\ts summary statistics\ty year-end info");
		System.out.println("\t\td detailed data\t\tk monster kills");
		System.out.println("\t\tt total monster kills\tx xp award ratios");
		System.out.println("\t-s start level for fighters (default =0)");  
		System.out.println("\t-t treasure awards by monster (default by dungeon)");
		System.out.println("\t-v man-vs-monster (default man-vs-man)");
		System.out.println("\t-x use revised XP award table (from Sup-I)");
		System.out.println("\t-y number of years to simulate (default =" + DEFAULT_NUM_YEARS + ")");
		System.out.println("\t-z fighter party size (default =" + DEFAULT_PARTY_SIZE + ")");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	public void parseArgs (String[] args) {
		for (String s: args) {
			if (s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'a': applyAgingEffects = true; break;
					case 'b': setBaseArmorFromInt(getParamInt(s)); break;
					case 'e': reportEveryEncounter = true; break;
					case 'f': fightsPerYear = getParamInt(s); break; 
					case 'm': pctMagicPerLevel = getParamInt(s); break;
					case 'n': numFighters = getParamInt(s); break;
					case 'p': FightManager.setPlayByPlayReporting(true); break;
					case 'r': setReportingFromParamCode(s); break;
					case 's': startLevel = getParamInt(s); break;
					case 't': useMonsterTreasureType = true; break;
					case 'v': fightManVsMonster = true; break;
					case 'x': useRevisedXPAwards = true; break;
					case 'y': numYears = getParamInt(s); break; 
					case 'z': if (fightManVsMonster)
									fighterPartySize = getParamInt(s); break;
					default: exitAfterArgs = true; break;
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
			}
		}
		exitAfterArgs = true;
		return -1;
	}

	/**
	*  Set base armor type from integer code.
	*/
	void setBaseArmorFromInt (int code) {
		switch (code) {
			case 0: baseArmorType = null; break;
			case 1: baseArmorType = Armor.Type.Leather; break;
			case 2: baseArmorType = Armor.Type.Chain; break;
			case 3: baseArmorType = Armor.Type.Plate; break;
			default: exitAfterArgs = true; break;
		} 
	}

	/**
	*  Set reporting for param char code.
	*/
	void setReportingFromParamCode (String s) {
		for (int i = 2; i < s.length(); i++) {
			switch (s.charAt(i)) {
				case 's': reportFighterStats = true; break;
				case 'd': reportFighterData = true; break;
				case 'k': reportMonsterKills = true; break;
				case 't': reportTotalMonsterKills = true; break;
				case 'x': reportXPAwards = true; break;
				case 'y': reportYearEnd = true; break;
				default: exitAfterArgs = true;   
			}
		} 
	}

	/**
	*  Run the arena's top-level algorithm.
	*/
	public void runSim () {
		for (int year = 1; year <= numYears; year++) {
			for (int i = 0; i < fightsPerYear; i++) {
				runOneCycle();  
			}
			yearEnd(year);
		}
	}

	/**
	*  Run one cycle of fights for the whole list.
	*/
	public void runOneCycle () {
		recruitNewFighters();
		fighterList.shuffleMembers();
		fightDuels();
		fighterList.bringOutYourDead();
		fighterList.clearFallen();
		fighterList.healAll();
	}

	/**
	*  Fill out the fighter list.
	*/
	void recruitNewFighters() {
		while (fighterList.size() < numFighters) {
			fighterList.add(newFighter(startLevel));
		}
	}

	/**
	*  Create a new fighter of the indicated level.
	*/
	Character newFighter (int level) {
		String name = NameGenerator.getInstance().getRandom();
		Character f = new Character(name, "Human", "Fighter", level, null); 
		if (baseArmorType != null) {
			f.setArmor(Armor.makeType(baseArmorType));
		}
		Weapon primaryWeapon = (baseWeaponType == null) ?
			Weapon.randomPrimary() : new Weapon(baseWeaponType);
		f.drawWeapon(primaryWeapon);
		f.addEquipment(Weapon.randomSecondary());
		if (primaryWeapon.getHandsUsed() <= 1) {
			f.setShield(Armor.makeType(Armor.Type.Shield));
		}
		if (fightManVsMonster) {
			f.addEquipment(Weapon.silverDagger());
		}
		for (int i = 0; i <= level; i++) {
			f.checkMagicArmsBoost(pctMagicPerLevel);
		}
		return f;
	}

	/**
	*  Fight duels for all fighters in list.
	*/
	void fightDuels () {
		if (fightManVsMonster)
			fightDuelsManVsMonster();
		else
			fightDuelsManVsMan();
	}

	/**
	*  Duel each pair of fighters.
	*/
	void fightDuelsManVsMan () {
		for (int i = 0; i < fighterList.size() - 1; i += 2) {
			Party party1 = new Party(fighterList.get(i));
			Party party2 = new Party(fighterList.get(i+1));
			if (reportEveryEncounter) {
				System.out.println("Arena event: " 
					+ party1 + " vs. " + party2);
			}
			FightManager.fight(party1, party2);
			grantFightAwards(party1, party2, -1);
		}
	}

	/**
	*  Duel each fighter against random monsters.
	*/
	void fightDuelsManVsMonster () {
		for (Monster fighter: fighterList) {
			int dungeonLevel = Math.max(fighter.getLevel(), 1);
			Party fighters = createFighterParty(fighter, fighterPartySize);
			Party monsters = createMonsterParty(dungeonLevel, fighterPartySize);
			Monster chiefMonster = monsters.get(0); // for kill tally
			if (reportEveryEncounter) {
				System.out.println("Dungeon level " + dungeonLevel + ": " 
					+ fighters + " vs. " + monsters);
			}
			FightManager.fight(fighters, monsters);
			grantFightAwards(fighters, monsters, dungeonLevel);
			if (fighter.horsDeCombat()) {
				addToKillTally(chiefMonster);
			}
		}
	}

	/**
	*  Create a party for a given fighter of same level.
	*/
	Party createFighterParty (Monster fighter, int numFighters) {
		Party fighterParty = new Party(fighter);
		for (int i = 1; i < numFighters; i++) {
			fighterParty.add(newFighter(fighter.getLevel()));
		}
		return fighterParty;
	} 

	/**
	*  Create a monster party to confront a number of fighters.
	*/
	Party createMonsterParty (int dungeonLevel, int numFighters) {
		Party monsterParty = new Party();
		Monster monster = MonsterTables.getInstance()
			.randomMonsterByDungeonLevel(dungeonLevel);
		int numMonsters = getMonsterNumber(
			monster, dungeonLevel, numFighters);
		for (int i = 0; i < numMonsters; i++) {
			monsterParty.add(monster.spawn());   
		}
		return monsterParty;
	}

	/**
	*  Get number of monsters for encounter (a la Vol-3, p. 11).
	*/
	int getMonsterNumber (Monster monster, int dungeonLevel, int numFighters) {
		int numMonsters = numFighters * dungeonLevel / monster.getEHD();
		return Math.max(1, numMonsters);
	}

	/**
	*  Grant post-fight awards.
	*/
	void grantFightAwards (Party party1, Party party2, int level) {
		if (party1.isLive())
			grantVictorAwards(party1, party2, level);
		else if (party2.isLive())
			grantVictorAwards(party2, party1, level);
	}

	/**
	*  Grant awards from fight to victorious party.
	*/
	void grantVictorAwards (Party victor, Party loser, int level) {

		// Compute total awards 
		int monsterXP = partyFallenXPValue(loser);
		int treasureXP = treasureValue(loser, level);
		totalMonsterXP += monsterXP;
		totalTreasureXP += treasureXP;

		// Divide into shares per member
		int sharePerMember = (monsterXP + treasureXP) / victor.size();
		for (Monster member: victor) {
			awardXP(member, sharePerMember);
		}
	}

	/**
	*  XP award value for given fallen party.
	*  Conditional on Vol-1 or Sup-I method.
	*/
	int partyFallenXPValue (Party party) {
		int total = 0;
		XPTable xpt = XPTable.getInstance();
		for (int i = 0; i < party.sizeFallen(); i++) {
			Monster monster = party.getFallen(i);
			total += useRevisedXPAwards ? 
				xpt.getXPAward(monster) : monster.getEHD() * 100;
		}
		return total; 
	}

	/**
	*  Value of treasure award (nominally in gold pieces).
	*  Conditional on using monster treasure type.
	*/
	int treasureValue (Party party, int level) {
		if (useMonsterTreasureType)
			return treasureValueByMonster(party);
		else
			return treasureValueByDungeon(party, level);
	}

	/**
	*  Get treasure value as per monster treasure type.
	*  (Officially valid for wilderness only.)
	*/
	int treasureValueByMonster (Party party) {
		Monster chief = party.getFallen(0);
		MonsterTreasureTable table = MonsterTreasureTable.getInstance();

		// For characters, type 'A' scaled by level & size
		if (chief instanceof Character) {
			int baseGP = table.randomValueByCode('A');
			int level = Math.max(chief.getLevel(), 1);
			return baseGP * level * party.sizeFallen() / 165;
		}

		// For monsters, treasure type scaled by size
		else {
			int baseGP = table.randomValueByCode(chief.getTreasureType());
			int avgNum = chief.getNumberAppearing().avgRoll();
			return baseGP * party.sizeFallen() / avgNum;
		} 
	}

	/**
	*  Get treasure value as per level beneath surface.
	*  (Officially valid for underworld only.)
	*/
	int treasureValueByDungeon (Party party, int level) {
		if (level < 1) { // dummy by leader level
			level = Math.max(party.getFallen(0).getLevel(), 1);
		}
		return DungeonTreasureTable.getInstance()
			.randomValueByLevel(level);
	} 

	/**
	*  Award XP and magic to one creature/character.
	*/
	void awardXP (Monster monster, int xp) {

		// Add XP
		int startLevel = monster.getLevel();
		monster.addXP(xp);

		// Check for level-up
		if (monster.getLevel() > startLevel) {
			monster.checkMagicArmsBoost(pctMagicPerLevel);
		}
	}

	/**
	*  Add to the kill tally for winning monster type.
	*/
	void addToKillTally (Monster monster) {
		Monster prototype = MonsterDatabase.getInstance()
			.getByRace(monster.getRace());
		if (prototype != null) {
			prototype.addToKillTally(1);
		}  
		else {
			System.err.println("Failed to find prototype: " + monster);
		}
	}

	/**
	*  End the year.
	*/
	void yearEnd (int year) {
		for (Monster fighter: fighterList) {
			((Character)fighter).incrementAge(applyAgingEffects); 
			fighter.setPerfectHealth();
		}  
		reportYearEnd(year);
	}

	/**
	*  Print simulation starting info.
	*/
	public void reportStart () {
		System.out.println("ARENA: "
			+ (fightManVsMonster ? "man-vs-monster" : "man-vs-man")
			+ ", numFighters " + numFighters
			+ ", numYears " + numYears
			+ ", fights/year " + fightsPerYear 
			+ ", party size " + fighterPartySize
			+ ", treasure by " + (useMonsterTreasureType ? "monster" : "dungeon")
			+ "\n");
	}

	/**
	*  Print simulation ending info.
	*/
	public void reportEnd () {
		reportFighterStatistics();
		reportFighterData();
		reportMonsterKills();
		reportTotalMonsterKills();
		reportXPAwards();
	}

	/**
	*  Print every individual fighter (for testing small groups).
	*/
	void reportFighterData () {
		if (reportFighterData) {
			fighterList.sortMembers();
			for (Monster fighter: fighterList) {
				System.out.println(fighter);
			}
			System.out.println();
		}
	}

	/**
	*  Generate and print statistics for the fighter list.
	*/
	void reportFighterStatistics () {
		if (reportFighterStats) {
			StatBin statBins[] = compileStatBins();
			System.out.println("Level Number Age HPs Str Int Wis Dex Con Cha");
			System.out.println("----- ------ --- --- --- --- --- --- --- ---");
			int maxLevel = fighterList.getMaxLevels();
			for (int level = 0; level <= maxLevel; level++) {
				StatBin bin = statBins[level];
				if (bin.size() > 0) {
					System.out.print(String.format("%3d   %5d  %3d %3d ",
						level, bin.size(), bin.getMeanAge(), bin.getMeanHp())); 
					for (Ability a: Ability.values())
						System.out.print(String.format("%3d ", bin.getMeanAbility(a)));
					System.out.println();
				}
			}
			System.out.println();
		}
	}

	/**
	*  Compile fighters into statistical bins by level.
	*/
	StatBin[] compileStatBins () {
		int maxLevel = fighterList.getMaxLevels();
		StatBin statBins[] = new StatBin[maxLevel+1];
		for (int i = 0; i <= maxLevel; i++) {
			statBins[i] = new StatBin();
		}   
		for (Monster fighter: fighterList) {
			statBins[fighter.getLevel()]
				.addCharacter((Character)fighter);
		}
		return statBins;
	}

	/**
	*  Prints number killed by each monster type.
	*/
	void reportMonsterKills () {
		if (fightManVsMonster && reportMonsterKills) {
			MonsterTables tables = MonsterTables.getInstance();
			int maxLevel = tables.getNumTables();
			for (int level = 1; level <= maxLevel; level++) {
				if (tables.getTotalKillsAtLevel(level) > 0) {
					reportMonsterKillsAtLevel(level);
				}
			}
		}
	}

	/**
	*  Prints number killed by each monster at given level.
	*/
	void reportMonsterKillsAtLevel (int level) {

		// Get list & sort
		MonsterTables tables = MonsterTables.getInstance();
		ArrayList<Monster> levelList
			= new ArrayList<Monster>(tables.getTable(level));
		levelList.sort(Comparator.comparing(Monster::getKillTally));
		int totalKills = tables.getTotalKillsAtLevel(level);

		// Print the list   
		System.out.println("Level " + level + " Monsters");
		for (Monster m: levelList) {
			float killPct = (float) m.getKillTally() / totalKills * 100;
			System.out.println(m.getRace() + ": " + m.getKillTally()
				+ " (" + String.format("%.0f", killPct) + "%)"); 
		}
		System.out.println();
	}

	/**
	*  Prints total kills at each monster level.
	*/
	void reportTotalMonsterKills () {
		if (fightManVsMonster && reportTotalMonsterKills) {
			MonsterTables tables = MonsterTables.getInstance();
			int maxLevel = tables.getNumTables();
			int grandTotal = tables.getGrandTotalKills();
			System.out.println("Total Monster Kills");
			System.out.println("-------------------");
			for (int i = 1; i <= maxLevel; i++) {
				int kills = tables.getTotalKillsAtLevel(i);
				float killPct = (float) kills / grandTotal * 100;
				System.out.println("Level " + i + ": " + kills
					+ " (" + String.format("%.0f", killPct) + "%)");
			}
			System.out.println();
		}
	}

	/**
	*  Report XP award ratios.
	*/
	void reportXPAwards () {
		long totalXP = totalMonsterXP + totalTreasureXP;
		double treasPct = (double) totalTreasureXP / totalXP * 100;
		System.out.println("Total XP from treasure awards: " 
			+ String.format("%.0f", treasPct) + "%");
		System.out.println();
	}

	/**
	*  Report year-end summary. 
	*/
	void reportYearEnd (int year) {
		if (reportYearEnd) {
			Character oldest = getOldestFighter();
			supMaxAge = Math.max(supMaxAge, oldest.getAge());
			System.out.println("Year " + year
				+ ": max level " + fighterList.getMaxLevels()
				+ ", oldest age " + oldest.getAge()
				+ ", oldest level " + oldest.getLevel()
				+ ", supMaxAge " + supMaxAge);
		} 
	}

	/**
	*  Find oldest fighter the list.
	*/
	Character getOldestFighter () {
		Character oldest = null;
		for (Monster fighter: fighterList) {
			if (oldest == null || ((Character)fighter).getAge() > oldest.getAge())
				oldest = (Character) fighter;
		}
		return oldest;
	}

	/**
	*  Counts fighters at a given level or above.
	*/
	public int countMenAboveLevel (int level) {
		return fighterList.countAboveLevel(level);
	}

	/**
	*  Prints fighters at a given level or above.
	*/
	public void printMenAboveLevel (int level) {
		fighterList.printAboveLevel(level);
	}

	/**
	*  Set the base armor type.
	*/
	public void setBaseArmor (Armor.Type type) {
		baseArmorType = type;
	}

	/**
	*  Set the base weapon type.
	*/
	public void setBaseWeapon (Weapon weapon) {
		baseWeaponType = weapon; 
	}

	/**
	*  Set use of revised XP awards (per Sup-I).
	*/
	public void setUseRevisedXPAwards (boolean use) {
		useRevisedXPAwards = use; 
	}

	/**
	*  Main application method.
	*/
	public static void main (String[] args) {
		Arena arena = new Arena();
		arena.parseArgs(args);
		if (arena.exitAfterArgs()) {
			arena.printUsage();
		}
		else {
			arena.reportStart();
			arena.runSim();
			arena.reportEnd();
		}
		//Toolkit.getDefaultToolkit().beep();
	}
}
