/******************************************************************************
*  Special ability types.
*  
*  Adding new types:
*  - Only add here if you also implement in code.
*  - All names here should be nouns or noun phrases.
*  - Add to category case methods as appropriate.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-07-24
******************************************************************************/

public enum SpecialType {

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	NPC, Poison, Paralysis, Petrification, BloodDrain, EnergyDrain,
	Constriction, Immolation, Rotting, Swallowing,
	SilverToHit, MagicToHit, ChopImmunity, ChopResistance,
	ManyHeads, Berserking, HitBonus, Invisibility, Detection, 
	Rending, SporeCloud, RockHurling, TailSpikes, Charm, Fear,
	SaveBonus, DodgeGiants, Regeneration, StrengthDrain, FleshEating, 
	Whirlwind, WallOfFire, ConeOfCold, AcidSpit, Confusion, 
	Displacement, Blinking, Phasing, CharmTouch, Dragon, 
	FireBreath, ColdBreath, VoltBreath, AcidBreath, PoisonBreath, 
	PetrifyingBreath, PetrifyingGaze, SummonVermin, SummonTrees,
	MindBlast, BrainConsumption, SappingStrands, Slowing, 
	FireImmunity, ColdImmunity, AcidImmunity, VoltImmunity, 
	SteamBreath, Stench, ResistStench, Webs, WebMove, Sleep, 
	Hold, Blindness, Polymorphism, Undead, Golem, Death, Spells,
	ManyEyeFunctions, MagicResistance, MagicImmunity, BlankMind,
	Fearlessness, ProtectionFromEvil, WoodEating, MetalEating,
	AntimagicSphere, BlownAway, Disintegration, Coma, Stun, 
	Feeblemind, Insanity, Hypnosis, SpellReflection, CharmPerTen,
	FireResistance, ColdResistance, AcidResistance, VoltResistance,
	FireVulnerability, ColdVulnerability, AcidVulnerability, 
	VoltVulnerability, PoisonSpit, DeathGaze, Smothering, Stunning,
	Shrieking, Conjuration, DropAttack, Slime, JointType, SetAttackBonus;
	
	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Find special type matching a string.
	*/
	static public SpecialType findByName (String s) {
		for (SpecialType t: SpecialType.values()) {
			if (s.equals(t.name())) {
				return t;
			}
		}
		return null;
	}
	
	/**
	*	Map condition to appropriate saving throw type.
	*/
	public SavingThrows.Type getSaveType () {
		switch (this) {

			// Spells saves
			case Charm: case Sleep: case Confusion:
			case Blindness: case Fear: case Polymorphism:
			case AntimagicSphere: case Hypnosis: 
			case Feeblemind:
				return SavingThrows.Type.Spells; 
				
			// Breath saves
			case Stench: case Rotting:
				return SavingThrows.Type.Breath;

			// Stone saves
			case Paralysis: case Petrification: 
			case Hold: case Webs: case Slowing:
			case SappingStrands: case Stunning:
				return SavingThrows.Type.Stone;

			// Death saves
			case Poison: case SporeCloud: case Death:
			case FleshEating: case BlownAway:
			case Disintegration: case EnergyDrain:
				return SavingThrows.Type.Death; 
		}	

		System.err.println("No saveType for condition: " + this);
		return null;
	}	
	
	/**
	*  Does this confer a disabling condition?
	*/
	public boolean isDisabling () {
		switch (this) {
			case Poison: case Paralysis: case Petrification: 
			case SporeCloud: case FleshEating: case Fear:  case Sleep: 
			case Charm: case Hold: case Polymorphism: case Death: 
			case BlownAway: case Disintegration: case BrainConsumption:
			case Coma: case Stun: case Feeblemind: case Insanity:
			case Hypnosis: case Stunning:
				return true;
		}
		return false;
	}

	/**
	*  Is this a breath weapon?
	*/
	public boolean isBreathWeapon () {
		switch (this) {
			case FireBreath: case ColdBreath: case AcidBreath:
			case VoltBreath: case PoisonBreath: case SteamBreath:
			case PetrifyingBreath:
				return true;
		}	
		return false;
	}
	
	/**
	*  Is this a gaze weapon?
	*/
	public boolean isGazeWeapon () {
		switch (this) {
			case PetrifyingGaze: case Confusion: case Hypnosis:
				return true;
		}	
		return false;
	}
	
	/**
	*  Is this a summons ability?
	*/
	public boolean isSummonsAbility () {
		switch (this) {
			case SummonVermin: case SummonTrees: case Shrieking:
				return true;
		}	
		return false;
	}

	/**
	*  Is this an attachment ability?
	*/
	public boolean isAttachmentAbility () {
		switch (this) {
			case BloodDrain: case Constriction: case Rending:
			case Smothering:
				return true;
		}	
		return false;
	}

	/**
	*  Is this a mental attack form?
	*/
	public boolean isMentalAttack () {
		switch (this) {
			case Charm: case Hold: case Sleep:
			case Fear: case Confusion: case MindBlast:
			case Hypnosis: case Feeblemind:
				return true;
		}	
		return false;
	}
}
