/******************************************************************************
*  Manages one RPG fight (one encounter). 
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-10
******************************************************************************/

public class FightManager {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Maximum number of turns allowed in a fight. */
	private static final int MAX_TURNS = 20;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Report play-by-play action for fights. */
	private static boolean reportPlayByPlay = false;

	/** Report warnings for unusual fight situations. */
	private static boolean reportWarnings = false;

	/** Count turns/rounds in current fight. */
	private int turnCount;

	/** First party in this fight. */
	private Party party1;
	
	/** Second party in this fight. */
	private Party party2;

	/** Winner of the fight. */
	private Party winner;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor for parties
	*/
	FightManager (Party party1, Party party2) {
		this.party1 = party1;
		this.party2 = party2;
		turnCount = 0;
		winner = null;
	}
	
	/**
	*  Constructor for solo monsters
	*/
	FightManager (Monster mon1, Monster mon2) {
		this(new Party(mon1), new Party(mon2));	
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Set play-by-play reporting.
	*/
	public static void setPlayByPlayReporting (boolean b) {
		reportPlayByPlay = b; 
	}

	/**
	*  Get play-by-play reporting.
	*/
	public static boolean getPlayByPlayReporting () {
		return reportPlayByPlay; 
	}

	/**
	*  Fight a duel between parties.
	*  @return the winner of the fight
	*/
	public Party fight () {

		// Prepare for battle
		party1.prepBattle(party2);
		party2.prepBattle(party1);

		// Roll for initiative
		Party partyInit1, partyInit2;
		if (Dice.coinFlip()) {
			partyInit1 = party1; 
			partyInit2 = party2;
		} 
		else {
			partyInit1 = party2;
			partyInit2 = party1;
		}

		// Make special attacks on entry
		turnCount = 0;
		reportPlayByPlay();
		partyInit1.makeSpecialAttacks(partyInit2);
		partyInit2.makeSpecialAttacks(partyInit1);

		// Alternate turns
		while (party1.isLive() && party2.isLive()) {
			turnCount++;
			if (turnCount > MAX_TURNS) break;
			reportPlayByPlay();
			partyInit1.takeTurn(partyInit2);
			partyInit2.takeTurn(partyInit1);
		}

		// Call winner for viable side
		callWinner();
		return winner;		
	}

	/**
	*  Report play-by-play. 
	*/
	private void reportPlayByPlay () {
		if (reportPlayByPlay) {
			System.out.println(this);
		}
	}

	/**
	*  Decide on the winner of a fight.
	*/
	private void callWinner () {

		// Check viability
		boolean viableParty1 = party1.isViableAgainst(party2);
		boolean viableParty2 = party2.isViableAgainst(party1);
		
		// Call the winner
		if (viableParty1 && !viableParty2) {
			winner = party1;
		}
		else if (viableParty2 && !viableParty1) {
			winner = party2;		
		}
		else if (viableParty1 && viableParty2) {
			if (reportWarnings) {
				System.err.println("Fight over with both sides viable: " + this);
			}
			winner = getRandomParty();
		}
		else {
			if (reportWarnings) {
				System.err.println("Fight over with neither side viable: " + this);
			}
			winner = getRandomParty();
		}
		
		// Report if desired		
		if (reportPlayByPlay) {
			System.out.println("* Winner is: " + winner + "\n");
		}
	}

	/**
	*  Get a random party in this fight.
	*/
	private Party getRandomParty () {
		return Dice.coinFlip() ? party1 : party2;
	}
	
	/**
	*  Get current turn count.
	*/
	public int getTurnCount () {
		return turnCount;
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString () {
		return party1 + " vs. " + party2;	
	}
}
