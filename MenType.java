import java.util.ArrayList;

/******************************************************************************
*  One type of NPC men encounter.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-14
*  @version  1.0
******************************************************************************/

public class MenType {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	String category;
	String HDStr;
	String alignStr;
	String composition;
	String notes;
	Armor.Type leaderArmor;
	boolean hasCasters;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
	*  Constructor (from string array)
	*/
	MenType (String[] s) {
		category = s[0];
		HDStr = s[1];
		alignStr = s[2];
		leaderArmor = Armor.Type.valueOf(s[3]);
		hasCasters = s[4].equals("Yes");
		composition = s[5];
		notes = s[6];
	}

	//--------------------------------------------------------------------------
	//  Inner class	
	//--------------------------------------------------------------------------

	/**
	*  Unit component
	*/
	public class Component {
		public int number;
		public String description;
		public Component (int num, String desc) {
			number = num;
			description = desc;
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Basic accessors
	public String getCategory () { return category; }
	public Armor.Type getLeaderArmor () { return leaderArmor; }
	public boolean hasCasters () { return hasCasters; }
	public String getNotes () { return notes; }


	/**
	*  Determine alignment from among permitted options.
	*/
	public Alignment determineAlignment () {
		String[] option = alignStr.split("/");
		int rand = Dice.roll(option.length) - 1;
		return Alignment.valueOf(option[rand]);
	}

	/**
	*  Create separate unit components for this type of men.
	*/
	public Component[] createComponents (int total) {
		String[] unitType = composition.split("; ");
		ArrayList<Component> components = new ArrayList<Component>();
		int remainder = total;
		for (int i = 0; i < unitType.length; i++) {

			// Determine unit number
			int unitNum;
			String[] part = unitType[i].split("% ");
			if (i < unitType.length - 1) {
				int percent = Integer.parseInt(part[0]);
				int baseNum = total * percent / 100;
				int roundNum = (baseNum + 5)/10 * 10;
				unitNum = Math.min(roundNum, remainder);
			}
			else {
				unitNum = remainder;							
			}

			// Create description
			if (unitNum > 0) {
				String desc = part[1].substring(0, part[1].length() - 1)
					+ ", HD " + HDStr + ")";
				components.add(new Component(unitNum, desc));
				remainder -= unitNum;
			}
		}		
		return components.toArray(new Component[components.size()]);
	}

	/**
	*  Identify this object as a string.
	*/
	public String toString () {
		return category;	
	}
}

