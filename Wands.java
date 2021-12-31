import java.io.IOException; 

/******************************************************************************
*  List of wands and staves.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-04
******************************************************************************/

public class Wands {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Name of file with wands. */
	final String WANDS_FILE = "Wands.csv";

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	class WandInfo {
		String name;
		int tier;

		WandInfo (String[] s) {
			name = s[0];
			tier = Integer.parseInt(s[1]);
		}
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static Wands instance = null;

	/** Table of wand information. */
	WandInfo[] wandList;

	/** Count of entries at each tier. */
	int[] tierCount;

	/** Maximum tier of wands. */
	int maxTier;


	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor (read from dedicated file).
	*/
	protected Wands () throws IOException {
		String[][] table = CSVReader.readFile(WANDS_FILE);
		wandList = new WandInfo[table.length - 1];
		for (int i = 1; i < table.length; i++) {
			wandList[i - 1] = new WandInfo(table[i]);
		}		
		setMaxTier();
		setTierCounts();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static Wands getInstance() {
		if (instance == null) {
			try {
				instance = new Wands();
			}
			catch (IOException e) {
				System.err.println("Failed to read the Wands file.");
			}
		}
		return instance;
	}

	/**
	*  Set the maximum tier.
	*/
	private void setMaxTier () {
		int max = -1;
		for (WandInfo w: wandList) {
			if (w.tier > max)
				max = w.tier;
		}
		maxTier = max;
	}

	/**
	*  Count entries at each tier.
	*/
	private void setTierCounts () {
		tierCount = new int[maxTier];
		for (WandInfo w: wandList)
			tierCount[w.tier - 1]++;
	}

	/**
	*  Get random wand at specified tier.
	*/
	public Equipment getRandom (int tier) {
		if (1 <= tier && tier <= maxTier) {
			int count = tierCount[tier - 1];
			if (count > 0) {
				int roll = Dice.roll(count);
				for (WandInfo w: wandList) {
					if (w.tier == tier) {
						roll--;
						if (roll == 0) {
							float weight = w.name.contains("Staff") ? 
								Equipment.ONE_THIRD : 0;
							return new Equipment(w.name, 
								Equipment.Material.Wood, weight, 0);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	*  Get tier of wand from equipment.
	*/
	public int getTier (Equipment wand) {
		for (WandInfo w: wandList) {
			if (w.name.equals(wand.getName()))
				return w.tier;
		}	
		return -1;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		Wands wands = Wands.getInstance();
		System.out.println("Random Wands:");
		for (int i = 1; i <= 4; i++) {
			System.out.println("Tier " + i + ": " + wands.getRandom(i));
		}
		System.out.println();
	}
}

