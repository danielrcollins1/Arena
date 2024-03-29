/**
	Magic spell class.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2018-12-06
*/

public class Spell {

	//--------------------------------------------------------------------------
	//  Enumerations
	//--------------------------------------------------------------------------

	/** Mode enumeration. */
	public enum Mode { Attack, Defense, Miscellany };

	/** Area shapes. */
	public enum Shape { None, Ball, Square, Line, Wall };

	//--------------------------------------------------------------------------
	//  Inner class
	//--------------------------------------------------------------------------

	/** Area of effect descriptor. */
	private static class AreaOfEffect {

		/** Shape of the effect. */
		private Shape shape;

		/** Size of the effect. */
		private int size;
		
		/** Constructor. */
		AreaOfEffect(Shape s, int i) {
			shape = s;
			size = i;
		}
	}	

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of the spell. */
	private String name;
	
	/** Level of the spell. */
	private int level;

	/** Range in inches (5-foot unit). */
	private int range;
	
	/** Duration in turns (1-minute unit). */
	private int duration;	

	/** Area of effect. */
	private AreaOfEffect area;
	
	/** Mode of usage. */
	private Mode mode;

	/** Game-sim casting formula. */
	private SpellCasting.Casting casting;
	
	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
		Constructor (from string descriptor).
	*/
	public Spell(String[] s) {
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
		Parse a range descriptor.
	*/
	private static int parseRange(String s) {
		if (s.equals("1 mile")) {
			return 1000;
		}
		else {
			try {
				return Integer.parseInt(s);
			}
			catch (NumberFormatException n) {
				System.err.println("Could not parse spell range: " + s);		
				return 0;
			}
		}
	}

	/**
		Parse a duration descriptor.
		@return duration in turns (minutes)
	*/
	private static int parseDuration(String s) {
		if (s.equals("1 day")) { return 1500; }
		else if (s.equals("1 week")) { return 10000; }
		else if (s.equals("1 month")) { return 40000; }
		else if (s.equals("1 year")) { return 500000; }
		else if (s.equals("infinite")) { return Integer.MAX_VALUE; }
		else  {
			try {
				return Integer.parseInt(s);
			}
			catch (NumberFormatException n) {
				System.err.println("Could not parse spell duration: " + s);		
				return 0;
			}
		}
	}

	/**
		Parse a mode descriptor.
	*/
	private static Mode parseMode(String s) {
		assert s.length() > 0;
		switch (s.charAt(0)) {
			case 'A': return Mode.Attack;
			case 'D': return Mode.Defense;
			case 'M': return Mode.Miscellany;
			default: System.err.println("Could not parse spell mode: " + s);
		}
		return null;
	}

	/**
		Parse area of effect descriptor.
	*/
	private static AreaOfEffect parseArea(String s) {
		AreaOfEffect aoe = new AreaOfEffect(Shape.None, 0);
		if (!s.equals("-")) {
			String[] part = s.split("-");
			if (part.length == 2) {
				aoe.shape = parseShape(part[0]);
				aoe.size = parseSize(part[1]);			
			}
			else {
				System.err.println("Could not parse spell area: " + s);
			}
		}
		return aoe;
	}

	/**
		Parse area shape from descriptor.
	*/
	private static Shape parseShape(String s) {
		if (s.equals("ball")) { return Shape.Ball; }
		else if (s.equals("square")) { return Shape.Square; }
		else if (s.equals("line")) { return Shape.Line; }
		else if (s.equals("wall")) { return Shape.Wall; }
		else { System.err.println("Could not parse spell shape: " + s); }
		return Shape.None;
	}

	/**
		Parse area size from descriptor.
	*/
	private static int parseSize(String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException n) {
			System.err.println("Could not parse spell size: " + s);
			return 0;
		}
	}

	/**
		Get the name.
	*/
	public String getName() {
		return name;
	}

	/**
		Get the level.
	*/
	public int getLevel() {
		return level;
	}

	/**
		Get the range.
	*/
	public int getRange() {
		return range;
	}

	/**
		Get the duration.
	*/
	public int getDuration() {
		return duration;
	}

	/**
		Get the shape.
	*/
	public Shape getShape() {
		return area.shape;
	}

	/**
		Get the size.
	*/
	public int getSize() {
		return area.size;	
	}

	/**
		Get the mode.
	*/
	public Mode getMode() {
		return mode;
	}

	/**
		Get max targets in area of spell.
		Assumes all targets are man-sized (1 inch space).
	*/
	public int getMaxTargetsInArea() {
		switch (area.shape) {
			case None: 
				return 1;
			case Line: case Wall: 
				return area.size;
			case Ball:
				return (int) (Math.PI * area.size * area.size);
			case Square:
				return area.size * area.size;
			default:
				System.err.println("Error: Unhandled spell shape.");
				return 0;			
		}	
	}

	/**
		Is this spell an area-effect type?
	*/
	public boolean isAreaEffect() {
		return area.shape != Shape.None;
	}

	/**
		Set the game-sim casting formula, if available.
	*/
	public void setCasting(SpellCasting.Casting c) {
		casting = c;
	}

	/**
		Does this spell have a casting formula usable in the sim?
	*/
	public boolean isCastable() {
		return casting != null;	
	}

	/**
		Expose casting max targets number.
	*/
	public int getMaxTargetNum() {
		assert casting != null;
		return casting.getMaxTargetNum();
	}

	/**
		Expose is-threat to monster check.
	*/
	public boolean isThreatTo(Monster monster) {
		assert casting != null;
		return casting.isThreatTo(monster);	
	}

	/**
		Cast this spell to help against enemy party.
	*/
	public void cast(Monster caster, Party friends, Party enemies) {
		assert casting != null;
		casting.cast(caster, friends, enemies);
	}

	/**
		Identify this object as a string.
	*/
	public String toString() {
		return name; 
	}	
	
	/**
		Main test function.
	*/
	public static void main(String[] args) {	
		String[] desc = new String[] 
			{"Charm Person", "1", "12", "1 day", "-", "A"};
		Spell spell = new Spell(desc);
		System.out.println(spell);
	}
}
