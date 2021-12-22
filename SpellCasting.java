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

	// TODO:
	// - Spell immunity.
	// - Magic resistance.
	// - Undead immunity to mind-affecting stuff.
	// - Wisdom save bonuses to mind-affecting stuff?

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
				if (!target.rollSaveSpells())
					target.addCondition(SpecialType.Charm);
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
				boolean save = target.rollSaveSpells();
				target.takeDamage(save ? damage / 2 : damage);
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
					if (!target.rollSaveSpells())
						target.addCondition(SpecialType.Sleep);
				}
			}	
		}
	}

	/** Darkness spell effect. */
	static class DarknessCasting extends Casting {
		void cast (int level, Party targets) {

			// Treat as targeted blindness for simplicity
			Monster target = targets.random();
			if (!target.rollSaveSpells())
				target.addCondition(SpecialType.Blindness);
		}
	}
	
	/** Web spell effect. */
	static class WebCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit); 
			for (Monster target: hitTargets) {
				if (!target.rollSave(SavingThrows.Type.Stone)) {

					// Give one chance to break out by Strength
					int strength = target.getAbilityScore(Ability.Str);
					int strBonus = Ability.getBonus(strength);
					boolean breakOut = Dice.roll(6) <= strBonus;
					if (!breakOut) {
						target.addCondition(SpecialType.Webbing);
					}
				}
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
				if (!target.isImmuneToEnergy(EnergyType.Fire)) { 
					boolean saved = target.rollSaveSpells();
					target.takeDamage(saved ? damage/2 : damage);
				}
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
				if (!target.isImmuneToEnergy(EnergyType.Volt)) { 
					boolean saved = target.rollSaveSpells();
					target.takeDamage(saved ? damage/2 : damage);
				}
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
					if (!target.rollSave(SavingThrows.Type.Stone, saveMod))
						target.addCondition(SpecialType.Paralysis);
				}	
			}
		}
	}

	/** Suggestion spell effect. */
	static class SuggestionCasting extends Casting {
		void cast (int level, Party targets) {

			// Treat like a charm spell (order to leave, etc.)
			Monster target = targets.random();
			if (!target.rollSaveSpells())
				target.addCondition(SpecialType.Charm);
		}
	}

	/** Confusion spell effect. */
	static class ConfusionCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				if (!target.rollSaveSpells())
					target.addCondition(SpecialType.Confusion);
			}	
		}
	}

	/** Fear spell effect. */
	static class FearCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				if (!target.rollSaveSpells())
					target.addCondition(SpecialType.Fear);
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
				int myDamage = damage;
				if (target.isImmuneToEnergy(EnergyType.Cold))
					myDamage /= 2;
				boolean saved = target.rollSaveSpells();
				target.takeDamage(saved ? myDamage/2 : myDamage);
			}	
		}
	}

	/** Charm Monster spell effect. */
	static class CharmMonsterCasting extends Casting {
		void cast (int level, Party targets) {

			// For utility, assume we can target many creatures in melee.
			// Compare/contrast with S&S area specifier of 1 monster only.
			List<Monster> shuffleParty = targets.randomGroup(targets.size());
			Monster firstTarget = shuffleParty.get(0);
			if (firstTarget.getHD() >= 4) {
				if (!firstTarget.rollSaveSpells())
					firstTarget.addCondition(SpecialType.Charm);
			}
			else {
				int effectHD = new Dice(2, 6).roll();
				for (Monster target: shuffleParty) {
					if (target.getHD() < 4 && target.getHD() <= effectHD) {
						effectHD -= target.getHD();									
						if (!target.rollSaveSpells())
							target.addCondition(SpecialType.Charm);
					}
				}			
			}
		}
	}

	/** Polymorph Other spell effect. */
	static class PolymorphOtherCasting extends Casting {
		void cast (int level, Party targets) {
			Monster target = targets.random();
			if (!target.rollSaveSpells())
				target.addCondition(SpecialType.Polymorphism);
		}
	}

	/** Cloudkill spell effect. */
	static class CloudkillCasting extends Casting {
		void cast (int level, Party targets) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				if (target.getHD() <= 6) {
					if (!target.rollSave(SavingThrows.Type.Death))
						target.instaKill();
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
				if (!target.rollSave(SavingThrows.Type.Stone, saveMod))
					target.addCondition(SpecialType.Paralysis);
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
					if (!target.rollSave(SavingThrows.Type.Death))
						target.instaKill();
				}			
			}
		}
	}

	/** Disintegrate spell effect. */
	static class DisintegrateCasting extends Casting {
		void cast (int level, Party targets) {
			Monster target = targets.random();
			if (!target.rollSave(SavingThrows.Type.Death))
				target.instaKill();
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
