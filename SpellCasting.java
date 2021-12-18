import java.util.*;

/******************************************************************************
*  Code to handle casting spells in the combat simulator.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2021-12-18
******************************************************************************/

public class SpellCasting {

	// TODO:
	// - Make repository of castable spells
	// - Test with dummy solo Party, Wiz12
	// - Provide access to wizards picking those spells
	// - Magic resistance.
	// - Undead immunity to mind-affecting stuff.
	// - Wisdom save bonuses to mind-affecting stuff?
	// - Blindness prevents spell casting?

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** The singleton class instance. */
	static SpellCasting instance = null;

	//--------------------------------------------------------------------------
	//  Constructors
	//--------------------------------------------------------------------------

	/**
	*  Constructor.
	*/
	protected SpellCasting () {
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Access the singleton class instance.
	*/
	public static SpellCasting getInstance() {
		if (instance == null) {
			instance = new SpellCasting();
		}
		return instance;
	}

	/**
	*  Try to cast a spell on a given party.
	*  Targets will be limited by effect and randomly determined.
	*
	*  @spell The spell being cast.
	*  @level The level of the caster.
	*  @targets Party of potential targets.
	*  @return true if spell was successfully cast/handled.
	*/
	public static boolean tryCastSpell (Spell spell, int level, Party targets) {
		String sName = spell.getName();
		if (sName.equals("Magic Missile")) return castMagicMissile(spell, level, targets);
		if (sName.equals("Sleep")) return castSleep(spell, level, targets);
		if (sName.equals("Darkness")) return castDarkness(spell, level, targets);
		return false;
	}

	/**
	*  Magic Missile casting.
	*/
	private static boolean castMagicMissile (Spell spell, int level, Party targets) {
		Monster target = targets.random();
		int numMissiles = Math.min((level + 1) / 2, 5);
		int damage = new Dice(numMissiles, 6, numMissiles).roll();
		boolean save = target.rollSave(SavingThrows.SaveType.Spells);
		target.takeDamage(save ? damage / 2 : damage);
		return true;	
	}
	
	/**
	*  Sleep casting.
	*/
	private static boolean castSleep (Spell spell, int level, Party targets) {
		int numHit = spell.getMaxTargetsInArea();
		List<Monster> hitTargets = targets.randomGroup(numHit); 
		for (Monster target: hitTargets) {
			if (!target.rollSave(SavingThrows.SaveType.Spells))
				target.addCondition(SpecialType.Sleep);
		}	
		return true;	

		// TODO: Undead immunity.
	}

	/**
	*  Darkness casting.
	*/
	private static boolean castDarkness (Spell spell, int level, Party targets) {

		// For simplicity, we assume this blinds one target creature.
		Monster target = targets.random();
		if (!target.rollSave(SavingThrows.SaveType.Spells))
			target.addCondition(SpecialType.Blindness);
		return true;
	}
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	
	}
}

