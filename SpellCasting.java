import java.util.*;

/******************************************************************************
*  Code to handle casting spells in the combat simulator.
*
*  Compare use of Casting classes here to the Strategy pattern.
*  Some implementations will be rough approximations of the real spell.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2021-12-18
******************************************************************************/

public class SpellCasting {

	//--------------------------------------------------------------------------
	//  Base Casting class
	//--------------------------------------------------------------------------

	/** Casting abstract base class. */
	public static abstract class Casting {

		/* Fields */
		Spell spellInfo;
		int maxTargetNum;
		boolean indirect;
		EnergyType energy;
		SpecialType condition;

		/** Set the linked spell object. */
		void setSpellInfo (Spell s) { spellInfo = s; }

		/** Get short name of this spell casting. */
		String getName () {
			String sName = getClass().getSimpleName();
			return sName.substring(0, sName.indexOf("Casting"));
		}
		
		/** Get the effective maximum targets we can hit. */
		int getMaxTargetNum () { 
			assert(spellInfo != null);
			return maxTargetNum != NUM_BY_AREA ?
				maxTargetNum : spellInfo.getMaxTargetsInArea();
		}

		/** Cast energy at a given monster. */
		void castEnergy (Monster target, int level, int damage) 
		{
			assert(energy != null);
			if (isThreatTo(target)) {
				target.catchEnergy(energy, damage, SavingThrows.Type.Spells, level);
			}
		}

		/** Cast condition at a given monster. */
		void castCondition (Monster target, int level, int saveMod) 
		{
			assert(condition != null);
			if (isThreatTo(target)) {
				target.catchCondition(condition, level, saveMod);
			}
		}
		
		/** Cast energy on random targets as per area. */
		void castEnergyOnArea (Party targets, int level, int damage) 
		{
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castEnergy(target, level, damage);
			}
		}
		
