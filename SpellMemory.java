import java.util.*;

/******************************************************************************
*  Memory of spells known by one creature.
*
*  We don't allow duplicates, so this uses the Set interface.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2021-12-19
******************************************************************************/

public class SpellMemory {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The set of spells in memory (no duplicates). */
	Set<Spell> memory;

	/** Should we select in-sim castable spells first? */
	static boolean preferCastableSpells = false;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	public SpellMemory () {
		memory = new LinkedHashSet<Spell>();
	}

	/**
	*  Copy constructor.
	*/
	public SpellMemory (SpellMemory src) {
		memory = new LinkedHashSet<Spell>(src.memory);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Set static preference for in-sim castable spells.
	*/
	public static void setPreferCastableSpells (boolean prefer) {
		preferCastableSpells = prefer;	
	}

	/**
	*  Add a spell.
	*/
	public boolean add (Spell s) {
		return memory.add(s);
	}

	/**
	*  Remove a spell.
	*/
	public boolean remove (Spell s) {
		return memory.remove(s);	
	}

	/**
	*  Is this spell in our memory?
	*/
	public boolean contains (Spell s) {
		return memory.contains(s);
	}
	
	/**
	*  Is our memory empty?
	*/
	public boolean isBlank () {
		return memory.isEmpty();	
	}

	/**
	*  Count spells of a given level.
	*/
	public int countAtLevel (int level) {
		int count = 0;
		for (Spell s: memory) {
			if (s.getLevel() == level)
				count++;
		}
		return count;		
	}	

	/**
	*  Add a random spell from index at a given level.
	*  Use rollMode() for weightings.
	*  @return true if we added a spell.
	*/
	public boolean addRandom (int level) {
		SpellsIndex index = SpellsIndex.getInstance();
		int startCount = countAtLevel(level);
		if (startCount < index.getNumAtLevel(level)) {
			Spell.Mode mode;
			Spell spell;
			do {
				mode = rollMode();
				if (preferCastableSpells
					&& startCount < index.getNumAtLevelCastable(level))
				{
					spell = index.getRandomCastable(level);
				}				
				else {
					spell = index.getRandom(level);
				}
			} while (contains(spell) || spell.getMode() != mode);
			add(spell);
			return true;
		}
		return false;
	}

	/**
	*  Roll random selected spell mode.
	*  As per analysis of Gygax modules: see blog 2018-12-17.
	*/
	private Spell.Mode rollMode () {
		switch (Dice.roll(6)) {
			case 1: return Spell.Mode.Miscellany;
			case 2: case 3: return Spell.Mode.Defense;
			default: return Spell.Mode.Attack;
		}	
	}

	/**
	*  Get the best (highest-level) castable attack spell.
	*  @param area true if area-effect spell desired.
	*  @return the best spell in memory.
	*/
	public Spell getBestAttackSpell (boolean areaEffect) {
		Spell best = null;	
		for (Spell s: memory) {
			if (s.isCastable()
				&& s.getMode() == Spell.Mode.Attack
				&& s.isAreaEffect() == areaEffect)
			{
				if (best == null 
					|| best.getLevel() < s.getLevel()) 
				{
					best = s;
				}							
			}
		}	
		return best;
	}

	/**
	*  Add a spell by naming it.
	*/
	public void addByName (String name) {
		add(SpellsIndex.getInstance().findByName(name));
	}

	/**
	*  Add all spells for a wizard of a given level.
	*/
	public void addSpellsForWizard (int level) {
		SpellsDaily spellsDaily = SpellsDaily.getInstance();
		int maxPower = spellsDaily.getMaxSpellLevel();
		for (int power = 1; power <= maxPower; power++) {
			int numSpells = spellsDaily.getSpellsDaily(level, power);
			for (int num = 0; num < numSpells; num++) {
				addRandom(power);
			}
		}
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString () {
		String s = "";
		for (Spell spell: memory) {
			if (s.length() > 0)
				s += ", ";
			s += spell.getName();
		}
		return s;
	}

	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		Dice.initialize();
		SpellMemory mem = new SpellMemory();

		// Add a random spell per level
		System.out.println("Random spells in memory:");
		for (int level = 1; level <= 6; level++) {
			mem.addRandom(level);
		}
		System.out.println(mem);
	}
}
