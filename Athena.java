/******************************************************************************
*  Master wrapper around all other applications in the Arena package.
*  Used as the main class in the runnable standalone jar.

*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2020-04-23
******************************************************************************/

public class Athena {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Available applications. */
	String appNames[] = {"Arena", "Marshal", "MonsterMetrics", "NPCGenerator"};

	/** Selected app index. */
	int appIndex;

	/** Flag to escape after parsing arguments. */
	boolean exitAfterArgs;

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Print program banner.
	*/
	void printBanner () {
		System.out.println("OED Athena Master");
		System.out.println("-----------------");
	}

	/**
	*  Print usage.
	*/
	void printUsage () {
		System.out.println("Usage: Athena [command] [options]");
		System.out.println("  where commands include:");
		for (String app: appNames) {
			System.out.println("\t" + app);
		}
		System.out.println("For options in individual programs, run with -?");
		System.out.println("");
	}

	/**
	*  Parse arguments.
	*/
	public void parseArgs (String[] args) {
		if (args.length == 0) {
			exitAfterArgs = true;
		}
		else {
			appIndex = getAppIdx(args[0]);
			if (appIndex < 0) {
				exitAfterArgs = true;
			}
		}
	}

	/**
	*  Determine index of chosen app.
	*/
	int getAppIdx (String command) {
		for (int i = 0; i < appNames.length; i++) {
			if (appNames[i].equals(command)) {
				return i;
			}
		}	
		return -1;
	}

	/**
	*  Run chosen app as per arguments.
	*/
	void runApp (String[] args) {
		String[] newargs = new String[args.length - 1];
		for (int i = 0; i < newargs.length; i++) {
			newargs[i] = args[i + 1];
		}
		switch(appIndex) {
			case 0: Arena.main(newargs); break;
			case 1: Marshal.main(newargs); break;
			case 2: MonsterMetrics.main(newargs); break;
			case 3: NPCGenerator.main(newargs); break;
			default: System.err.println("Error: Invalid app index.");
		}			
	}	

	/**
	*  Main application method.
	*/
	public static void main (String[] args) {
		Athena athena = new Athena();
		athena.parseArgs(args);
		if (athena.exitAfterArgs) {
			athena.printBanner();
			athena.printUsage();
		}
		else {
			athena.runApp(args);		
		}
	}
}
