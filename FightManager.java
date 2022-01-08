import java.util.*;

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

	/** Count turns/rounds in current fight. */
	private int turnCount;

	/** First party in this fight. */
	private Party party1;
	
	/** Second party in this fight. */
	private Party party2;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor for parties
	*/
	FightManager (Party party1, Party party2) {
		this.party1 = party1;
		this.party2 = party2;
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
	public static void setPlayByPlayReporting (boolean report) {
		reportPlayByPlay = report; 
	}

	/**
	*  Get play-by-play reporting.
	*/
	public static boolean getPlayByPlayReporting () {
		return reportPlayByPlay; 
	}

	/**
	*  Report play-by-play status. 
	*/
	private void reportPlayByPlay () {
		if (reportPlayByPlay) {
			System.out.println(this);
		}
	}

	/**
	*  Fight a duel between parties.
	*  @return the winner of the fight
	*/
	public Party fight () {

		// Prepare for battle
		party1.prepBattle(party2);
		party2.prepBattle(party1);
		List<Party> order = getInitiativeOrder();

		// Make special attacks on entry
		turnCount = 0;
		reportPlayByPlay();
		order.get(0).makeSpecialAttacks(order.get(1));
		order.get(1).makeSpecialAttacks(order.get(0));

		// Alternate turns
		while ((turnCount < MAX_TURNS)
			&& party1.isLive() && party2.isLive()) 
		{
			turnCount++;
			reportPlayByPlay();
			order.get(0).takeTurn(order.get(1));
			order.get(1).takeTurn(order.get(0));
		}

		// Call the winner
		Party winner = callWinner();
		if (reportPlayByPlay) {
			System.out.println("* Winner is " + winner + "\n");
		}
		return winner;		
	}

	/**
	*  Get parties in initiative order.
	*/
	private List<Party> getInitiativeOrder () {
		List<Party> order = new ArrayList<Party>(2);
		if (Dice.coinFlip()) {
			order.add(party1);
			order.add(party2);
		}
		else {
			order.add(party2);
			order.add(party1);
		}
		return order;
	}

	/**
	*  Decide on the winner of a fight.
	*/
	private Party callWinner () {

		// Get ratio alive
		double ratioLive1 = party1.getRatioLive();
		double ratioLive2 = party2.getRatioLive();
		
		// Call the winner
		if (ratioLive1 > ratioLive2) {
			return party1;
		}
		else if (ratioLive2 > ratioLive1) {
			return party2;
		}
		else {
			return Dice.coinFlip() ? party1 : party2;		
		}
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
