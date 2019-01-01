/******************************************************************************
*  One magic spell.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-06
*  @version  1.0
******************************************************************************/

public class Spell {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of the spell. */
	String name;
	
	/** Level of the spell. */
	int level;
	
	/** Usage of the spell. */
	Usage usage;
	
	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	/** Usage enumeration. */
	public enum Usage {Offensive, Defensive, Miscellany};

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (from string descriptor).
	*/
	Spell (String[] s) {
		name = s[0];
		level = Integer.parseInt(s[1]);
		switch (s[2].charAt(0)) {
			case 'O': usage = Usage.Offensive; break;
			case 'D': usage = Usage.Defensive; break;
			default: usage = Usage.Miscellany;
		}			
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

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
	*  Get the usage.
	*/
	public Usage getUsage () {
		return usage;
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
		String desc[] = new String[] {"Charm Person", "1", "O"};
		Spell spell = new Spell(desc);
		System.out.println(spell);
	}
}

