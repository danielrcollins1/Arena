import java.util.Scanner;
import java.io.FileWriter; 
import java.io.IOException; 

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

	/** File writer end-line symbol. */
	static final String ENDL = System.getProperty("line.separator");

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

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	/** Output filename. */
	String outFileName;
	
	/** File writer. */
	FileWriter fileWriter;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	public NPCGenerator () {
		Dice.initialize();
		inputProfile = new GenProfile();
		Character.setFeatUsage(true);
		Character.setBoostInitialAbilities(true);
		Character.setPctMagicPerLevel(PCT_MAGIC_PER_LEVEL);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print program banner.
	*/
	public void printBanner () {
		System.out.println("OED NPC Generator\n");	
	}

	/**
	*  Print usage.
	*/
	public void printUsage () {
		System.out.println("Usage: NPCGenerator [filename]");
		System.out.println("\tfilename: Text file to write NPCs");
		System.out.println();
	}

	/**
	*  Parse arguments.
	*/
	public void parseArgs (String[] args) {
		for (String s: args) {
			if (s.equals("-?"))
				exitAfterArgs = true;
			else
				outFileName = s;
		}
	}

	/**
	*  Roll for a race.
	*/
	public String rollRace () {
		switch (Dice.roll(6)) {
			case 1: return "Dwarf";
			case 2: return "Elf";
			case 3: return "Halfling";
			default: return "Human"; 
		}
	}

	/**
	*  Roll for a class.
	*/
	public String rollClass () {
		switch (Dice.roll(6)) {
			case 1: return "Wizard";
			case 2: case 3: return "Thief";
			default: return "Fighter";
		}
	}

	/**
	*  Roll for elf non-wizard class.
	*/
	public String rollElfClass () {
		switch(Dice.roll(6)) {
			case 1: case 2: return "Thief";
			default: return "Fighter";
		}	
	}

	/**
	*  Roll for an alignment.
	*/
	public String rollAlign() {
		switch (Dice.roll(6)) {
			case 1: return "Lawful";
			default: return "Neutral";
			case 6: return "Chaotic";
		}
	}

	/**
	*  Get a clean string reference from the Scanner.
	*  (Empty input returns as null.)
	*/
	String scanCleanLine (Scanner scan) {
		String s = scan.nextLine();
		return (s.length() == 0) ? null : s;	
	}

	/**
	*  Get a clean integer value from the Scanner.
	*  (Non-integer input returns as -1.)
	*/
	int scanCleanInt (Scanner scan) {
		String s = scan.nextLine();
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	*  Get a clean race name from the Scanner.
	*  (Possibly null to randomize.)
	*/
	String scanCleanRace (Scanner scan) {
		String s = scanCleanLine(scan);
		return (s != null 
				&& (s.equals("Human") || s.equals("Elf") 
					|| s.equals("Dwarf") || s.equals("Halfling"))) ?
			s : null;				
	}

	/**
	*  Get a clean class name from the Scanner.
	*  (Possibly null to randomize.)
	*/
	String scanCleanClass (Scanner scan) {
		String s = scanCleanLine(scan);
		return (s != null 
				&& ClassIndex.getInstance().getTypeFromName(s) != null) ?
			s : null;
	}

	/**
	*  Get the input profile from user.
	*/
	public void getUserInput () {
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the following data, "
			+ "or blank for random determination.");
		System.out.print("Race? ");
		inputProfile.race = scanCleanRace(scan);
		System.out.print("Class? ");
		inputProfile.class1 = scanCleanClass(scan);
		System.out.print("Level? ");
		inputProfile.level1 = scanCleanInt(scan);
		if (inputProfile.race != null 
				&& inputProfile.race.equals("Elf"))  {
			System.out.print("2nd Class? ");
			inputProfile.class2 = scanCleanClass(scan);
			System.out.print("2nd Level? ");
			inputProfile.level2 = scanCleanInt(scan);
		}
		System.out.print("Alignment? ");
		inputProfile.align = scanCleanLine(scan);
		System.out.print("Number? ");
		numNPCs = scanCleanInt(scan);
		if (numNPCs < 1) numNPCs = 1;
		System.out.println();
	}

	/**
	*  Create one fully-formed profile from incomplete source.
	*/
	GenProfile fillProfile (GenProfile ip) {
		GenProfile profile = new GenProfile();
 		profile.race = (ip.race != null) ? ip.race : rollRace();
 		profile.class1 = (ip.class1 != null) ? ip.class1 : rollClass();
 		profile.level1 = (ip.level1 > -1) ? ip.level1 : 1;
		profile.class2 = ip.class2;
		profile.level2 = ip.level2;
		profile.align = (ip.align != null) ? ip.align : rollAlign();
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
	public void makeAllNPCs () {
		for (int i = 0; i < numNPCs; i++) {
			GenProfile p = fillProfile(inputProfile);
			Character c = makeNPCFromProfile(p);
			writeOutput(c.toString() + ENDL + ENDL);
		}
	}

	/**
	*  Open file writer.
	*/
	public void fileWriterOpen() {
		if (outFileName != null) {
			try {
				fileWriter = new FileWriter(outFileName);
			}
			catch (IOException e) {
            System.err.println("File writer could not be opened.") ;
			}
		}
	}

	/**
	*  Close file writer.
	*/
	public void fileWriterClose() {
		if (fileWriter != null) {
			try {
				fileWriter.close();		
			}
			catch (IOException e) {
            System.err.println("File writer could not be closed.") ;
			}
		}
	}

	/**
	*  Write an output string (to file writer or system out).
	*/
	public void writeOutput (String s) {
		if (fileWriter != null) {
			try {
				fileWriter.write(s);
			}
			catch (IOException e) {
            System.err.println("File writer write error.") ;
			}
		}			
		else {
			System.out.print(s);
		}
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
			gen.getUserInput();
			gen.fileWriterOpen();
			gen.makeAllNPCs();
			gen.fileWriterClose();
		}
	}
}

