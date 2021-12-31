import java.util.*;

/******************************************************************************
*  Utilities for working with Java maps (dictionaries).
*  For theory, see: https://en.wikipedia.org/wiki/Associative_array
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2017-08-02
******************************************************************************/

public class MapUtil {

	//--------------------------------------------------------------------------
	//  Methods
	//--------------------------------------------------------------------------

	/**
	*  Return a SortedSet of a Map based on Comparable values.
	*  From: https://stackoverflow.com/questions/2864840/treemap-sort-by-value
	*/
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map, boolean reverse) {
		SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
			new Comparator<Map.Entry<K,V>>() {
				public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
					int res = e1.getValue().compareTo(e2.getValue());
					if (reverse) res = -res;
					return res != 0 ? res : 1;
				}
			}
		);
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}
	
	/**
	*	Main test method.
	*/
	public static void main	(String[] args) {
		Map<String, Integer> map = new TreeMap<String, Integer>();
		map.put("A", 2);
		map.put("B", 3);
		map.put("C", 1);   
		map.put("D", 2);
		System.out.println(map);
		System.out.println(entriesSortedByValues(map, false));
		System.out.println(entriesSortedByValues(map, true));
	}
	
	/*--------------------------------------------------------------------------
	*	If the sorting method above is somehow deficient, consider instead:
	*  https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	*-------------------------------------------------------------------------*/
}

