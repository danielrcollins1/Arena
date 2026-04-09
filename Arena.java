import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
	Arena of battling fighters (as gladiators).

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2014-05-20
*/

public class Arena {

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	/** Treasure award models. */
	public enum TreasureModel { Monster, Dungeon, Assortment };

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

		/** Number of years to run the arena. */
	private static final int DEFAULT_NUM_YEARS = 50;
	
	/** Number of fights for each man in a year. */	
	private static final int DEFAULT_FIGHTS_PER_YEAR = 12;

	/** Default number of fighters in the arena. */
	private static final int DEFAULT_NUM_FIGHTERS = 100;

	/** Default group size for each side in a fight. */
	private static final int DEFAULT_PARTY_SIZE = 1;

	/** Default chance of a magic item per character level. */
	private static final int DEFAULT_PCT_MAGIC_PER_LEVEL = 15;

	/** Default armor type fighters wear. */
	private static final Armor.Type DEFAULT_ARMOR = Armor.Type.Plate;

	/** Baseline XP per defeated monster EHD. */
	private static final int BASE_XP_PER_EHD = 100;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** List of fighters for the arena. */
	private Party fighterList;

	/** Number of years to run simulation. */
	private int numYears;

	/** Fights held per simulated year. */
	private int fightsPerYear;

	/** Total fighter population size. */
	private int fighterPopSize;

	/** Fighter party size. */
	private int fighterPartySize;

	/** Starting level for new recruits. */
	private int startLevel;

	/** Fight man vs. monster? */
	private boolean fightManVsMonster;

	/** Treasure award model. */
	private TreasureModel treasureModel;

	/** XP awards use revised table from Sup-I? */
	private boolean useRevisedXPAwards;

	/** Report summary statistics? */
	private boolean reportFighterStats;

	/** Report full details on each fighter? */
	private boolean reportFighterData;

	/** Report kills achieved by each monster type? */
	private boolean reportMonsterKills;

	/** Report total kills by monster level? */
	private boolean reportTotalMonsterKills;

	/** Report status at each year-end? */
	private boolean reportYearEnd;

	/** Report XP award ratios? */
	private boolean reportXPAwards;

	/** Report each individual XP award? */
	private boolean reportAllXPAwards;

	/** Report every encounter? */
	private boolean reportEveryEncounter;

	/** Create win percent matrix? */
	private boolean makeWinPercentMatrix;

	/** Award magic from treasure drops? */
	private boolean awardMagicTreasureDrops;

	/** Base armor type for fighters. */
	private Armor.Type baseArmorType;

	/** Age of oldest fighter who ever lived. */
	private int supMaxAge;

	/** Flag to escape after parsing arguments. */
	private boolean exitAfterArgs;

	/** Total awarded monster XP. */
	private long totalMonsterXP;

	/** Total awarded treasure XP. */
	private long totalTreasureXP;

	/** Typical alignment for generated men. */
	private Alignment typicalAlignment;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor.
	*/
	public Arena() {
		Dice.initialize();
		numYears = DEFAULT_NUM_YEARS;
		fightsPerYear = DEFAULT_FIGHTS_PER_YEAR;
		fighterPopSize = DEFAULT_NUM_FIGHTERS;
		fighterPartySize = DEFAULT_PARTY_SIZE;
		treasureModel = TreasureModel.Dungeon;
		baseArmorType = DEFAULT_ARMOR;
		Character.setPctMagicPerLevel(DEFAULT_PCT_MAGIC_PER_LEVEL);
		typicalAlignment = Alignment.Neutral;
		fighterList = new Party(); 
		reportFighterStats = true;
	}

