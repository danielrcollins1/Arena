import java.util.Arrays;
import java.lang.reflect.Method;

/**
	Master wrapper around all other applications in the Arena package.
	Used as the main class in the runnable standalone jar.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2020-04-23
*/

public class Athena {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Available applications. */
	private static final String[] APP_NAMES = {
		"Arena", "Marshal", "MonsterMetrics", "NPCGenerator"
	};

	/** Selected application. */
	private String appSelect;

	/** Modified argument array. */
	private String[] appArgs;

	/** Flag to escape after parsing arguments. */
	private boolean exitAfterArgs;

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Print program banner.
	*/
	private void printBanner() {
		System.out.println("OED Athena Master");
		System.out.println("-----------------");
	}

	/**
		Print usage.
	*/
	private void printUsage() {
		System.out.println("Usage: Athena [command] [options]");
		System.out.println("  where commands include:");
		for (String app: APP_NAMES) {
			System.out.println("\t" + app);
		}
		System.out.println("For options in individual programs, run with -?");
		System.out.println("");
	}

	/**
		Parse arguments.
	*/
	private void parseArgs(String[] args) {
		if (args.length == 0) {
			exitAfterArgs = true;
		}
		else {
			appSelect = args[0];
			if (!Arrays.asList(APP_NAMES).contains(appSelect)) {
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
		Run chosen app with modified arguments.
	*/
	private void runApp() {
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
		Main application method.
	*/
	public static void main(String[] args) {
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
