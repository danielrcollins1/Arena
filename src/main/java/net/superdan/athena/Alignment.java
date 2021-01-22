package net.superdan.athena;

/******************************************************************************
 *  net.superdan.athena.Alignment enumeration.
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2014-05-19
 *  @version 1.0
 ******************************************************************************/

public enum Alignment {

    //--------------------------------------------------------------------------
    //  Fields
    //--------------------------------------------------------------------------

    Lawful, Neutral, Chaotic;

    //--------------------------------------------------------------------------
    //  Methods
    //--------------------------------------------------------------------------

    /**
     * Convert a letter to alignment.
     */
    public static Alignment getFromChar (char c) {
        return switch (c) {
            case 'L' -> Lawful;
            case 'N' -> Neutral;
            case 'C' -> Chaotic;
            default -> null;
        };
    }

    /**
     *  Convert a string to an alignment.
     */
    public static Alignment getFromString (String s) {
        return (s == null || s.length() == 0) ?
                null : getFromChar(s.charAt(0));
    }

    /**
     *  Randomize a normally-distributed alignment.
     */
    public static Alignment randomNormal() {
        return switch (Dice.roll(6)) {
            case 1 -> Lawful;
            default -> Neutral;
            case 6 -> Chaotic;
        };
    }

    /**
     *  Randomize a uniformly-distributed alignment.
     */
    public static Alignment randomUniform() {
        return switch (Dice.roll(6)) {
            case 1, 2 -> Lawful;
            default -> Neutral;
            case 5, 6 -> Chaotic;
        };
    }

    /**
     *  Randomize a Lawful-biased alignment.
     */
    public static Alignment randomLawfulBias() {
        return switch (Dice.roll(6)) {
            case 1, 2 -> Neutral;
            default -> Lawful;
        };
    }

    /**
     *  Randomize a Chaotic-biased alignment.
     */
    public static Alignment randomChaoticBias() {
        return switch (Dice.roll(6)) {
            case 1, 2 -> Neutral;
            default -> Chaotic;
        };
    }
}