	/**
		Constructor (set size, sim modes).
	*/
	public Arena(int numFighters, boolean manVsMon, TreasureModel treasMod) {
		this();
		this.fighterPopSize = numFighters;
		this.fightManVsMonster = manVsMon;
		this.treasureModel = treasMod;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Helper print method.
	*/
	private void println(String s) {
		System.out.println(s);
	}

	/**
		Print program banner.
	*/
	private void printBanner() {
		println("OED Arena Simulator");
		println("-------------------");
	}

	/**
		Print usage.
	*/
	public void printUsage() {
		println("Usage: Arena [options]");
		println("  where options include:");
		println("\t-a apply aging effects");
		println("\t-b base type of armor (=0-3, default 3)");
		println("\t-d award magic treasure drops");
		println("\t-e report every encounter");
		println("\t-f fights per year (default =" 
			+ DEFAULT_FIGHTS_PER_YEAR + ")");
		println("\t-l print all XP awards");
		println("\t-m magic per level chance (default =" 
			+ DEFAULT_PCT_MAGIC_PER_LEVEL + ")");
		println("\t-n number of men fighting (default =" 
			+ DEFAULT_NUM_FIGHTERS + ")");
		println("\t-p play-by-play reporting");  
		println("\t-r reporting types");
		println("\t\ts summary statistics\ty year-end info");
		println("\t\td detailed data\t\tk monster kills");
		println("\t\tt total monster kills\tx xp award ratios");
		println("\t-s start level for fighters (default =0)");  
		println("\t-t treasure model (m, d, or a)");
		println("\t-u create matrix of win percentages");
		println("\t-v man-vs-monster (default man-vs-man)");
		println("\t-w use fighter sweep attacks (by level vs. 1 HD)");
		println("\t-x use revised XP award table (from Sup-I)");
		println("\t-y number of years to simulate (default =" 
			+ DEFAULT_NUM_YEARS + ")");
		println("\t-z fighter party size (default =" 
			+ DEFAULT_PARTY_SIZE + ")");
		println("");
	}

	/**
		Parse arguments.
	*/
	public void parseArgs(String[] args) {
		for (String s: args) {
			if (s.length() > 1 && s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'a': Character.setApplyAgingEffects(true); break;
					case 'b': setBaseArmorFromInt(getParamInt(s)); break;
					case 'd': awardMagicTreasureDrops = true; break;
					case 'e': reportEveryEncounter = true; break;
					case 'f': fightsPerYear = getParamInt(s); break; 
					case 'l': reportAllXPAwards = true; break;
					case 'm': Character.setPctMagicPerLevel(getParamInt(s)); break;
					case 'n': fighterPopSize = getParamInt(s); break;
					case 'p': FightManager.setPlayByPlayReporting(true); break;
					case 'r': setReportingFromParamCode(s); break;
					case 's': startLevel = getParamInt(s); break;
					case 't': setTreasureModelFromParamCode(s); break;
					case 'u': makeWinPercentMatrix = true; break;
					case 'v': fightManVsMonster = true; break;
					case 'w': Character.setSweepAttacks(true); break;
					case 'x': useRevisedXPAwards = true; break;
					case 'y': numYears = getParamInt(s); break; 
					case 'z': setPartySize(getParamInt(s)); break;
					default: exitAfterArgs = true; break;
				}
			}
			else {
				exitAfterArgs = true;			
			}
		}
	}

