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
		Spell spellInfo = null;
		String getName () {
			String sName = getClass().getSimpleName();
			return sName.substring(0, sName.indexOf("Casting"));
		}
		void setSpellInfo (Spell s) { spellInfo = s; }
		abstract void cast (int level, Party targets);
	}

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

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

	/** Charm Person spell effect. */
	static class CharmPersonCasting extends Casting {
		void cast (int level, Party targets) {
			Monster target = targets.random();
			if (target.isPerson()) {
				target.saveVsCondition(SpecialType.Charm, level);
			}
		}
	}

	/** Magic Missile spell effect. */
	static class MagicMissileCasting extends Casting {
		void cast (int level, Party targets) {
			int numMissiles = Math.min((level + 1) / 2, 5);
			for (int i = 0; i < numMissiles; i++) {
				Monster target = targets.random();
				int damage = Dice.roll(6) + 1;
				target.saveVsEnergy(EnergyType.Other, damage, 
					SavingThrows.Type.Spells, level);
			}		
		}
	}

	/** Sleep spell effect. */
	static class SleepCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int effectHD = new Dice(2, 6).roll();
			for (Monster target: hitTargets) {
				if (target.getHD() <= 4 && target.getHD() <= effectHD) {
					effectHD -= target.getHD();
					target.saveVsCondition(SpecialType.Sleep, level);
				}
			}	
		}
	}

	/** Darkness spell effect. */
	static class DarknessCasting extends Casting {
		void cast (int level, Party targets) {

			// Treat as targeted blindness for simplicity
			Monster target = targets.random();
				target.saveVsCondition(SpecialType.Blindness, level);
		}
	}
	
	/** Web spell effect. */
	static class WebCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit); 
			for (Monster target: hitTargets) {
				target.saveVsCondition(SpecialType.Webs, level);
			}	
		}
	}
	
	/** Fireball spell effect. */
	static class FireballCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int numDice = Math.min(level, 10);
			int damage = new Dice(numDice, 6).roll();
			for (Monster target: hitTargets) {
				target.saveVsEnergy(EnergyType.Fire, damage, 
					SavingThrows.Type.Spells, level);
			}	
		}
	}

	/** Lightning Bolt spell effect. */
	static class LightningBoltCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int numDice = Math.min(level, 10);
			int damage = new Dice(numDice, 6).roll();
			for (Monster target: hitTargets) {
				target.saveVsEnergy(EnergyType.Volt, damage, 
					SavingThrows.Type.Spells, level);
			}	
		}
	}

	/** Hold Person spell effect. */
	static class HoldPersonCasting extends Casting {
		void cast (int level, Party targets) {

			// For utility, assume we can target up to 4 creatures in melee.
			// Contrast with S&S specifier of 3" dia. area effect.
			List<Monster> hitTargets = targets.randomGroup(4);
			int saveMod = hitTargets.size() == 1 ? -2 : 0;
			for (Monster target: hitTargets) {
				if (target.isPerson()) {
					target.saveVsCondition(SpecialType.Hold, level);
				}	
			}
		}
	}

	/** Suggestion spell effect. */
	static class SuggestionCasting extends Casting {
		void cast (int level, Party targets) {

			// Treat like a charm spell (order to leave, etc.)
			Monster target = targets.random();
			target.saveVsCondition(SpecialType.Charm, level);
		}
	}

	/** Confusion spell effect. */
	static class ConfusionCasting extends Casting {
		void cast (int level, Party targets) {

			// S&S gives an area for this spell, which we use
			int maxHit = spellInfo.getMaxTargetsInArea();
			int numEffect = new Dice(2, 6).roll();
			if (level > 8) numEffect += (level - 8);
			int numHit = Math.min(maxHit, numEffect);
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				target.saveVsCondition(SpecialType.Confusion, level);
			}	
		}
	}

	/** Fear spell effect. */
	static class FearCasting extends Casting {
		void cast (int level, Party targets) {

			// S&S give a circular area for this spell, which we use
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				target.saveVsCondition(SpecialType.Fear, level);
			}	
		}
	}

	/** Ice Storm spell effect. */
	static class IceStormCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int damage = new Dice(8, 6).roll();
			for (Monster target: hitTargets) {

				// For simplicity, assume this is all cold damage
				target.saveVsEnergy(EnergyType.Cold, damage, 
					SavingThrows.Type.Spells, level);
			}	
		}
	}

	/** Charm Monster spell effect. */
	static class CharmMonsterCasting extends Casting {
		void cast (int level, Party targets) {

			// OD&D Vol-1 gives an option for one creature or many of low level.
			// The former implies targeted use, the latter area-effect
			// (but our setup here can't handle both).
			// S&S sets an area of "1 monster" only;
			// we follow that for balance & simplicity.
			Monster target = targets.random();
			target.saveVsCondition(SpecialType.Charm, level);
		}
	}

	/** Polymorph Other spell effect. */
	static class PolymorphOtherCasting extends Casting {
		void cast (int level, Party targets) {
			Monster target = targets.random();
			target.saveVsCondition(SpecialType.Polymorphism, level);
		}
	}

	/** Cloudkill spell effect. */
	static class CloudkillCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				if (target.getHD() <= 6) {
					target.saveVsCondition(SpecialType.Death, level);
				}			
			}
		}
	}

	/** Hold Monster spell effect. */
	static class HoldMonsterCasting extends Casting {
		void cast (int level, Party targets) {

			// For utility, assume we can target up to 4 creatures in melee.
			// Contrast with S&S specifier of 3" dia. area effect.
			List<Monster> hitTargets = targets.randomGroup(4);
			int saveMod = hitTargets.size() == 1 ? -2 : 0;
			for (Monster target: hitTargets) {
				target.saveVsCondition(SpecialType.Hold, level);
			}
		}
	}

	/** Death Spell effect. */
	static class DeathSpellCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			int numDice = Math.min(level, 20);
			int effectHD = new Dice(numDice, 6).roll();
			for (Monster target: hitTargets) {
				if (target.getHD() <= 8 && target.getHD() <= effectHD) {
					effectHD -= target.getHD();
					target.saveVsCondition(SpecialType.Death, level);
				}			
			}
		}
	}

	/** Disintegrate spell effect. */
	static class DisintegrateCasting extends Casting {
		void cast (int level, Party targets) {
			Monster target = targets.random();
			target.saveVsCondition(SpecialType.Death, level);
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
