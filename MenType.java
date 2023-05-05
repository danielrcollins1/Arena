import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
	One type of NPC men encounter (per Vol-2).
	
	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2016-02-14
*/

public class MenType {

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Name of this type. */
	private String name;
	
	/** Hit dice descriptor. */
	private String hitDiceStr;
	
	/** Alignment descriptor. */
	private String alignStr;
	
	/** Unit composition descriptor. */
	private String composition;
	
	/** Miscellaneous notes. */
	private String notes;
	
	/** Armor type for lead fighters. */
	private Armor.Type leaderArmor;
	
	/** Does this type have spellcasters? */	
	private boolean hasCasters;

	//--------------------------------------------------------------------------
	//  Constructor
	//--------------------------------------------------------------------------

	/**
		Constructor (from string array).
	*/
	public MenType(String[] s) {
		name = s[0];
		hitDiceStr = s[1];
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
		One of the units that make up the composition of forces.
	*/
	public class Component {

		/** Number of men. */
		private int number;
		
		/** Unit description. */
		private String description;
		
		/** Get the number of men. */
		public int getNumber() { return number; }
		
		/** Get the unit description. */
		public String getDescription() { return description; }

		/** Constructor. */
		public Component(int num, String desc) {
			number = num;
			description = desc;
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	// Basic accessors
	public String getName() { return name; }
	public Armor.Type getLeaderArmor() { return leaderArmor; }
	public boolean hasCasters() { return hasCasters; }
	public String getNotes() { return notes; }

	/**
		Determine alignment from among permitted options.
	*/
	public Alignment getAlignment() {
		String[] option = alignStr.split("/");
		int rand = Dice.roll(option.length) - 1;
		return Alignment.valueOf(option[rand]);
	}

	/**
		Create separate unit components for this type of men.
	*/
	public Component[] createComponents(int total) {
		int remainder = total;
		List<Component> components = new ArrayList<Component>();
		String[] unitType = composition.split("; ");
		for (int i = 0; i < unitType.length; i++) {
			String unitName;
			int percent, armorClass, moveRate;

			// Parse the component descriptor
			Pattern p = Pattern.compile(
				"(\\d+)% (.+) \\x28AC (\\d+), MV (\\d+)\\x29");
			Matcher m = p.matcher(unitType[i]);
			if (m.matches()) {
				unitName = m.group(2);
				percent = Integer.parseInt(m.group(1));
				armorClass = Integer.parseInt(m.group(3));
				moveRate = Integer.parseInt(m.group(4));
			}
			else {
				System.err.println("Could not parse men type: " + unitType[i]);
				continue;
			}

			// Set component number, rounding to tens
			int unitNum = remainder;
			if (i < unitType.length - 1) {
				int baseNum = total * percent / 100;
				int roundNum = (baseNum + 5) / 10 * 10;
				unitNum = Math.min(roundNum, remainder);
			}
			remainder -= unitNum;

			// Create description
			if (unitNum > 0) {
				String desc = unitName
					+ ": AC " + armorClass
					+ ", MV " + moveRate
					+ ", HD " + hitDiceStr;
				components.add(new Component(unitNum, desc));
			}
		}		
		return components.toArray(new Component[components.size()]);
	}

	/**
		Identify this object as a string.
	*/
	public String toString() {
		return name;	
	}
}
