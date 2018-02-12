import java.util.regex.Pattern;
import java.util.regex.Matcher;

/******************************************************************************
*  Parameterized special ability for a monster.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-01-20
*  @version  1.01
******************************************************************************/

public class SpecialAbility {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** SpecialType of special ability. */
	private SpecialType type;

	/** Parameter for level of ability. */
	private int param;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**	
	*  Constructor (type, param).
	*/
	SpecialAbility (SpecialType type, int param) {
		this.type = type;
		this.param = param;
	}

	/**	
	*  Constructor (type only).
	*/
	SpecialAbility (SpecialType type) {
		this(type, 0);
	}
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Create new special ability from a string.
	*/
	static public SpecialAbility createFromString (String s) {
		Pattern p = Pattern.compile("(\\w+)( \\(([-]?\\d+))?\\)?");
		Matcher m = p.matcher(s);
		if (m.matches()) {
			SpecialType type = SpecialType.findByName(m.group(1));
			if (type != null) {
				return m.group(3) == null ?
					new SpecialAbility(type) :
					new SpecialAbility(type, Integer.parseInt(m.group(3)));
			}
			else {
				SpecialUnknownList.getInstance().recordName(m.group(1));
				return null;
			}
		}
		else {
			System.err.println("Error: Invalid special ability format: " + s);
			return null;		
		}
	}

	/**
	*  Get the type of this special ability.
	*/
	public SpecialType getType () { 
		return type; 
	}

	/**
	*  Get the parameter of this special ability.
	*/
	public int getParam () { 
		return param; 
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString() {
		String s = type.name();
		if (param != 0) {
			s += " (" + param + ")";		
		}
		return s;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {
		System.out.println(createFromString("Poison"));
		System.out.println(createFromString("Poison (2)"));
		System.out.println(createFromString("Poison (4)"));
		System.out.println(createFromString("EnergyDrain (1)"));
		System.out.println(createFromString("EnergyDrain (2)"));
		System.out.println(createFromString("Regeneration (3)"));
	}
}

