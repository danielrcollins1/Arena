import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
	Manages one RPG fight (one encounter).

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2016-02-10
*/

public class FightManager {

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Default maximum turns allowed in a fight. */
	private static final int DEFAULT_MAX_TURNS = 20;

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Report play-by-play action for fights. */
	private static boolean reportPlayByPlay;

	/** Maximum turns allowed in thus fight. */
	private int maxTurns = DEFAULT_MAX_TURNS;

	/** Count turns/rounds in current fight. */
	private int turnCount;

	/** First party in this fight. */
	private Party party1;
	
	/** Second party in this fight. */
	private Party party2;

	/** Parties ordered by initiative. */
	private List<Party> initOrder;

	/** Winner of this fight. */
	private Party winner;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
		Constructor for parties.
	*/
	public FightManager(Party party1, Party party2) {
		this.party1 = party1;
		this.party2 = party2;
		setInitiativeOrder();
	}
	
	/**
		Constructor for solo monsters.
	*/
	public FightManager(Monster mon1, Monster mon2) {
		this(new Party(mon1), new Party(mon2));	
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Set play-by-play reporting.
	*/
	public static void setPlayByPlayReporting(boolean report) {
		reportPlayByPlay = report; 
	}

	/**
		Get play-by-play reporting.
	*/
	public static boolean getPlayByPlayReporting() {
		return reportPlayByPlay; 
	}

	/**
		Report play-by-play status.
	*/
	private void reportPlayByPlay() {
		if (reportPlayByPlay) {
			System.out.println(this);
		}
	}

	/**
		Set maximum turns.
	*/
	public void setMaxTurns(int turns) {
		maxTurns = turns;
	}

	/**
		Fight a duel between parties.
		@return the winner of the fight
	*/
	public Party fight() {

		// Prepare for battle
		party1.prepBattle(party2);
		party2.prepBattle(party1);

		// Make special attacks on entry
		turnCount = 0;
		reportPlayByPlay();
		initOrder.get(0).makeSpecialAttacks(initOrder.get(1));
		initOrder.get(1).makeSpecialAttacks(initOrder.get(0));
		handleMemberChanges();

		// Alternate turns
		while (turnCount < maxTurns
			&& party1.isLive() && party2.isLive()) 
		{
			turnCount++;
			reportPlayByPlay();
			initOrder.get(0).takeTurn(initOrder.get(1));
			initOrder.get(1).takeTurn(initOrder.get(0));
			handleMemberChanges();
		}

		// Call the winner
		callWinner();
		return winner;
	}

	/**
		Set the initiative order.
	*/
	private void setInitiativeOrder() {
		initOrder = new ArrayList<Party>();
		initOrder.add(party1);
		initOrder.add(party2);
		if (Dice.coinFlip()) {
			Collections.reverse(initOrder);
		}
	}

	/**
		Handle membership changes for both sides.
		E.g., new creatures conjured or dispelled.
	*/
	private void handleMemberChanges() {
		party1.handleMemberChanges();
		party2.handleMemberChanges();
	}

	/**
		Decide on the winner of a fight.
	*/
	private void callWinner() {

		// Get ratio alive
		double ratioLive1 = party1.getRatioLive();
		double ratioLive2 = party2.getRatioLive();
		
		// Call the winner
		if (ratioLive1 > ratioLive2) {
			winner = party1;
		}
		else if (ratioLive2 > ratioLive1) {
			winner = party2;
		}
		else {
			winner = Dice.coinFlip() ? party1 : party2;		
		}
		
		// Report if desired
		if (reportPlayByPlay) {
			System.out.println("* Winner is " + winner + "\n");
		}
	}

	/**
		Get current turn count.
	*/
	public int getTurnCount() {
		return turnCount;
	}

	/**
		Get the winner.
	*/
	public Party getWinner() {
		return winner;
	}

	/**
		Check if winner was first-mover?
	*/
	public boolean winnerWonInit() {
		return winner == initOrder.get(0);
	}

	/**
		Identify this object as a string.
	*/
	public String toString() {
		return party1 + " vs. " + party2;	
	}
}
