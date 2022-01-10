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
		int maxTargetHD;
		EnergyType energy;
		SpecialType condition;
		boolean isPersonOnly;

		/** Constructor (all fields) */
		Casting (int maxTargetNum, int maxTargetHD, 
			EnergyType energy, SpecialType condition, boolean isPerson) 
		{
			this.maxTargetNum = maxTargetNum;
			this.maxTargetHD = maxTargetHD;
			this.energy = energy;
			this.condition = condition;
			this.isPersonOnly = isPerson;		
		}

		/** Constructor (energy-specific) */
		Casting (EnergyType energy) {
			this(NUM_BY_AREA, INF, energy, null, false);
		}

		/** Constructor (condition-specific, non-person) */
		Casting (int maxTargetNum, int maxTargetHD, SpecialType condition) {
			this(maxTargetNum, maxTargetHD, null, condition, false);	
		}

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
		
		/* Miscellaneous accessors */
		int getMaxTargetHD () { return maxTargetHD; }
		boolean isPersonEffect () { return isPersonOnly; }
		EnergyType getEnergy () { return energy; }
		SpecialType getCondition () { return condition; }

		/** Cast energy attack on a target monster. */
		void castEnergy (Monster target, int level, int damage) {
			assert(energy != null);
			target.catchEnergy(energy, damage, SavingThrows.Type.Spells, level);
		};
		
		/** Cast conditional attack on a target monster. */
		void castCondition (Monster target, int level, int saveMod) {
			assert(condition != null);
			if ((target.getHD() <= maxTargetHD)
				&& (!isPersonOnly || target.isPerson()))
			{
				target.catchCondition(condition, level, saveMod);
			}
		};

		/** Cast energy on random targets as per area. */
		void castEnergyOnArea (Party targets, int level, int damage) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castEnergy(target, level, damage);			
			}
		}
		
		/** Cast condition on random targets as per area. */
		void castConditionOnArea (Party targets, int level, int saveMod) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castCondition(target, level, saveMod);
			}
		}

		/** Cast the spell at a target party. */
		abstract void cast (int level, Party targets);
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
		new HoldMonsterCasting(), new DeathSpellCasting(), new DisintegrateCasting()
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
			super(1, INF, null, SpecialType.Charm, true);
		}
		void cast (int level, Party targets) {
			castCondition(targets.random(), level, 0);
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
			super(EnergyType.Other);
		}
		void cast (int level, Party targets) {
			int numMissiles = Math.min((level + 1) / 2, 5);
			for (int i = 0; i < numMissiles; i++) {
				Monster target = targets.random();
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
			super(NUM_BY_AREA, 4, SpecialType.Sleep);
		}
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int effectHD = new Dice(2, 6).roll();
			for (Monster target: hitTargets) {
				if (target.getHD() <= maxTargetHD && target.getHD() <= effectHD) {
					effectHD -= target.getHD();
					castCondition(target, level, 0);
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
			super(1, INF, SpecialType.Blindness);
		}
		void cast (int level, Party targets) {
			castCondition(targets.random(), level, 0);
		}
	}
	
	/** 
	*  Web spell effect. 
	*/
	static class WebCasting extends Casting {
		WebCasting () {
			super(NUM_BY_AREA, INF, SpecialType.Webs);
		}
		void cast (int level, Party targets) {
			castConditionOnArea(targets, level, 0);
		}
	}
	
	/** 
	*  Fireball spell effect. 
	*/
	static class FireballCasting extends Casting {
		FireballCasting () {
			super(EnergyType.Fire);
		}
		void cast (int level, Party targets) {
			int numDice = Math.min(level, 10);
			int damage = new Dice(numDice, 6).roll();
			castEnergyOnArea(targets, level, damage);
		}
	}

	/** 
	*  Lightning Bolt spell effect. 
	*/
	static class LightningBoltCasting extends Casting {
		LightningBoltCasting () {
			super(EnergyType.Volt);
		}
		void cast (int level, Party targets) {
			int numDice = Math.min(level, 10);
			int damage = new Dice(numDice, 6).roll();
			castEnergyOnArea(targets, level, damage);
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
			super(4, INF, null, SpecialType.Hold, true);
		}	
		void cast (int level, Party targets) {
			List<Monster> hitTargets = targets.randomGroup(maxTargetNum);
			int saveMod = (hitTargets.size() == 1) ? -2 : 0;
			for (Monster target: hitTargets) {
				castCondition(target, level, saveMod);
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
			super(1, INF, SpecialType.Charm);
		}
		void cast (int level, Party targets) {
			castCondition(targets.random(), level, 0);
		}
	}

	/** 
	*  Confusion spell effect. 
	*
	*  S&S gives an area for this spell, which we use
	*/
	static class ConfusionCasting extends Casting {
		ConfusionCasting () {
			super(NUM_BY_AREA, INF, SpecialType.Confusion);
		}
		void cast (int level, Party targets) {
			int maxHit = spellInfo.getMaxTargetsInArea();
			int numEffect = new Dice(2, 6).roll();
			if (level > 8) numEffect += (level - 8);
			int numHit = Math.min(maxHit, numEffect);
			List<Monster> hitTargets = targets.randomGroup(numHit);
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
			super(NUM_BY_AREA, INF, SpecialType.Fear);
		}
		void cast (int level, Party targets) {
			castConditionOnArea(targets, level, 0);
		}
	}

	/** 
	*  Ice Storm spell effect. 
	*/
	static class IceStormCasting extends Casting {
		IceStormCasting () {
			super(EnergyType.Cold);		
		}
		void cast (int level, Party targets) {
			int damage = new Dice(8, 6).roll();
			castEnergyOnArea(targets, level, damage);
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
			super(1, INF, SpecialType.Charm);
		}
		void cast (int level, Party targets) {
			castCondition(targets.random(), level, 0);
		}
	}

	/** 
	*  Polymorph Other spell effect. 
	*/
	static class PolymorphOtherCasting extends Casting {
		PolymorphOtherCasting () {
			super(1, INF, SpecialType.Polymorphism);
		}
		void cast (int level, Party targets) {
			castCondition(targets.random(), level, 0);
		}
	}

	/** 
	*  Cloudkill spell effect. 
	*/
	static class CloudkillCasting extends Casting {
		CloudkillCasting () {
			super(NUM_BY_AREA, 6, SpecialType.Death);
		}
		void cast (int level, Party targets) {
			castConditionOnArea(targets, level, 0);
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
			super(4, INF, SpecialType.Hold);
		}
		void cast (int level, Party targets) {
			List<Monster> hitTargets = targets.randomGroup(maxTargetNum);
			int saveMod = hitTargets.size() == 1 ? -2 : 0;
			for (Monster target: hitTargets) {
				castCondition(target, level, 0);
			}
		}
	}

	/** 
	*  Death Spell effect. 
	*/
	static class DeathSpellCasting extends Casting {
		DeathSpellCasting () {
			super(120, 8, SpecialType.Death);
		}
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int numDice = Math.min(level, 20);
			int effectHD = new Dice(numDice, 6).roll();
			for (Monster target: hitTargets) {
				if (target.getHD() <= maxTargetHD && target.getHD() <= effectHD) {
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
			super(1, INF, SpecialType.Disintegration);
		}
		void cast (int level, Party targets) {
			castCondition(targets.random(), level, 0);
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
		casting.cast(12, party);
		party.bringOutYourDead();
		System.out.println(party);
	}
}
