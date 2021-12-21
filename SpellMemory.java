import java.util.LinkedHashSet;

/******************************************************************************
*  Memory of spells known by one creature.
*
*  We don't allow duplicates, so this uses the Set interface.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2021-12-19
******************************************************************************/

public class SpellMemory extends LinkedHashSet<Spell> {

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	public SpellMemory () {
	}

	/**
	*  Copy constructor.
	*/
	public SpellMemory (SpellMemory src) {
		super(src);
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Count spells of a given level.
	*/
	public int countAtLevel (int level) {
		int count = 0;
		for (Spell s: this) {
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
		if (countAtLevel(level) < index.getNumAtLevel(level)) {
			Spell.Mode mode;
			Spell spell;
			do {
				mode = rollMode();
				spell = index.getRandom(level);
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
		for (Spell s: this) {
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
	*  Identify this object as a string.
	*/
	public String toString () {
		String s = "";
		for (Spell spell: this) {
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
	}
}
