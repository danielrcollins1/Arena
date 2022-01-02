/******************************************************************************
*  Make matrix of win ratios for opposing solo fighters of different levels.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2021-07-10
******************************************************************************/

public class WinMatrix {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Number of trials per matrix entry. */
	static final int NUM_TRIALS = 10000;

	/** Maximum level considered. */
	static final int MAX_LEVEL = 12;

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Main test method.
	*/
	public static void main (String[] args) {
		Arena arena = new Arena(1, false, true);
		for (int i = 1; i <= MAX_LEVEL; i++) {
			for (int j = 1; j <= MAX_LEVEL; j++) {
				int numFtr1Wins = 0;
				for (int n = 1; n <= NUM_TRIALS; n++) {
					Party party1 = new Party(arena.newFighter(i));
					Party party2 = new Party(arena.newFighter(j));
					FightManager manager = new FightManager(party1, party2);
					if (manager.fight() == party1)
						numFtr1Wins++;
				}
				int winPct = 100 * numFtr1Wins / NUM_TRIALS;
				System.out.print(winPct + "\t");				
			}
			System.out.println();
		}
		System.out.println();
	}
}

