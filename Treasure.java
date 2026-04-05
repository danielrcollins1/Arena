/**
	One treasure cache.

	@author Daniel R. Collins (dcollins@superdan.net)
	@since 2026-04-05
*/

public class Treasure {

	//--------------------------------------------------------------------------
	//  Enumeration
	//--------------------------------------------------------------------------

	/** Treasure categories. */
	public enum Category { 
		Copper, Silver, Electrum, Gold, Platinum, Gems, Jewelry, Magic;
		
		/** Cached array of values. */
		public static final Category[] VALUES = values();
	};

	//--------------------------------------------------------------------------
	//  Fields
	//--------------------------------------------------------------------------

	/** Treasure category amounts. */
	private int[] catAmount = new int[Category.VALUES.length];

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
		Get one amount.
	*/
	public int get(Category cat) {
		return catAmount[cat.ordinal()];	
	}

	/**
		Set one amount.
	*/
	public void set(Category cat, int amount) {
		catAmount[cat.ordinal()] = amount;
	}

	/**
		Add to one amount.
	*/
	public void add(Category cat, int amount) {
		catAmount[cat.ordinal()] += amount;
	}

	/**
		Get total monetary value (in gold piece standard).
	*/
	public int getValue() {
		return get(Category.Copper) / 50
			+ get(Category.Silver) / 10
			+ get(Category.Electrum) / 2
			+ get(Category.Gold) * 1
			+ get(Category.Platinum) * 5
			+ get(Category.Gems)
			+ get(Category.Jewelry);
	}
}