		/** Cast condition on random targets as per area. */
		void castConditionOnArea (Party targets, int level, int saveMod) 
		{
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castCondition(target, level, saveMod);
			}
		}

		/** See if this spell is a threat to a given monster. */
		boolean isThreatTo (Monster m) {
			if (!indirect && m.isImmuneToMagic()) return false;
			if (energy != null && m.isImmuneToEnergy(energy)) return false;
			if (condition != null && m.isImmuneToCondition(condition)) return false;
			return true;
		}

		/** Cast the spell at a target party. */
		abstract void cast (Monster caster, Party friends, Party enemies);
	}

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Infinity integer. */
	private static final int INF = Integer.MAX_VALUE;
	
	/** Code to compute num affected by area. */
	private static final int NUM_BY_AREA = -1;

	/** List of available castings. */
	private static final Casting castingFormula [] = {
		new SleepCasting(), new CharmPersonCasting(), new MagicMissileCasting(),
		new DarknessCasting(), new WebCasting(), new FireballCasting(), 
		new HoldPersonCasting(), new LightningBoltCasting(), new SuggestionCasting(), 
		new CharmMonsterCasting(), new ConfusionCasting(), new FearCasting(),
		new IceStormCasting(), new PolymorphOtherCasting(), new CloudkillCasting(),
		new HoldMonsterCasting(), new DeathSpellCasting(), new DisintegrateCasting(),
		new ConjureElementalCasting(), new FeeblemindCasting()
	};

	//--------------------------------------------------------------------------
	//  Main class methods
	//--------------------------------------------------------------------------

	/**
	*  Try to link a spell with its casting formula.
	*  @return true if casting was found.
	*/
	public static boolean linkSpellWithCasting (Spell spell) {
		String shortName = spell.getName().replaceAll(" ", "");
		for (Casting c: castingFormula) {
			if (c.getName().equals(shortName)) {
				c.setSpellInfo(spell);
				spell.setCasting(c);
				return true;
			}		
		}
		return false;	
	}

	//--------------------------------------------------------------------------
	//  Spell-specific Casting subclasses
	//--------------------------------------------------------------------------

	/** 
	*  Charm Person spell effect. 
	*/
	static class CharmPersonCasting extends Casting {
		CharmPersonCasting () {
			condition = SpecialType.Charm;
			maxTargetNum = 1;
		}
		boolean isThreatTo (Monster m) {
			return super.isThreatTo(m) && m.isPerson();
		}
		void cast (Monster caster, Party friends, Party enemies) {
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
	*  Magic Missile spell effect. 
	*
	*  While this spell can technically hit up to 5 targets,
	*  we don't expose that, since it's level-dependent, 
	*  not disabling, and we don't want to prioritize this 
	*  spell over others like hold or charm.
	*/
	static class MagicMissileCasting extends Casting {
		MagicMissileCasting () {
			energy = EnergyType.Other;
			maxTargetNum = 1;
		}
		void cast (Monster caster, Party friends, Party enemies) {
			int level = caster.getLevel();
			int numMissiles = Math.min((level + 1) / 2, 5);
			for (int i = 0; i < numMissiles; i++) {
				Monster target = enemies.random();
				int damage = Dice.roll(6) + 1;
				castEnergy(target, level, damage);
			}		
		}
	}

	/** 
	*  Sleep spell effect. 
	*/
	static class SleepCasting extends Casting {
		SleepCasting () {
			condition = SpecialType.Sleep;
			maxTargetNum = NUM_BY_AREA;
		}
		boolean isThreatTo (Monster m) {
			return super.isThreatTo(m) && m.getHD() <= 4;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = enemies.randomGroup(numHit);
			int effectHD = new Dice(2, 6).roll();
			for (Monster target: hitTargets) {
				if (isThreatTo(target) && target.getHD() <= effectHD) {
					effectHD -= target.getHD();
					castCondition(target, caster.getLevel(), 0);
				}
			}	
		}
	}

	/** 
	*  Darkness spell effect. 
	*
	*  Treat as targeted blindness for simplicity
	*/
	static class DarknessCasting extends Casting {
		DarknessCasting () {
			condition = SpecialType.Blindness;
			maxTargetNum = 1;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}
	
	/** 
	*  Web spell effect. 
	*/
	static class WebCasting extends Casting {
		WebCasting () {
			condition = SpecialType.Webs;
			maxTargetNum = NUM_BY_AREA;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castConditionOnArea(enemies, caster.getLevel(), 0);
		}
	}
	
	/** 
	*  Fireball spell effect. 
	*/
	static class FireballCasting extends Casting {
		FireballCasting () {
			energy = EnergyType.Fire;
			maxTargetNum = NUM_BY_AREA;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			int level = caster.getLevel();
			int numDice = Math.min(level, 10);
			int damage = new Dice(numDice, 6).roll();
			castEnergyOnArea(enemies, level, damage);
		}
	}

	/** 
	*  Lightning Bolt spell effect. 
	*/
	static class LightningBoltCasting extends Casting {
		LightningBoltCasting () {
			energy = EnergyType.Volt;
			maxTargetNum = NUM_BY_AREA;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			int level = caster.getLevel();
			int numDice = Math.min(level, 10);
			int damage = new Dice(numDice, 6).roll();
			castEnergyOnArea(enemies, level, damage);
		}
	}

	/** 
	*  Hold Person spell effect. 
	*
	*  For utility, assume we can target up to 4 creatures in melee.
	*  Contrast with S&S specifier of 3" dia. area effect.
	*/
	static class HoldPersonCasting extends Casting {
		HoldPersonCasting () {
			condition = SpecialType.Hold;
			maxTargetNum = 4;
		}	
		boolean isThreatTo (Monster m) {
			return super.isThreatTo(m) && m.isPerson();
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			List<Monster> hitTargets = enemies.randomGroup(maxTargetNum);
			int saveMod = (hitTargets.size() == 1) ? -2 : 0;
			for (Monster target: hitTargets) {
				castCondition(target, caster.getLevel(), saveMod);
			}
		}
	}

	/** 
	*  Suggestion spell effect. 
	*
	*  Treat like a charm spell (order to leave, etc.)
	*/
	static class SuggestionCasting extends Casting {
		SuggestionCasting () {
			condition = SpecialType.Charm;
			maxTargetNum = 1;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
	*  Confusion spell effect. 
	*
	*  S&S gives an area for this spell, which we use
	*/
	static class ConfusionCasting extends Casting {
		ConfusionCasting () {
			condition = SpecialType.Confusion;
			maxTargetNum = NUM_BY_AREA;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			int level = caster.getLevel();
			int maxHit = spellInfo.getMaxTargetsInArea();
			int numEffect = new Dice(2, 6).roll();
			if (level > 8) numEffect += (level - 8);
			int numHit = Math.min(maxHit, numEffect);
			List<Monster> hitTargets = enemies.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castCondition(target, level, 0);
			}	
		}
	}

	/** 
	*  Fear spell effect. 
	*
	*  S&S give a circular area for this spell, which we use
	*/
	static class FearCasting extends Casting {
		FearCasting () {
			condition = SpecialType.Fear;
			maxTargetNum = NUM_BY_AREA;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castConditionOnArea(enemies, caster.getLevel(), 0);
		}
	}

	/** 
	*  Ice Storm spell effect. 
	*/
	static class IceStormCasting extends Casting {
		IceStormCasting () {
			energy = EnergyType.Cold;
			maxTargetNum = NUM_BY_AREA;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			int damage = new Dice(8, 6).roll();
			castEnergyOnArea(enemies, caster.getLevel(), damage);
		}
	}

	/** 
	*  Charm Monster spell effect. 
	*	
	*  OD&D Vol-1 gives an option for one creature or many of low level.
	*  The former implies targeted use, the latter area-effect
	*  (but our setup here can't handle both).
	*  S&S sets an area of "1 monster" only;
	*  we follow that for balance & simplicity.
	*/
	static class CharmMonsterCasting extends Casting {
		CharmMonsterCasting () {
			condition = SpecialType.Charm;
			maxTargetNum = 1;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
	*  Polymorph Other spell effect. 
	*/
	static class PolymorphOtherCasting extends Casting {
		PolymorphOtherCasting () {
			condition = SpecialType.Polymorphism;
			maxTargetNum = 1;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
	*  Cloudkill spell effect. 
	*/
	static class CloudkillCasting extends Casting {
		CloudkillCasting () {
			condition = SpecialType.Death;
			maxTargetNum = NUM_BY_AREA;
		}
		boolean isThreatTo (Monster m) {
			return super.isThreatTo(m) && m.getHD() <= 6;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castConditionOnArea(enemies, caster.getLevel(), 0);
		}
	}

	/** 
	*  Hold Monster spell effect. 
	*
	*  For utility, assume we can target up to 4 creatures in melee.
	*  Contrast with S&S specifier of 3" dia. area effect.
	*/
	static class HoldMonsterCasting extends Casting {
		HoldMonsterCasting () {
			condition = SpecialType.Hold;
			maxTargetNum = 4;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			List<Monster> hitTargets = enemies.randomGroup(maxTargetNum);
			int saveMod = hitTargets.size() == 1 ? -2 : 0;
			for (Monster target: hitTargets) {
				castCondition(target, caster.getLevel(), 0);
			}
		}
	}

	/** 
	*  Conjure Elemental effect. 
	*/
	static class ConjureElementalCasting extends Casting {
		ConjureElementalCasting () {
			indirect = true;
			maxTargetNum = 0;
		}
		void cast (Monster caster, Party friends, Party enemies) {
			caster.conjureElemental(friends);
		}
	}

	/** 
	*  Feeblemind effect. 
	*/
	static class FeeblemindCasting extends Casting {
		FeeblemindCasting () {
			condition = SpecialType.Feeblemind;
			maxTargetNum = 1;
		}
		boolean isThreatTo (Monster m) {
			return super.isThreatTo(m) && m.hasSpells();
		}
		void cast (Monster caster, Party friends, Party enemies) {
			List<Monster> targets = enemies.randomGroup(enemies.size());
			for (Monster target: targets) {
				if (isThreatTo(target)) {
					castCondition(target, caster.getLevel(), -4);
					break;
				}
			}
		}
	}

	/** 
	*  Death Spell effect. 
	*/
	static class DeathSpellCasting extends Casting {
		DeathSpellCasting () {
			condition = SpecialType.Death;
			maxTargetNum = 120;
		}
		boolean isThreatTo (Monster m) {
			return super.isThreatTo(m) && m.getHD() <= 8;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			int level = caster.getLevel();
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = enemies.randomGroup(numHit);
			int numDice = Math.min(level, 20);
			int effectHD = new Dice(numDice, 6).roll();
			for (Monster target: hitTargets) {
				if (isThreatTo(target) && target.getHD() <= effectHD) {
					effectHD -= target.getHD();
					castCondition(target, level, 0);
				}			
			}
		}
	}

	/** 
	*  Disintegrate spell effect. 
	*/
	static class DisintegrateCasting extends Casting {
		DisintegrateCasting () {
			condition = SpecialType.Disintegration;
			maxTargetNum = 1;
		}
		void cast (Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	
	/**
	*  Main test function.
	*/
	public static void main (String[] args) {	

		// Create target party
		Dice.initialize();
		Monster monster = new Monster("Orc", 6, 9, 1, 1);
		Party party = new Party(monster, 10);
		System.out.println(party);

		// Cast a spell
		Casting casting = new MagicMissileCasting();
		System.out.println("Casting " + casting.getName());
		casting.cast(monster, party, party);
		party.bringOutYourDead();
		System.out.println(party);
	}
}
