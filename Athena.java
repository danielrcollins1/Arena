import java.util.Arrays;
import java.lang.reflect.Method;

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

	/** Selected application. */
	String appSelect;

	/** Modified argument array. */
	String appArgs[];

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
			appSelect = args[0];
			if (!Arrays.asList(appNames).contains(appSelect)) {
				exitAfterArgs = true;
			}
			else {
				try {
					appArgs = Arrays.copyOfRange(args, 1, args.length);
				}
				catch (Exception e) {
					System.out.println(e);
					exitAfterArgs = true;
				}
			}
		}
	}

	/**
	*  Run chosen app with modified arguments.
	*/
	void runApp () {
		try {		
			Class<?> clazz = Class.forName(appSelect);
			Method method = clazz.getMethod("main", String[].class); 
			method.invoke(null, (Object) appArgs);
		}			
		catch (Exception e) {
			System.err.println(e);		
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
			athena.runApp();		
		}
	}
}
