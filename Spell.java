/******************************************************************************
*  Magic spell class.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-06
******************************************************************************/

public class Spell {

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** Mode enumeration. */
	public enum Mode {Attack, Defense, Miscellany};

	/** Area shapes. */
	public enum Shape {None, Ball, Disk, Line, Wall};

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Area of effect descriptor. */
	private static class AreaOfEffect {
		public int size;
		public Shape shape;
		public AreaOfEffect (Shape s, int i) {
			shape = s;
			size = i;
		}
	}	

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of the spell. */
	String name;
	
	/** Level of the spell. */
	int level;

	/** Range in inches (5-foot unit). */
	int range;
	
	/** Duration in turns (1-minute unit). */
	int duration;	

	/** Area of effect. */
	AreaOfEffect area;
	
	/** Mode of usage. */
	Mode mode;

	/** Game-sim casting formula. */
	SpellCasting.Casting casting;
	
	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (from string descriptor).
	*/
	Spell (String[] s) {
		name = s[0];
		level = Integer.parseInt(s[1]);
		range = parseRange(s[2]);
		duration = parseDuration(s[3]);
		area = parseArea(s[4]);
		mode = parseMode(s[5]);
		casting = null;
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Parse a range descriptor.
	*/
	private static int parseRange (String s) {
		if (s.equals("1 mile")) return 1000;
		else try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException n) {
			System.err.println("Could not parse spell range: " + s);		
			return 0;
		}
	}

	/**
	*  Parse a duration descriptor.
	*/
	private static int parseDuration (String s) {
		if (s.equals("1 day")) return 1500;
		else if (s.equals("1 week")) return 10000;
		else if (s.equals("1 month")) return 40000;
		else if (s.equals("1 year")) return 500000;
		else if (s.equals("infinite")) return Integer.MAX_VALUE;
		else try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException n) {
			System.err.println("Could not parse spell duration: " + s);		
			return 0;
		}
	}

	/**
	*  Parse a mode descriptor.
	*/
	private static Mode parseMode (String s) {
		if (s.equals("A")) return Mode.Attack;
		else if (s.equals("D")) return Mode.Defense;
		else if (s.equals("M")) return Mode.Miscellany;
		else {
			System.err.println("Could not parse spell mode: " + s);		
			return Mode.Miscellany;
		}
	}

	/**
	*  Parse area of effect descriptor.
	*/
	private static AreaOfEffect parseArea (String s) {
		AreaOfEffect aoe = new AreaOfEffect(Shape.None, 0);
		if (!s.equals("-")) {
			String[] part = s.split("-");
			if (part.length == 2) {
				aoe.shape = parseShape(part[0]);
				aoe.size = parseSize(part[1]);			
			}
			else System.err.println("Could not parse spell area: " + s);
		}
		return aoe;
	}

	/**
	*  Parse area shape from descriptor.
	*/
	private static Shape parseShape (String s) {
		if (s.equals("ball")) return Shape.Ball;
		else if (s.equals("disk")) return Shape.Disk;
		else if (s.equals("line")) return Shape.Line;
		else if (s.equals("wall")) return Shape.Wall;
		else {
			System.err.println("Could not parse spell shape: " + s);
			return Shape.None;
		}
	}

	/**
	*  Parse area size from descriptor.
	*/
	private static int parseSize (String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException n) {
			System.err.println("Could not parse spell size: " + s);
			return 0;
		}
	}

	/**
	*  Get the name.
	*/
	public String getName () {
		return name;
	}

	/**
	*  Get the level.
	*/
	public int getLevel () {
		return level;
	}

	/**
	*  Get the range.
	*/
	public int getRange () {
		return range;
	}

	/**
	*  Get the duration.
	*/
	public int getDuration () {
		return duration;
	}

	/**
	*  Get the shape.
	*/
	public Shape getShape () {
		return area.shape;
	}

	/**
	*  Get the size.
	*/
	public int getSize () {
		return area.size;	
	}

	/**
	*  Get the mode.
	*/
	public Mode getMode () {
		return mode;
	}

	/**
	*  Get max targets in area of spell.
	*  Assumes all targets are man-sized (1 inch space).
	*/
	public int getMaxTargetsInArea () {
		switch (area.shape) {
			case None: 
				return 1;
			case Line: case Wall: 
				return area.size;
			case Ball: case Disk:
				return (int) (Math.PI * area.size * area.size);
			default:
				System.err.println("Error: Unhandled spell shape.");
				return 0;			
		}	
	}

	/**
	*  Is this spell an area-effect type?
	*/
	public boolean isAreaEffect () {
		return area.shape != Shape.None;
	}

	/**
	*  Set the game-sim casting formula, if available.
	*/
	public void setCasting (SpellCasting.Casting c) {
		casting = c;
	}

	/**
	*  Does this spell have a casting formula usable in the sim?
	*/
	public boolean isCastable () {
		return casting != null;	
	}

	/**
	*  Expose casting max targets number.
	*/
	public int getMaxTargetNum () {
		assert(casting != null);
		return casting.getMaxTargetNum();
	}

	/**
	*  Expose casting max target HD.
	*/
	public int getMaxTargetHD () { 
		assert(casting != null);
		return casting.getMaxTargetHD();
	}
		
	/**
	*  Expose casting energy effect.
	*/
	public EnergyType getEnergy () {
		assert(casting != null);
		return casting.getEnergy();
	}
		
	/**
	*  Expose casting condition effect.
	*/
	public SpecialType getCondition () {
		assert(casting != null);
		return casting.getCondition();
	}
		
	/**
	*  Expose casting person-only.
	*/
	public boolean isPersonEffect () {
		assert(casting != null);
		return casting.isPersonEffect();
	}

	/**
	*  Cast this spell in-game at a given party.
	*/
	public void cast (int casterLevel, Party enemies) {
		assert(casting != null);
		casting.cast(casterLevel, enemies);
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		return name; 
	}	
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
		String desc[] = new String[] {"Charm Person", "1", "12", "1 day", "-", "A"};
		Spell spell = new Spell(desc);
		System.out.println(spell);
	}
}
