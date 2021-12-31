/******************************************************************************
*  Fighter feats enumeration.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2018-12-08
******************************************************************************/

public enum Feat {
	/** 
	*  Optional OED fighter feats. 
	*  Caution: Not all are implemented in code at this time.
	*/
	Berserking, ExceptionalStrength, GreatFortitude, IronWill,
	MountedCombat, RapidShot, RapidStrike, Survival, Toughness, 
	Tracking, TwoWeaponFighting, WeaponSpecialization;

	/** Total number of feats available. */
	public static final int number = Feat.values().length;
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	
	/**
	*  Format a feat name with spaces.
	*/
	public static String formatName (Feat feat) {
		String f = "";
		String s = feat.toString();
		for (int i = 0; i < s.length(); i++) {
			if (i > 0 && 
					java.lang.Character.isUpperCase(s.charAt(i))) {
				f += " ";
			}
			f += s.charAt(i);
		}			
		return f;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {
		System.out.println("Feats");
		System.out.println("-----");
		for (int i = 0; i < Feat.number; i++) {
			System.out.println(Feat.values()[i]);		
		}
		System.out.println();
	}
}
