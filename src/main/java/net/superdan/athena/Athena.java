package net.superdan.athena;

import java.lang.reflect.Method;
import java.util.Arrays;

/******************************************************************************
 *  Master wrapper around all other applications in the net.superdan.athena.Arena package.
 *  Used as the main class in the runnable standalone jar.
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2020-04-23
 ******************************************************************************/

public class Athena {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/**
	 * Available applications.
	 */
	String[] appNames = {"Arena", "Marshal", "MonsterMetrics", "NPCGenerator"};

	/**
	 * Selected application.
	 */
	String appSelect;

	/**
	 * Modified argument array.
	 */
	String[] appArgs;

	/**
	 * Flag to escape after parsing arguments.
	 */
	boolean exitAfterArgs;

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	 * Print program banner.
	 */
	void printBanner() {
		System.out.println("OED net.superdan.athena.Athena Master");
		System.out.println("-----------------");
	}

	/**
	 * Print usage.
	 */
	void printUsage() {
		for (String s : Arrays.asList("Usage: Athena [command] [options]", "  where commands include:")) {
			System.out.println(s);
		}
		for (String app : appNames) {
			System.out.println("\t" + app);
		}
		System.out.println("For options in individual programs, run with -?\n");
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
			} else {
				try {
					appArgs = Arrays.copyOfRange(args, 1, args.length);
				} catch (Exception e) {
					e.printStackTrace();
					exitAfterArgs = true;
				}
			}
		}
	}

	/**
	 * Run chosen app with modified arguments.
	 */
	void runApp() {
		try {
			Class<?> clazz = Class.forName("net.superdan.athena." + appSelect);
			Method method = clazz.getMethod("main", String[].class);
			method.invoke(null, (Object) appArgs);
		} catch (Exception e) {
			e.printStackTrace(System.err);
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
