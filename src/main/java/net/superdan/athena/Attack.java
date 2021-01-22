package net.superdan.athena;

/******************************************************************************
 *  net.superdan.athena.Attack routine specification (sword, claw, rock, etc.).
 *
 *  @author Daniel R. Collins (dcollins@superdan.net)
 *  @since 2014-05-20
 *  @version 1.0
 ******************************************************************************/

public class Attack {
    //--------------------------------------------------------------------------
    //  Fields
    //--------------------------------------------------------------------------

    /**
     * Name descriptor of this attack form.
     */
    String name;

    /**
     * net.superdan.athena.Attack bonus added to d20 hit rolls.
     */
    int bonus;

    /**
     * Rate of attacks per round.
     */
    int rate;

    /**
     * Damage dice on successful hit.
     */
    Dice damage;

    //--------------------------------------------------------------------------
    //  Constructors
    //--------------------------------------------------------------------------

    /**
     * Constructor (full fields).
     */
    Attack(String name, int rate, int bonus, Dice damage) {
        this.name = name;
        this.rate = rate;
        this.bonus = bonus;
        this.damage = damage;
    }

    /**
     * Constructor (rate, bonus, damage dice).
     */
    Attack(int rate, int bonus, int damDice) {
        this(null, rate, bonus, new Dice(damDice, 6));
    }

    /**
     * Constructor (bonus, damage dice).
     */
    Attack(int bonus, int damDice) {
        this(null, bonus, 1, new Dice(damDice, 6));
    }

    //--------------------------------------------------------------------------
    //  Methods
    //--------------------------------------------------------------------------
    public String getName() {
        return name;
    }

    public int getBonus() {
        return bonus;
    }

    public int getRate() {
        return rate;
    }

    public Dice getDamage() {
        return damage;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * Roll damage for successful hit.
     */
    public int rollDamage() {

        // Some attacks do 0 damage (e.g., carrion crawler)
        if (damage.getNum() <= 0)
            return damage.boundRoll(0);

            // Everything else does minimum 1 point (even w/penalties)
        else
            return damage.boundRoll(1);
    }

    /**
     * Identify this object as a string.
     */
    public String toString() {
        return (rate == 1 ? "" : rate + " ")
                + (name == null ? "Attack" : name) + " "
                + Dice.formatBonus(bonus)
                + " (" + damage + ")";
    }
}

