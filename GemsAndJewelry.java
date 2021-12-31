/******************************************************************************
*  Gems and jewelry valuations.
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-11-18
******************************************************************************/

public class GemsAndJewelry {

	/**
	*  Randomize one gem value.
	*/
	static int randomGemValue () {
		int roll = Dice.roll(100);
		if (roll <= 10)      return 10;
		else if (roll <= 25) return 50;
		else if (roll <= 75) return 100;
		else if (roll <= 90) return 500;
		else                 return 1000;
	}

	/**
	*  Randomize jewelry value class.
	*/
	static Dice randomJewelryClass () {
		int roll = Dice.roll(100);
		if (roll <= 20) 
			return new Dice(3, 6, 100, 0);
		else if (roll <= 80) 
			return new Dice(1, 6, 1000, 0);
		else 
			return new Dice(1, 10, 1000, 0);
	}
	
	/**
	*  Randomize one jewelry value.
	*/
	static int randomJewelryValue () {
		return randomJewelryClass().roll();	
	}
}

