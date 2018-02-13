/******************************************************************************
*  Manages one RPG fight (one encounter). 
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-10
*  @version  1.0
******************************************************************************/

public class FightManager {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Report play-by-play action for fights. */
	static boolean reportPlayByPlay;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Basic constructor
	*/
	FightManager () {
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Set play-by-play reporting.
	*/
	static public void setPlayByPlayReporting (boolean b) {
		reportPlayByPlay = b; 
	}

	/**
	*  Fight a duel between parties.
	*/
	static public void fight (Party party1, Party party2) {

		// Prepare for battle
		party1.prepBattle(party2);
		party2.prepBattle(party1);

		// Roll for initiative
		if (Math.random() < 0.5) {
			Party swap = party1;
			party1 = party2;
			party2 = swap;    
		}

		// Make special attacks on entry
		reportPlayByPlay(party1, party2);
		party1.makeSpecialAttacks(party2);
		party2.makeSpecialAttacks(party1);

		// Alternate turns
		while (party1.isLive() && party2.isLive()) {
			reportPlayByPlay(party1, party2);
			party1.takeTurn(party2);
			party2.takeTurn(party1);
		}

		// Victory to side still standing
		Party winner = party1.isLive() ? party1 : party2;
		reportVictory(winner);
	}

	/**
	*  Fight a duel between individual monsters.
	*/
	static public void fight (Monster monster1, Monster monster2) {
		fight(new Party(monster1), new Party(monster2));
	}

	/**
	*  Report play-by-play. 
	*/
	static void reportPlayByPlay (Party party1, Party party2) {
		if (reportPlayByPlay) {
			System.out.println(party1 + " vs. " + party2);
		}
	}

	/**
	*  Report victory. 
	*/
	static void reportVictory (Party winner) {
		if (reportPlayByPlay) {
			System.out.println("* Victory to " + winner + "\n");
		}
	}
}
