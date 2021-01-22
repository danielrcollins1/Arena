package net.superdan.athena;

/******************************************************************************
 *  Fighter feats enumeration.
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2018-12-08
 *  @version 1.00
 ******************************************************************************/

public enum Feat {
    /**
     * Optional OED fighter feats.
     * Caution: Not all are implemented in code at this time.
     */
    Berserking, GreatCleave, GreatFortitude, GreatStrength,
    IronWill, MountedCombat, RapidShot, RapidStrike, Survival,
    Toughness, TwoWeaponFighting, WeaponSpecialization;

    /**
     * Total number of feats available.
     */
    public static final int number = Feat.values().length;

    //--------------------------------------------------------------------------
    //  Methods
    //--------------------------------------------------------------------------

    /**
     * Format a feat name with spaces.
     */
    public static String formatName(Feat feat) {
        StringBuilder f = new StringBuilder();
        var s = feat.toString();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 &&
                    java.lang.Character.isUpperCase(s.charAt(i))) {
                f.append(" ");
            }
            f.append(s.charAt(i));
        }
        return f.toString();
    }
}
