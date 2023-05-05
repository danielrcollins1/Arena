import java.util.List;

/**
	Code to handle casting spells in the combat simulator.

	Compare use of Casting classes here to the Strategy pattern.
	Some implementations will be rough approximations of the real spell.
	Only attack spells are handled currently.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2021-12-18
*/

public class SpellCasting {

	//--------------------------------------------------------------------------
	//  Base Casting class
	//--------------------------------------------------------------------------

	/** Casting abstract base class. */
	public abstract static class Casting {

		/** Basic spell information. */
		protected Spell spellInfo;

		/** Maximum number of targets. */
		protected int maxTargetNum;

		/** Energy type of this spell. */
		protected EnergyType energy;

		/** Special condition inflicted by this spell. */
		protected SpecialType condition;

		/** 
			Is this an indirect attack spell?
			(e.g.: Conjure Elemental)
		*/
		protected boolean indirect;

		/** Set the linked spell object. */
		private void setSpellInfo(Spell s) { 
			spellInfo = s; 
		}

		/** Get short name of this spell casting. */
		private String getName() {
			String sName = getClass().getSimpleName();
			return sName.substring(0, sName.indexOf("Casting"));
		}
		
		/** Get the effective maximum targets we can hit. */
		protected int getMaxTargetNum() { 
			assert spellInfo != null;
			return maxTargetNum != NUM_BY_AREA 
				? maxTargetNum : spellInfo.getMaxTargetsInArea();
		}

		/** Cast energy at a given monster. */
		protected void castEnergy(Monster target, int level, int damage) {
			assert energy != null;
			if (isThreatTo(target)) {
				target.catchEnergy(energy, damage, SavingThrows.Type.Spells, level);
			}
		}

		/** Cast condition at a given monster. */
		protected void castCondition(Monster target, int level, int saveMod) {
			assert condition != null;
			if (isThreatTo(target)) {
				target.catchCondition(condition, level, saveMod);
			}
		}
		
