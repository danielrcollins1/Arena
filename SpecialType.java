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
	Constriction, Corrosion, Immolation, Rotting, Swallowing,
	SilverToHit, MagicToHit, ChopImmunity, DamageReduction,
	Multiheads, Berserking, HitBonus, Invisibility, Detection, 
	Grabbing, SporeCloud, RockHurling, TailSpikes, Charm, Fear,
	SaveBonus, DodgeGiants, Regeneration, StrengthDrain, Absorption,
	Whirlwind, WallOfFire, ConeOfCold, AcidSpitting, Confusion, 
	Displacement, Blinking, Phasing, CharmTouch, Dragon, 
	FireBreath, ColdBreath, VoltBreath, AcidBreath, PoisonBreath, 
	PetrifyingBreath, PetrifyingGaze, SummonVermin, SummonTrees,
	MindBlast, BrainConsumption, SappingStrands, Slowing, 
	FireImmunity, ColdImmunity, AcidImmunity, VoltImmunity, 
	SteamBreath, Stench, ResistStench, Webbing, WebMove, Sleep, 
	Holding, Blindness, Polymorphism, Undead, Golem, 
	ManyEyeFunctions, Death;
	
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
				return SavingThrows.Type.Spells; 

			// Stone saves
			case Paralysis: case Petrification: 
			case Holding: case Webbing:
				return SavingThrows.Type.Stone;

			// Death saves
			case Poison: case SporeCloud: case Death:
				return SavingThrows.Type.Death; 
		}	
		System.err.println("Error: No saveType for condition: " + this);
		return null;
	}	
	
	/**
	*  Does this type confer a disabling condition?
	*/
	public boolean isDisabling () {
		switch (this) {
			case Poison: case Paralysis: case Petrification: 
			case Swallowing: case SporeCloud: case Absorption: 
			case Fear: case MindBlast: case Sleep: case Charm:
			case Holding: case Webbing: case Polymorphism: 
			case Death:
				return true;
		}
		return false;
	}

	/**
	*  Is this type a breath weapon?
	*/
	public boolean isBreathWeapon () {
		switch (this) {
			case FireBreath: case ColdBreath: case VoltBreath:
			case AcidBreath: case PoisonBreath: case PetrifyingBreath:
			case SteamBreath:
				return true;
		}	
		return false;
	}
	
	/**
	*  Is this a gaze weapon?
	*/
	public boolean isGazeWeapon () {
		switch (this) {
			case PetrifyingGaze: case Confusion: 
				return true;
		}	
		return false;
	}
	
	/**
	*  Is this type a summons ability?
	*/
	public boolean isSummonsAbility () {
		switch (this) {
			case SummonVermin: case SummonTrees:
				return true;
		}	
		return false;
	}
}
