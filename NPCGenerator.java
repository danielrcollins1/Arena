/**
	Generates random NPCs to user specification.

	Optionally take name of output file, so we can see user prompts
	in console, while final output goes to text file.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2018-12-04
*/

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
		Record for user NPC specification.
	*/
	private class GenProfile {
	
		/** Racial descriptor. */
		private String race;

		/** Alignment descriptor. */
		private String align;
		
		/** Primary class descriptor. */
		private String class1;
		
		/** Secondary class descriptor. */
		private String class2;

		/** Primary class level. */
		private int level1;
		
		/** Secondary class level. */
		private int level2;
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Profile input by user. */
	private GenProfile inputProfile;

	/** Number of NPCs to create. */
	private int numNPCs; 

	/** Number of line breaks between NPCs. */
	private int lineBreaks;

	/** Flag to print PDF character sheets. */
	private boolean printPDFs;

	/** Flag to escape after parsing arguments. */
	private boolean exitAfterArgs;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
		Constructor.
	*/
	public NPCGenerator() {
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
		Print program banner.
	*/
	private void printBanner() {
		System.out.println("OED NPC Generator");
		System.out.println("-----------------");
	}

	/**
		Print usage.
	*/
	private void printUsage() {
		System.out.println("Usage: NPCGenerator [options]");
		System.out.println("  where options include:");
		System.out.println("\t-a alignment (=[L, N, C])");
		System.out.println("\t-r race (=[M, D, E, H])");
		System.out.println("\t-c class (=[F, T, W])");
		System.out.println("\t-l level (=#)");
		System.out.println("\t-n number (=#)");
		System.out.println("\t-b breaks between NPCs (=#)");
		System.out.println("\t-p PDF output");
		System.out.println();
	}

	/**
		Parse arguments.
	*/
	private void parseArgs(String[] args) {
		for (String s: args) {
			if (s.length() > 1 && s.charAt(0) == '-') {
				switch (s.charAt(1)) {
					case 'r': parseRace(s); break;
					case 'c': parseClass(s); break;
					case 'a': parseAlignment(s); break;
					case 'l': inputProfile.level1 = getParamInt(s); break;
					case 'n': numNPCs = getParamInt(s); break;
					case 'b': lineBreaks = getParamInt(s); break;
					case 'p': printPDFs = true; break;
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
		Parse the race command-line parameter.
	*/
	private void parseRace(String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (java.lang.Character.toUpperCase(s.charAt(3))) {
				case 'M': inputProfile.race = "Human"; return;
				case 'D': inputProfile.race = "Dwarf"; return;
				case 'E': inputProfile.race = "Elf"; return;
				case 'H': inputProfile.race = "Halfling"; return;
				default: System.err.println("Unknown race code.");
			}
		}
		exitAfterArgs = true;
	}

	/**
		Parse the class command-line parameter.
	*/
	private void parseClass(String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (java.lang.Character.toUpperCase(s.charAt(3))) {
				case 'F': inputProfile.class1 = "Fighter"; return;
				case 'T': inputProfile.class1 = "Thief"; return;
				case 'W': inputProfile.class1 = "Wizard"; return;
				default: System.err.println("Unknown class code.");
			}
		}
		exitAfterArgs = true;
	}

	/**
		Parse the alignment command-line parameter.
	*/
	private void parseAlignment(String s) {
		if (s.length() > 3 && s.charAt(2) == '=') {
			switch (java.lang.Character.toUpperCase(s.charAt(3))) {
				case 'L': inputProfile.align = "Lawful"; return;
				case 'N': inputProfile.align = "Neutral"; return;
				case 'C': inputProfile.align = "Chaotic"; return;
				default: System.err.println("Unknown alignment code.");
			}
		}
		exitAfterArgs = true;
	}

	/**
		Roll for a race.
	*/
	private String rollRace() {
		switch (Dice.roll(6)) {
			case 1: return "Dwarf";
			case 2: return "Elf";
			case 3: return "Halfling";
			default: return "Human"; 
		}
	}

	/**
		Roll for a class.
	*/
	private String rollClass() {
		switch (Dice.roll(6)) {
			case 1: return "Wizard";
			case 2: case 3: return "Thief";
			default: return "Fighter";
		}
	}

	/**
		Roll for elf non-wizard class.
	*/
	private String rollElfClass() {
		switch (Dice.roll(6)) {
			case 1: case 2: return "Thief";
			default: return "Fighter";
		}	
	}

	/**
		Create one fully-formed profile from incomplete source.
	*/
	private GenProfile fillProfile(GenProfile p) {
		GenProfile profile = new GenProfile();
 		profile.race = p.race != null ? p.race : rollRace();
 		profile.class1 = p.class1 != null ? p.class1 : rollClass();
 		profile.level1 = p.level1 > 0 ? p.level1 : 1;
		profile.class2 = p.class2;
		profile.level2 = p.level2;
		profile.align = p.align != null ? p.align 
			: Alignment.randomNormal().toString();
		if (profile.race.equals("Elf")) {
			fillElfProfile(profile);
		}
		return profile;	
	}

	/**
		Fill in 2nd class & level for an Elf profile.
	*/
	private void fillElfProfile(GenProfile p) {
		if (p.class2 == null) {
			p.class2 = (p.class1.equals("Wizard")) ? rollElfClass() : "Wizard"; 
		}
		if (p.level2 < 1) {
			p.level2 = Dice.roll(p.level1);
			if (p.class2.equals("Fighter") && p.level2 > 4) {
				p.level2 = 4;
			}
			if (p.class2.equals("Wizard") && p.level2 > 8) {
				p.level2 = 8;
			}
		}
	}

	/**
		Make one NPC from a fully-formed profile.
	*/
	private Character makeNPCFromProfile(GenProfile p) {
		Character c = (p.class2 == null) 
			? new Character(p.race, p.class1, p.level1, p.align) 
			: new Character(p.race, p.class1, p.level1, 
				p.class2, p.level2, p.align);
		c.setBasicEquipment();
		c.boostMagicItemsToLevel();
		c.drawBestWeapon(null);
		return c;	
	}

	/**
		Make multiple NPCs as per input profile.
	*/
	private void makeAllNPCs() {
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
		Print a character to the console.
	*/
	private void printToConsole(Character c) {
		System.out.println(c);
		for (int j = 0; j < lineBreaks; j++) {
			System.out.println();
		}
	}

	/**
		Print a character to a PDF file.
	*/
	private void printToPDF(Character c) {
		System.out.println("Writing " + c.getFilename() + ".pdf");
		CharacterPDF cp = new CharacterPDF();
		cp.writePDF(c);
	}

	/**
		Main test method.
	*/
	public static void main(String[] args) {
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
