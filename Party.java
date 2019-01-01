import java.util.*;

/******************************************************************************
*  One party (force, team, band, or group) of Monsters.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-31
*  @version  1.01
******************************************************************************/

public class Party implements Iterable<Monster> {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** List of party members. */
	List<Monster> members;

	/** List of fallen members. */
	List<Monster> fallen;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Empty constructor
	*/
	Party () {
		members = new ArrayList<Monster>();
		fallen = new ArrayList<Monster>();
	}

	/**
	*  Solo party
	*/
	Party (Monster monster) {
		members = new ArrayList<Monster>();
		fallen = new ArrayList<Monster>();
		members.add(monster);
	}

	/**
	*  List constructor
	*/
	Party (List<Monster> list) {
		members = list;
		fallen = new ArrayList<Monster>();
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Basic accessors
	public int size () { return members.size(); }
	public Monster get (int idx) { return members.get(idx); }
	public int sizeFallen () { return fallen.size(); } 
	public Monster getFallen (int idx) { return fallen.get(idx); }

	// Basic mutators
	public void add (Monster m) { members.add(m); }

	/**
	*  Is this party operational? 
	*/
	public boolean isLive () { 
		return !members.isEmpty(); 
	}

	/**
	*  Clear records of attacks taken.
	*/
	public void clearTimesMeleed () {
		for (Monster m: this) {
			m.clearTimesMeleed();
		} 
	}

	/**
	*  Pick a random party member.
	*/
	public Monster random () {
		if (isLive()) {
			return get(Dice.roll(size()) - 1);
		}
		return null;
	}

	/**
	*  Pick a target for melee.
	*/
	public Monster randomMelee () {
		if (isOpenToMelee()) {
			while (true) {
				Monster m = random();
				if (m.isOpenToMelee()) return m;
			}
		}
		return null; 
	}

	/**
	*  Check if any members are yet un-melee'd.
	*/
	public boolean isOpenToMelee () {
		for (Monster m: this) {
			if (m.isOpenToMelee()) return true;
		} 
		return false;
	}

	/**
	*  Get a random subset of this party.
	*/
	public List<Monster> randomGroup (int number) {

		// Make a copy of members & shuffle it
		List<Monster> shuffledMembers 
			= new ArrayList<Monster>(members);
		Collections.shuffle(shuffledMembers);

		// Deal out top elements from shuffle
		List<Monster> group = new ArrayList<Monster>();
		int num = Math.min(number, size());
		for (int i = 0; i < num; i++) {
			group.add(shuffledMembers.get(i));
		}
		return group;
	}

	/**
	*  Prepare for battle against an enemy.
	*/
	public void prepBattle (Party enemy) {
		if (enemy.isLive()) {
			summonAllMinions();
			for (Monster m: members) {
				m.drawBestWeapon(enemy.random());
				m.initBreathCharges();
			} 
		}
	}

	/**
	*  Make all summons possible (add to party).
	*  Caution: Must not be recursive.
	*/
	private void summonAllMinions () {
		int num = size();
		for (int i = 0 ; i < num; i++) {
			get(i).summonMinions(this);
		}
	}

	/**
	*  Add a number of some monster race to this party.
	*/
	public void addMonsters (String race, int number) {
		Monster m = MonsterDatabase.getInstance().getByRace(race);
		if (m != null) {
			for (int i = 0; i <number; i++) {
				add(m.spawn());  
			}
		}
	}

	/**
	*  Make special attacks against an enemy party.
	*/
	public void makeSpecialAttacks (Party enemy) {
		if (enemy.isLive()) {
			for (Monster m: this) {
				m.makeSpecialAttack(enemy);
			}
			enemy.bringOutYourDead();
		}
	}

	/**
	*  Have each member take its turn against an enemy.
	*/
	public void takeTurn (Party enemy) {
		if (enemy.isLive()) {
			enemy.clearTimesMeleed();
			for (Monster m: this) {

				// Special abilities
				m.checkRegeneration();
				m.checkConstriction();
				m.checkSlowing(enemy);
				if (m.checkGrabbing()) continue;
				if (m.checkDrainBlood()) continue;
				if (m.checkBreathWeapon(enemy)) continue;
				if (m.checkConfused(this)) continue;

				// Melee attacks
				Attack attack = m.getAttack();
				for (int i = 0; i < attack.getRate(); i++) {
					if (enemy.isOpenToMelee()) {
						Monster target = enemy.randomMelee();
						boolean last = (i == attack.getRate() - 1);
						m.singleAttack(m.getAttack(), target, last);
						if (target.horsDeCombat() && 
							m.hasFeat(Feat.GreatCleave)) i--;
						target.incTimesMeleed();
					}
				}
			}
			enemy.bringOutYourDead();
		}
	}

	/**
	*  Move dead members to list of fallen.
	*/
	public void bringOutYourDead () {
		for (int i = size() - 1; i > -1; i--) {
			Monster member = get(i); 
			if (member.horsDeCombat()) {
				fallen.add(member); 
				members.remove(i);
			}  
		}
	}

	/**
	*  Clear out the list of fallen.
	*/
	public void clearFallen () {
		fallen.clear();
	}

	/**
	*  Sort the list of members by increasing level/hit dice.
	*/
	public void sortMembers () {
		members.sort((a, b) -> a.getHD() - b.getHD());
	}

	/**
	*  Sort the list of members by decreasing level/hit dice.
	*/
	public void sortMembersDown () {
		members.sort((a, b) -> b.getHD() - a.getHD());
	}

	/**
	*  Shuffle the list of members.
	*/
	public void shuffleMembers () {
		Collections.shuffle(members);
	}

	/**
	*  Heal the party fully.
	*/
	public void healAll () {
		for (Monster m: this) {
			m.setPerfectHealth();
		}
	}

	/**
	*  Get max level in the party.
	*/
	public int getMaxLevels () {
		int max = 0; 
		for (Monster m: this) {
			max = Math.max(max, m.getLevel());
		}
		return max;
	}

	/**
	*  Get total levels in the party.
	*/
	public int getSumLevels () {
		int sum = 0; 
		for (Monster m: this) {
			sum += m.getLevel(); 
		}
		return sum;
	}

	/**
	*  Print top number of members of party. 
	*/
	public void printTopMembers (int number) {
		sortMembersDown();
		for (int i = 0; i < number && i < members.size(); i++) {
			System.out.println(members.get(i));				
		}
		System.out.println();
	}	

	/**
	* Identify this object as a string.
	*/
	public String toString() {
		if (size() == 0) {
			return "Dead party";
		}
		else {
			// Assuming party is all one race
			String s = members.get(0).getRace();
			if (size() > 1) {
				s += "s (" + size() + ")";
			}
			if (size() < 6) {
				s += ": hp " + getHitPointList();
			}
			return s;
		}
	}

	/**
	* Make a list of the party's hit points.
	*/
	public List<Integer> getHitPointList () {
		List<Integer> list = new ArrayList<Integer>(size());
		for (Monster m: this) {
			list.add(m.getHP());    
		} 
		return list;
	}

	/**
	* Return iterator for the iterable interface.
	*/
	public Iterator<Monster> iterator() {        
		return members.iterator();
	}

	/**
	* Main test method.
	*/
	public static void main (String[] args) {
		Dice.initialize();
		Monster m = new Monster("Orc", 6, 9, 
			new Dice(1, 6), new Attack(1, 1));

		// Create various size parties
		Party p0 = new Party();
		Party p1 = new Party(m.spawn());
		Party p3 = new Party();
		for (int i = 0; i < 3; i++) {
			p3.add(m.spawn());  
		}
		Party p10 = new Party();
		for (int i = 0; i < 10; i++) {
			p10.add(m.spawn());  
		}

		// Print them
		System.out.println(p0);
		System.out.println(p1);
		System.out.println(p3);
		System.out.println(p10);
	}
}
