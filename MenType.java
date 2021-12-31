import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/******************************************************************************
*  One type of NPC men encounter.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2016-02-14
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
	public Alignment getAlignment () {
		String[] option = alignStr.split("/");
		int rand = Dice.roll(option.length) - 1;
		return Alignment.valueOf(option[rand]);
	}

	/**
	*  Create separate unit components for this type of men.
	*/
	public Component[] createComponents (int total) {
		int remainder = total;
		List<Component> components = new ArrayList<Component>();
		String[] unitType = composition.split("; ");
		for (int i = 0; i < unitType.length; i++) {
			String unitName;
			int percent, armorClass, moveRate;

			// Parse the component descriptor
			Pattern p = Pattern.compile("(\\d+)% (.+) \\x28AC (\\d+), MV (\\d+)\\x29");
			Matcher m = p.matcher(unitType[i]);
			if (m.matches()) {
				unitName = m.group(2);
				percent = Integer.parseInt(m.group(1));
				armorClass = Integer.parseInt(m.group(3));
				moveRate = Integer.parseInt(m.group(4));
			}
			else {
				System.err.println("Error: Could not parse men type descriptor: " + unitType[i]);
				continue;
			}

			// Set component number, rounding to tens
			int unitNum = remainder;
			if (i < unitType.length - 1) {
				int baseNum = total * percent / 100;
				int roundNum = (baseNum + 5)/10 * 10;
				unitNum = Math.min(roundNum, remainder);
			}
			remainder -= unitNum;

			// Create description
			if (unitNum > 0) {
				String desc = unitName
					+ ": AC " + armorClass
					+ ", MV " + moveRate
					+ ", HD " + HDStr;
				components.add(new Component(unitNum, desc));
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