		/** Cast energy on random targets as per area. */
		protected void castEnergyOnArea(Party targets, int level, int damage) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castEnergy(target, level, damage);
			}
		}
		
		/** Cast condition on random targets as per area. */
		protected void castConditionOnArea(Party targets, int level, int mod) {
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = targets.randomGroup(numHit);
			for (Monster target: hitTargets) {
				castCondition(target, level, mod);
			}
		}

		/** Is this spell a threat to a given monster? */
		public boolean isThreatTo(Monster m) {
			if (!indirect && m.isImmuneToMagic()) { return false; }
			if (energy != null && m.isImmuneToEnergy(energy)) { return false; }
			if (condition != null && m.isImmuneToCondition(condition)) { 
				return false; 
			}
			return true;
		}

		/** Cast this spell at an enemy party. */
		public abstract void cast(Monster caster, Party friends, Party enemies);
	}

	//--------------------------------------------------------------------------
	//  Constants
	//--------------------------------------------------------------------------

	/** Infinity integer. */
	private static final int INF = Integer.MAX_VALUE;
	
	/** Code to compute num affected by area. */
	private static final int NUM_BY_AREA = -1;

	/** List of available castings. */
	private static final Casting[] CASTING_FORMULA = {
		new SleepCasting(), new CharmPersonCasting(), 
		new MagicMissileCasting(), new DarknessCasting(), 
		new WebCasting(), new FireballCasting(), 
		new HoldPersonCasting(), new LightningBoltCasting(), 
		new SuggestionCasting(), new CharmMonsterCasting(), 
		new ConfusionCasting(), new FearCasting(),
		new IceStormCasting(), new PolymorphOtherCasting(), 
		new CloudkillCasting(), new HoldMonsterCasting(), 
		new DeathSpellCasting(), new DisintegrateCasting(),
		new ConjureElementalCasting(), new FeeblemindCasting(), 
		new DispelMagicCasting()
	};

	//--------------------------------------------------------------------------
	//  Main class methods
	//--------------------------------------------------------------------------

	/**
		Try to link a spell with its casting formula.
		@return true if casting was found.
	*/
	public static boolean linkSpellWithCasting(Spell spell) {
		String shortName = spell.getName().replaceAll(" ", "");
		for (Casting c: CASTING_FORMULA) {
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
		Charm Person spell effect.
	*/
	static class CharmPersonCasting extends Casting {

		/** Constructor. */
		private CharmPersonCasting() {
			condition = SpecialType.Charm;
			maxTargetNum = 1;
		}

		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.isPerson();
		}

		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
		Magic Missile spell effect.

		While this spell can technically hit up to 5 targets,
		we don't expose that, since it's level-dependent,
		not disabling, and we don't want to prioritize this
		spell over others like hold or charm.
	*/
	static class MagicMissileCasting extends Casting {
	
		/** Constructor. */
		private MagicMissileCasting() {
			energy = EnergyType.Other;
			maxTargetNum = 1;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
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
		Sleep spell effect.
	*/
	static class SleepCasting extends Casting {
	
		/** Constructor. */
		private SleepCasting() {
			condition = SpecialType.Sleep;
			maxTargetNum = NUM_BY_AREA;
		}
		
		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.getHD() <= 4;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
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
		Darkness spell effect.

		Treat as targeted blindness for simplicity
	*/
	static class DarknessCasting extends Casting {
	
		/** Constructor. */
		private DarknessCasting() {
			condition = SpecialType.Blindness;
			maxTargetNum = 1;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}
	
	/** 
		Web spell effect.
	*/
	static class WebCasting extends Casting {
	
		/** Constructor. */
		private WebCasting() {
			condition = SpecialType.Webs;
			maxTargetNum = NUM_BY_AREA;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castConditionOnArea(enemies, caster.getLevel(), 0);
		}
	}
	
	/** 
		Fireball spell effect.
	*/
	static class FireballCasting extends Casting {
	
		/** Constructor. */
		private FireballCasting() {
			energy = EnergyType.Fire;
			maxTargetNum = NUM_BY_AREA;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			int level = caster.getLevel();
			int numDice = Math.min(level, 12);
			int damage = new Dice(numDice, 6).roll();
			castEnergyOnArea(enemies, level, damage);
		}
	}

	/** 
		Lightning Bolt spell effect.
	*/
	static class LightningBoltCasting extends Casting {
	
		/** Constructor. */
		private LightningBoltCasting() {
			energy = EnergyType.Volt;
			maxTargetNum = NUM_BY_AREA;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			int level = caster.getLevel();
			int numDice = Math.min(level, 12);
			int damage = new Dice(numDice, 6).roll();
			castEnergyOnArea(enemies, level, damage);
		}
	}

	/** 
		Hold Person spell effect.

		While OED gives this an optional area-of-effect use-case
		(following S&S), testing shows that in our overall context,
		it's more valuable to reserve this for individual-only use
		during the melee portion of a fight.
	*/
	static class HoldPersonCasting extends Casting {
	
		/** Constructor. */
		private HoldPersonCasting() {
			condition = SpecialType.Hold;
			maxTargetNum = 1;
		}	

		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.isPerson();
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
			castCondition(enemies.random(), caster.getLevel(), -2);
		}
	}

	/** 
		Suggestion spell effect.

		Treat like a charm spell (order to leave, etc.)
	*/
	static class SuggestionCasting extends Casting {
	
		/** Constructor. */
		private SuggestionCasting() {
			condition = SpecialType.Charm;
			maxTargetNum = 1;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
		Dispel Magic effect.

		Used to remove enemy conjured elementals.
	*/
	static class DispelMagicCasting extends Casting {
	
		/** Constructor. */
		private DispelMagicCasting() {
			maxTargetNum = 1;
		}

		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) 
				&& m.hasCondition(SpecialType.Conjuration);
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
			List<Monster> targets = enemies.randomGroup(enemies.size());
			for (Monster target: targets) {
				if (isThreatTo(target)) {
					target.catchDispel(enemies);
					break;
				}
			}
		}
	}

	/** 
		Confusion spell effect.

		S&S gives an area for this spell, adjusted in OED.
	*/
	static class ConfusionCasting extends Casting {
	
		/** Constructor. */
		private ConfusionCasting() {
			condition = SpecialType.Confusion;
			maxTargetNum = NUM_BY_AREA;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
			int numHit = Math.min(new Dice(2, 6).roll(),
				spellInfo.getMaxTargetsInArea());
			List<Monster> hitTargets = enemies.randomGroup(numHit);
			for (Monster target: hitTargets) {
				if (isThreatTo(target)) {
					castCondition(target, caster.getLevel(), 0);
				}			
			}
		}
	}

	/** 
		Fear spell effect.

		S&S give a circular area for this spell, which we use
	*/
	static class FearCasting extends Casting {
	
		/** Constructor. */
		private FearCasting() {
			condition = SpecialType.Fear;
			maxTargetNum = NUM_BY_AREA;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castConditionOnArea(enemies, caster.getLevel(), 0);
		}
	}

	/** 
		Ice Storm spell effect.
	*/
	static class IceStormCasting extends Casting {
	
		/** Constructor. */
		private IceStormCasting() {
			energy = EnergyType.Cold;
			maxTargetNum = NUM_BY_AREA;
		}

		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			int damage = new Dice(8, 6).roll();
			castEnergyOnArea(enemies, caster.getLevel(), damage);
		}
	}

	/** 
		Charm Monster spell effect.
	*	
		See comments above re: Hold Person.
		We reserve this spell for individual-targeting in melee
		(and ignore the option for an area-based effect).
	*/
	static class CharmMonsterCasting extends Casting {
	
		/** Constructor. */
		private CharmMonsterCasting() {
			condition = SpecialType.Charm;
			maxTargetNum = 1;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
		Polymorph Other spell effect.
	*/
	static class PolymorphOtherCasting extends Casting {
	
		/** Constructor. */
		private PolymorphOtherCasting() {
			condition = SpecialType.Polymorphism;
			maxTargetNum = 1;
		}

		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.isLivingType();
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	/** 
		Cloudkill spell effect.
	*/
	static class CloudkillCasting extends Casting {
	
		/** Constructor. */
		private CloudkillCasting() {
			condition = SpecialType.Death;
			maxTargetNum = NUM_BY_AREA;
		}

		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.getHD() <= 6;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castConditionOnArea(enemies, caster.getLevel(), 0);
		}
	}

	/** 
		Hold Monster spell effect.

		See comment above for Hold Person.
	*/
	static class HoldMonsterCasting extends Casting {
	
		/** Constructor. */
		private HoldMonsterCasting() {
			condition = SpecialType.Hold;
			maxTargetNum = 1;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
			castCondition(enemies.random(), caster.getLevel(), -2);
		}
	}

	/** 
		Conjure Elemental effect.
	*/
	static class ConjureElementalCasting extends Casting {
	
		/** Constructor. */
		private ConjureElementalCasting() {
			indirect = true;
			maxTargetNum = 0;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
			caster.conjureElemental(friends);
		}
	}

	/** 
		Feeblemind effect.
	*/
	static class FeeblemindCasting extends Casting {
	
		/** Constructor. */
		private FeeblemindCasting() {
			condition = SpecialType.Feeblemind;
			maxTargetNum = 1;
		}

		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.hasSpells();
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {
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
		Death Spell effect.
	*/
	static class DeathSpellCasting extends Casting {
	
		/** Constructor. */	
		private DeathSpellCasting() {
			condition = SpecialType.Death;
			maxTargetNum = 36;
		}
		
		@Override
		public boolean isThreatTo(Monster m) {
			return super.isThreatTo(m) && m.getHD() <= 8;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			int numHit = spellInfo.getMaxTargetsInArea();
			List<Monster> hitTargets = enemies.randomGroup(numHit);
			int effectHD = new Dice(10, 6).roll();
			for (Monster target: hitTargets) {
				if (isThreatTo(target) && target.getHD() <= effectHD) {
					effectHD -= target.getHD();
					castCondition(target, caster.getLevel(), 0);
				}			
			}
		}
	}

	/** 
		Disintegrate spell effect.
	*/
	static class DisintegrateCasting extends Casting {
	
		/** Constructor. */
		private DisintegrateCasting() {
			condition = SpecialType.Disintegration;
			maxTargetNum = 1;
		}
		
		@Override
		public void cast(Monster caster, Party friends, Party enemies) {		
			castCondition(enemies.random(), caster.getLevel(), 0);
		}
	}

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------
	
	/**
		Main test function.
	*/
	public static void main(String[] args) {	

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