	/**
		Get integer following equals sign in command parameter.
	*/
	private int getParamInt(String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
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
		Set base armor type from integer code.
	*/
	private void setBaseArmorFromInt(int code) {
		switch (code) {
			case 0: baseArmorType = null; break;
			case 1: baseArmorType = Armor.Type.Leather; break;
			case 2: baseArmorType = Armor.Type.Chain; break;
			case 3: baseArmorType = Armor.Type.Plate; break;
			default: exitAfterArgs = true; break;
		} 
	}
	
	/**
		Set fighter party size.
	*/
	private void setPartySize(int n) {
		if (fightManVsMonster) {
			fighterPartySize = n;		
		}	
	}

	/**
		Set reporting for param char code.
	*/
	private void setReportingFromParamCode(String s) {
		reportFighterStats = false; // shut off default
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
		Set the treasure model for param char code.
	*/
	private void setTreasureModelFromParamCode(String s) {
		if (s.length() >= 3) {
			switch (s.charAt(2)) {
				case 'm': treasureModel = TreasureModel.Monster; break;
				case 'd': treasureModel = TreasureModel.Dungeon; break;
				case 'a': treasureModel = TreasureModel.Assortment; break;
				default: exitAfterArgs = true;
			}
		}
		else {
			exitAfterArgs = true;   
		}
	}

	/**
		Run the arena's top-level algorithm.
	*/
	public void runSim() {
		for (int year = 1; year <= numYears; year++) {
			for (int i = 0; i < fightsPerYear; i++) {
				runOneCycle();  
			}
			yearEnd(year);
		}
	}

	/**
		Run one cycle of fights for the whole list.
	*/
	public void runOneCycle() {
		recruitNewFighters();
		fighterList.shuffleMembers();
		fightDuels();
		fighterList.bringOutYourDead();
		fighterList.clearFallen();
		fighterList.healAll();
	}

	/**
		Fill out the fighter list.
	*/
	private void recruitNewFighters() {
		while (fighterList.size() < fighterPopSize) {
			fighterList.add(newFighter(startLevel));
		}
	}

	/**
		Create a new fighter of the indicated level.
	*/
	public Character newFighter(int level) {
		Character f = new Character("Human", "Fighter", level, null);
		f.setAlignment(Alignment.randomBias(typicalAlignment));
		f.setBasicEquipment();
		if (baseArmorType != null) {
			f.setArmor(Armor.makeType(baseArmorType));
		}
		if (fightManVsMonster) {
			f.addEquipment(Weapon.silverDagger());
		}
		f.boostMagicItemsToLevel();
		return f;
	}

	/**
		Fight duels for all fighters in list.
	*/
	private void fightDuels() {
		if (fightManVsMonster) {
			fightDuelsManVsMonster();
		}
		else {
			fightDuelsManVsMan();
		}
	}

	/**
		Duel each pair of fighters.
	*/
	private void fightDuelsManVsMan() {
		for (int i = 0; i < fighterList.size() - 1; i += 2) {
			Party party1 = new Party(fighterList.get(i));
			Party party2 = new Party(fighterList.get(i + 1));
			FightManager manager = new FightManager(party1, party2);
			if (reportEveryEncounter) {
				System.out.println("Arena event: "  + manager);
			}
			manager.fight();
			grantFightAwards(party1, party2, -1);
		}
	}

	/**
		Duel each fighter against random monsters.
	*/
	private void fightDuelsManVsMonster() {
		for (Monster fighter: fighterList) {
			int dungeonLevel = Math.max(fighter.getLevel(), 1);
			Party fighters = createFighterParty(fighter, fighterPartySize);
			Party monsters = createMonsterParty(dungeonLevel, fighterPartySize);
			FightManager manager = new FightManager(fighters, monsters);
			if (reportEveryEncounter) {
				System.out.println("Dungeon level " 
					+ dungeonLevel + ": " + manager);
			}
			Monster chiefMonster = monsters.get(0); // for kill tally
			manager.fight();
			if (fighter.horsDeCombat()) {
				addToKillTally(chiefMonster);
			}
			grantFightAwards(fighters, monsters, dungeonLevel);
		}
	}

	/**
		Create a party for a given fighter of same level.
	*/
	private Party createFighterParty(Monster fighter, int numFighters) {
		Party fighterParty = new Party(fighter);
		for (int i = 1; i < numFighters; i++) {
			fighterParty.add(newFighter(fighter.getLevel()));
		}
		return fighterParty;
	} 

	/**
		Create a monster party to confront a number of fighters.
	*/
	private Party createMonsterParty(int dungeonLevel, int numFighters) {
		Monster monster;
		int numMonsters = 0;
		do {
			monster = MonsterTables.getInstance()
				.randomMonsterByDungeonLevel(dungeonLevel);
			numMonsters = getMonsterNumber(
				monster, dungeonLevel, numFighters);
		} while (numMonsters < 1);
		return new Party(monster, numMonsters);
	}

	/**
		Get number of monsters for encounter (a la Vol-3, p. 11).

		Standard number is 1d6 (avg 3.5 ~ 4) if monsters match dungeon level.
		See the D&D pre-draft (1d6), and also M&TA (average 4 EHD per level).
		In general this is NOT scaled to party size, but we do scale down here
			for below-average party sizes (so we can simulate 1-1 man vs. monster).
		We also scale for varying EHD monsters on different levels.
	*/
	private int getMonsterNumber(
		Monster monster, int dungeonLevel, int numFighters) 
	{
		int roll = Dice.roll(6);
		final double avgNumMonsters = 4;
		double partyScale = Math.min((double) numFighters / avgNumMonsters, 1.0);
		double dangerScale = (double) dungeonLevel / monster.getEHD();
		int numMonsters = (int) Math.round(roll * partyScale * dangerScale);		
		return numMonsters;
	}

	/**
		Grant post-fight awards.
	*/
	private void grantFightAwards(Party party1, Party party2, int level) {
		if (party1.isLive()) {
			grantVictorAwards(party1, party2, level);
		}
		if (party2.isLive()) {
			grantVictorAwards(party2, party1, level);
		}
	}

	/**
		Grant awards from fight to victorious party.
	*/
	private void grantVictorAwards(Party victor, Party loser, int level) {

		// Get monster award
		int monsterXP = partyFallenXPValue(loser);
		totalMonsterXP += monsterXP;

		// Get treasure award
		Treasure treas = new Treasure();
		if (!loser.isLive()) {
			treas = treasureDrop(loser, level);
			distributeMagicTreasure(victor, treas);
		}
		int treasureXP = treas.getValue();
		totalTreasureXP += treasureXP;

		// Divide into shares per member
		int sharePerMember = (monsterXP + treasureXP) / victor.size();
		for (Monster member: victor) {
			awardXP(member, sharePerMember);
		}
		
		// Report on XP awards
		if (reportAllXPAwards) {
			System.out.println(victor + " each gain " 
				+ sharePerMember + " XP");
		}
	}

	/**
		XP award value for given fallen party.
		Conditional on Vol-1 or Sup-I method.
	*/
	private int partyFallenXPValue(Party party) {
		int total = 0;
		XPAwardTable xpt = XPAwardTable.getInstance();
		for (int i = 0; i < party.sizeFallen(); i++) {
			Monster monster = party.getFallen(i);
			total += useRevisedXPAwards 
				? xpt.getXPAward(monster) 
				: monster.getEHD() * BASE_XP_PER_EHD;
		}
		return total; 
	}

	/**
		Get treasure dropped by a fallen party.
	*/
	private Treasure treasureDrop(Party party, int level) {
		assert party.sizeFallen() > 0;
		
		// Mock dungeon level by character level
		if (level < 0) {
			level = party.getFallen(0).getLevel();
		}
		
		// If 0-level, individual coins as Pirates (Vol-2, p. 23)
		if (level == 0) {
			Treasure treas = new Treasure();
			treas.set(Treasure.Category.Gold, 
				Dice.roll(2, 6) * party.sizeFallen());
			return treas;
		}

		// Consult general treasure model
		switch (treasureModel) {
			case Monster: return treasureByMonster(party);
			case Dungeon: return treasureByDungeon(party, level);
			case Assortment: return treasureByAssortment(party, level);
			default: System.err.println("Unhandled treasure model");
		}
		return null;
	}

	/**
		Get treasure as per Vol-2 monster treasure type.
		(Recommended for wilderness encounters only.)
	*/
	private Treasure treasureByMonster(Party party) {
		Monster boss = party.getFallen(0);
		return boss.rollTreasureType(party.sizeFallen());
	}

	/**
		Get treasure as per Vol-3 dungeon level.
		(Officially valid for underworld only.)
	*/
	private Treasure treasureByDungeon(Party party, int level) {
		return DungeonTreasureTable.rollTreasureForLevel(level);
	}

	/**
		Get treasure as per Monster & Treasure Assortment system.
	*/
	private Treasure treasureByAssortment(Party party, int level) {
		return AssortmentTreasureTable.rollTreasureForLevel(level);
	}

	/**
		Award XP and possibly magic to one creature/character.
	*/
	private void awardXP(Monster monster, int xp) {

		// Add XP
		int oldLevel = monster.getLevel();
		monster.addXP(xp);

		// Check for level-up
		if (monster.getLevel() > oldLevel) {
			assert monster.getLevel() == oldLevel + 1;
			if (!awardMagicTreasureDrops) {
				monster.boostMagicItemsOneLevel();
			}
		}
	}

	/**
  		Distribute magic item treasure to winning party.

		For each magic item, offer it to several party members to take.
	*/
	private void distributeMagicTreasure(Party party, Treasure treas) {
		if (awardMagicTreasureDrops) {
			assert party.isLive();
			int size = party.size();
			int numMagic = treas.get(Treasure.Category.Magic);
			for (int i = 0; i < numMagic; i++) {
				ArrayList<Equipment> list = rollMagicItems();
				for (Equipment item: list) {
					for (int j = 0; j < size; j++) {
						int rcvIdx = Dice.roll(size) - 1;
						boolean taken = party.get(rcvIdx).takeEquipment(item);
						if (taken) { break; }
					}
				}
			}		
		}	
	}

	/**
		Roll for a magic item.
		
		Handles only fighter-based items.
		Returns array in case of armor & shield sets.
		Models limited part of magic items table in Vol-2.
	*/
	private ArrayList<Equipment> rollMagicItems() {
		ArrayList<Equipment> list = new ArrayList<Equipment>();
		int roll = Dice.rollPct();
		if (roll <= 20) {
			list.add(Weapon.randomMagicSword());
		}
		else if (roll <= 35) {
			list.addAll(Armor.randomMagicArmor());
		}
		return list;
	}

	/**
		Add to the kill tally for winning monster type.
	*/
	private void addToKillTally(Monster monster) {
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
		End the year.
	*/
	private void yearEnd(int year) {
		for (Monster fighter: fighterList) {
			((Character) fighter).incrementAge(); 
			fighter.setPerfectHealth();
		}  
		if (reportYearEnd) {
			reportYearEnd(year);
		}
	}

	/**
		Print simulation starting info.
	*/
	public void reportStart() {
		System.out.println("Settings: "
			+ (fightManVsMonster ? "man-vs-monster" : "man-vs-man")
			+ ", numFighters " + fighterPopSize
			+ ", numYears " + numYears
			+ ", fights/year " + fightsPerYear 
			+ ", party size " + fighterPartySize
			+ ", treasure by " + treasureModel
			+ "\n");
	}

	/**
		Print simulation ending info.
	*/
	public void reportEnd() {
		if (reportFighterStats) {
			reportFighterStatistics();
		}
		if (reportFighterData) {
			reportFighterData();
		}
		if (fightManVsMonster) {
			if (reportMonsterKills) {
				reportMonsterKills();
			}
			if (reportTotalMonsterKills) {
				reportTotalMonsterKills();
			}
		}
		if (reportXPAwards) {
			reportXPAwards();
		}
	}

	/**
		Print every individual fighter (for testing small groups).
	*/
	private void reportFighterData() {
		fighterList.sortMembersUp();
		for (Monster fighter: fighterList) {
			System.out.println(fighter);
		}
		System.out.println();
	}

	/**
		Generate and print statistics for the fighter list.
	*/
	public void reportFighterStatistics() {
		StatBin[] statBins = compileStatBins();
		System.out.println(
			"Level Number Age HPs Str Int Wis Dex Con Cha W+ A+ S+");
		System.out.println(
			"----- ------ --- --- --- --- --- --- --- --- -- -- --");
		int maxLevel = fighterList.getMaxLevels();
		for (int level = 0; level <= maxLevel; level++) {
			StatBin bin = statBins[level];
			if (bin.size() > 0) {
				System.out.print(String.format("%3d   %5d  %3.0f %3.0f ",
					level, bin.size(), bin.getMeanAge(), bin.getMeanHp())); 
				for (Ability a: Ability.values()) {
					System.out.print(String.format("%3.0f ", bin.getMeanAbility(a)));
				}
				System.out.print(String.format("%2.0f %2.0f %2.0f ",
					bin.getMeanWeaponBonus(), bin.getMeanArmorBonus(), 
					bin.getMeanShieldBonus()));
				System.out.println();
			}
		}
		System.out.println();
	}

	/**
		Compile fighters into statistical bins by level.
	*/
	private StatBin[] compileStatBins() {
		int maxLevel = fighterList.getMaxLevels();
		StatBin[] statBins = new StatBin[maxLevel + 1];
		for (int i = 0; i <= maxLevel; i++) {
			statBins[i] = new StatBin();
		}   
		for (Monster fighter: fighterList) {
			statBins[fighter.getLevel()]
				.addCharacter((Character) fighter);
		}
		return statBins;
	}

	/**
		Prints number killed by each monster type.
	*/
	private void reportMonsterKills() {
		MonsterTables tables = MonsterTables.getInstance();
		int maxLevel = tables.getNumTables();
		for (int level = 1; level <= maxLevel; level++) {
			if (tables.getTotalKillsAtLevel(level) > 0) {
				reportMonsterKillsAtLevel(level);
			}
		}
	}

	/**
		Prints number killed by each monster at given level.
	*/
	private void reportMonsterKillsAtLevel(int level) {

		// Get list & sort
		MonsterTables tables = MonsterTables.getInstance();
		List<Monster> levelList
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
		Prints total kills at each monster level.
	*/
	private void reportTotalMonsterKills() {
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

	/**
		Report XP award ratios.
	*/
	private void reportXPAwards() {
		long totalXP = totalMonsterXP + totalTreasureXP;
		double treasPct = (double) totalTreasureXP / totalXP * 100;
		System.out.println("Total XP from treasure awards: " 
			+ String.format("%.0f", treasPct) + "%");
		System.out.println();
	}

	/**
		Report year-end summary.
	*/
	private void reportYearEnd(int year) {
		if (reportYearEnd) {
			Character oldest = getOldestFighter();
			supMaxAge = Math.max(supMaxAge, oldest.getAge());
			System.out.println("Year " + year
				+ ": max level " + fighterList.getMaxLevels()
				+ ", oldest age " + oldest.getAge()
				+ ", oldest level " + oldest.getLevel()
				+ ", supMaxAge " + supMaxAge);
			if (year == numYears) {
				System.out.println();
			}
		} 
	}

	/**
		Find oldest fighter the list.
		Break ties by highest level.
	*/
	private Character getOldestFighter() {
		Character oldest = null;
		for (Monster monster: fighterList) {
			Character fighter = (Character) monster;
			if (oldest == null
				|| fighter.getAge() > oldest.getAge()
				|| (fighter.getAge() == oldest.getAge()
					&& fighter.getLevel() > oldest.getLevel()))
			{
				oldest = fighter;
			}
		}
		return oldest;
	}

	/**
		Get the top fighters in list.
	*/
	public List<Monster> getTopFighters(int number) {
		return fighterList.getTopMembers(number);
	}

	/**
		Set the base armor type.
	*/
	public void setBaseArmor(Armor.Type type) {
		baseArmorType = type;
	}

	/**
		Set change of magic per level.
	*/
	public void setPctMagicPerLevel(int percent) {
		Character.setPctMagicPerLevel(percent);
	}

	/**
		Set total number of fight cycles.
	*/
	public void setFightCycles(int num) {
		numYears = num / fightsPerYear;	
	}

	/**
		Set typical alignment.
	*/
	public void setTypicalAlignment(Alignment align) {
		typicalAlignment = align;
	}

	/**
		Get random alignment for one man.
	*/
	private Alignment getRandomAlignment() {
		switch (typicalAlignment) {
			case Lawful: return Alignment.randomLawfulBias();
			case Chaotic: return Alignment.randomChaoticBias();
			default: return Alignment.randomNormal();
		}	
	}

	/**
		Main application method.
	*/
	public static void main(String[] args) {
		Arena arena = new Arena();
		arena.printBanner();
		arena.parseArgs(args);
		if (arena.exitAfterArgs) {
			arena.printUsage();
		}
		else {
			arena.reportStart();
			arena.runSim();
			arena.reportEnd();
		}
	}
}
