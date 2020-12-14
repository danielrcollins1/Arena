package net.superdan.athena;

import picocli.CommandLine;

import java.util.concurrent.Callable;

/******************************************************************************
*  Generates random NPCs to user specification.
*
*  Optionally take name of output file, so we can see user prompts 
*  in console, while final output goes to text file. 
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-04
*  @version  1.0
******************************************************************************/

@SuppressWarnings("RedundantThrows")
@CommandLine.Command(name = "NPCGenerator", mixinStandardHelpOptions = true, version = "NPCGenerator 1.0",
		description = "Makes a group of NPCs according to input specs and writes to a file or STDOUT.")
public class NPCGenerator implements Callable<Integer> {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Percent chance for magic per level. */
	static final int PCT_MAGIC_PER_LEVEL = 15;

	//--------------------------------------------------------------------------
	//  Inner Class
	//--------------------------------------------------------------------------

	/**
	*  Record for user NPC specification.
	*/
	static class GenProfile {
		String race, class1, class2, align;
		int level1, level2;
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Profile input by user. */
	GenProfile inputProfile;

	/** Number of NPCs to create. */
	@CommandLine.Option(names = {"-n", "--number"}, description = "The number of NPCs to create")
	int numNPCs; 

	/** Print PDF character sheets */
	@CommandLine.Option(names = {"-p", "--pdf"}, description = "Print to PDF")
	boolean printPDFs;

	/** Line breaks between NPCs */
	@CommandLine.Option(names = {"-b", "--breaks"}, description = "Line breaks between NPCs")
	int lineBreaks;

	@CommandLine.Option(names = {"-r", "--race"}, description = "Race Valid Values: ${COMPLETION-CANDIDATES}")
	Race race;

	@CommandLine.Option(names = {"-c", "--class"}, description = "Class Valid Values: ${COMPLETION-CANDIDATES}")
	CharacterClass aClass;

	@CommandLine.Option(names = {"-a", "--alignment"}, description = "Alignment Valid Values: ${COMPLETION-CANDIDATES}")
	Alignment alignment;

	@CommandLine.Option(names = {"-l", "--level"}, description = "Level of NPC to generate")
	int level;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	NPCGenerator () {
		Dice.initialize();
		inputProfile = new GenProfile();
		Character.setFeatUsage(true);
		Character.setBoostInitialAbilities(true);
		Character.setPctMagicPerLevel(PCT_MAGIC_PER_LEVEL);
		numNPCs = 1;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print program banner.
	*/
	void printBanner () {
		System.out.println("OED NPC Generator");
		System.out.println("-----------------");
	}

	/**
	*  Roll for a race.
	*/
	String rollRace () {
		return switch (Dice.roll(6)) {
			case 1 -> "Dwarf";
			case 2 -> "Elf";
			case 3 -> "Halfling";
			default -> "Human";
		};
	}

	/**
	*  Roll for a class.
	*/
	String rollClass () {
		return switch (Dice.roll(6)) {
			case 1 -> "Wizard";
			case 2, 3 -> "Thief";
			default -> "Fighter";
		};
	}

	/**
	*  Roll for elf non-wizard class.
	*/
	String rollElfClass () {
		return switch (Dice.roll(6)) {
			case 1, 2 -> "Thief";
			default -> "Fighter";
		};
	}

	/**
	*  Roll for an alignment.
	*/
	String rollAlign() {
		return switch (Dice.roll(6)) {
			case 1 -> "Lawful";
			default -> "Neutral";
			case 6 -> "Chaotic";
		};
	}

	/**
	*  Create one fully-formed profile from incomplete source.
	*/
	GenProfile fillProfile (GenProfile ip) {
		GenProfile profile = new GenProfile();
 		profile.race = ip.race == null ? rollRace() : ip.race;
 		profile.class1 = ip.class1 == null ? rollClass() : ip.class1;
 		profile.level1 = ip.level1 == 0 ? 1 : ip.level1;
		profile.class2 = ip.class2;
		profile.level2 = ip.level2;
		profile.align = ip.align == null ? rollAlign() : ip.align;
		if (profile.race.equals("Elf")) {
			fillElfProfile(profile);
		}
		return profile;	
	}

	/**
	*  Fill in 2nd class & level for an Elf profile.
	*/
	void fillElfProfile (GenProfile p) {
		if (p.class2 == null) {
			p.class2 = (p.class1.equals("Wizard")) ? rollElfClass() : "Wizard"; 
		}
		if (p.level2 < 1) {
			p.level2 = Dice.roll(p.level1);
			if (p.class2.equals("Fighter") && p.level2 > 4)
				p.level2 = 4;
			if (p.class2.equals("Wizard") && p.level2 > 8)
				p.level2 = 8;
		}
	}

	/**
	*  Make one NPC from a fully-formed profile.
	*/
	Character makeNPCFromProfile (GenProfile p) {
		Character c = (p.class2 == null) ?
			new Character(p.race, p.class1, p.level1, p.align) :
			new Character(p.race, p.class1, p.level1, p.class2, p.level2, p.align);
		c.setBasicEquipment();
		c.boostMagicItemsToLevel();
		c.drawBestWeapon(null);
		return c;	
	}

	/**
	*  Make multiple NPCs as per input profile.
	*/
	void makeAllNPCs () {
		for (int i = 0; i < numNPCs; i++) {
			GenProfile p = fillProfile(inputProfile);
			Character c = makeNPCFromProfile(p);
			if (printPDFs) {
				printToPDF(c);
			}
			else {
				printToConsole(c);			
			}
		}
	}

	/**
	*  Print a character to the console.
	*/
	void printToConsole (Character c) {
		System.out.println(c);
		for (int j = 0; j < lineBreaks; j++) {
			System.out.println();
		}
	}

	/**
	*  Print a character to a PDF file.
	*/
	void printToPDF (Character c) {
		System.out.println("Writing " + c.getFilename() + ".pdf");
		CharacterPDF cp = new CharacterPDF();
		cp.writePDF(c);
	}

	enum Race {
		Human,
		Dwarf,
		Elf,
		Halfling
	}

	enum CharacterClass {
		Fighter,
		Wizard,
		Thief
	}

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	@Override
	public Integer call() throws Exception {
		inputProfile.align = alignment != null ? String.valueOf(alignment) : null;
		inputProfile.class1 = aClass != null ? String.valueOf(aClass) : null;
		inputProfile.race = race != null ? String.valueOf(race) : null;
		inputProfile.level1 = level;
		printBanner();
		makeAllNPCs();
		return 0;
	}

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		int exitCode = new CommandLine(new NPCGenerator()).execute(args);
		System.exit(exitCode);
	}
}

