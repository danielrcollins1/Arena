/**
	One RPG character base class type.
	
	Every class is in one of the broad archetypes given here.
	We don't expect that this base class set should ever change,
	even if the codebase elsewhere is expanded to handle various
	subclasses (e.g., paladin, assassin, monk, etc.)
	This represents what 2E and 5.5E call "class groups".

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2023-04-30
*/

public enum BaseClassType {

   Fighter, Wizard, Cleric, Thief;

	/** Size of this enumeration. */
   private static final int SIZE = values().length;

	/** Number of prioritized abilities. */
   private static final int NUM_PRIORITIES = 3;

	/**
		Priorities for selecting ability scores.
		This array is in the same order as our enumeration here.
		The first item in each sub-array must be the prime requisite
		for the corresponding base class type.
	*/
   private static final Ability[][] ABILITY_PRIORITY = {
   	{Ability.Strength, Ability.Dexterity, Ability.Constitution},
   	{Ability.Intelligence, Ability.Dexterity, Ability.Constitution},
   	{Ability.Wisdom, Ability.Strength, Ability.Constitution},
   	{Ability.Dexterity, Ability.Strength, Ability.Intelligence}
   };

	/**
		Map a prime requisite to a base type.
	*/
   public static BaseClassType getTypeFromPrimeReq(Ability primeReq) {
      switch (primeReq) {
         case Strength: return Fighter;
         case Intelligence: return Wizard;
         case Wisdom: return Cleric;
         case Dexterity: return Thief;
         default: System.err.println("Unhandled prime requisite.");		
      }
      return null;
   }

	/**
		Make an array of weights based on ability priorities.
		Should have weights 3, 2, 1 in abilities noted in array.
	*/
   public int[] getAbilityPriorityWeights() {
      int[] weights = new int[Ability.size()];
      for (int i = 0; i < NUM_PRIORITIES; i++) {
         weights[ABILITY_PRIORITY[this.ordinal()][i].ordinal()] = 3 - i;
      }
      return weights;
   }
}
