package net.superdan.athena;

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

public class NPCGenerator {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Percent chance for magic per level. */
	static final int PCT_MAGIC_PER_LEVEL = 15;

	/** Name of PDF character sheet source file. */
	static final String CHAR_SHEET_FILE = "OED-CharacterSheet.pdf";

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
	int numNPCs; 

	/** Print PDF character sheets */
	boolean printPDFs;

	/** Line breaks between NPCs */
	int lineBreaks;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

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
	*  Print usage.
	*/
	void printUsage () {
		System.out.println("Usage: net.superdan.athena.NPCGenerator [options]");
		System.out.println("  where options include:");
		System.out.println("\t-a alignment (=[L, N, C])");
		System.out.println("\t-r race (=[M, D, E, H])");
		System.out.println("\t-r class (=[F, T, W])");
		System.out.println("\t-l level (=#)");
		System.out.println("\t-n number (=#)");
		System.out.println("\t-b breaks between NPCs (=#)");
		System.out.println("\t-p PDF output");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	void parseArgs (String[] args) {
		for (String s: args) {
			if (s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'r' -> parseRace(s);
					case 'c' -> parseClass(s);
					case 'a' -> parseAlignment(s);
					case 'l' -> inputProfile.level1 = getParamInt(s);
					case 'n' -> numNPCs = getParamInt(s);
					case 'b' -> lineBreaks = getParamInt(s);
					case 'p' -> printPDFs = true;
					default -> exitAfterArgs = true;
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
				e.printStackTrace(System.err);
			}
		}
		exitAfterArgs = true;
		return -1;
	}

	/**
	*  Parse the race command-line parameter.
	*/
	void parseRace (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (java.lang.Character.toUpperCase(s.charAt(3))) {
				case 'M' -> {
					inputProfile.race = "Human";
					return;
				}
				case 'D' -> {
					inputProfile.race = "Dwarf";
					return;
				}
				case 'E' -> {
					inputProfile.race = "Elf";
					return;
				}
				case 'H' -> {
					inputProfile.race = "Halfling";
					return;
				}
			}
		}
		exitAfterArgs = true;
	}

	/**
	*  Parse the class command-line parameter.
	*/
	void parseClass (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (java.lang.Character.toUpperCase(s.charAt(3))) {
				case 'F' -> {
					inputProfile.class1 = "Fighter";
					return;
				}
				case 'T' -> {
					inputProfile.class1 = "Thief";
					return;
				}
				case 'W' -> {
					inputProfile.class1 = "Wizard";
					return;
				}
			}
		}
		exitAfterArgs = true;
	}

	/**
	*  Parse the alignment command-line parameter.
	*/
	void parseAlignment (String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (java.lang.Character.toUpperCase(s.charAt(3))) {
				case 'L' -> {
					inputProfile.align = "Lawful";
					return;
				}
				case 'N' -> {
					inputProfile.align = "Neutral";
					return;
				}
				case 'C' -> {
					inputProfile.align = "Chaotic";
					return;
				}
			}
		}
		exitAfterArgs = true;
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

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		NPCGenerator gen = new NPCGenerator();
		gen.printBanner();
		gen.parseArgs(args);
		if (gen.exitAfterArgs) {
			gen.printUsage();
		}
		else {		
			gen.makeAllNPCs();
		}
	}
}

