package net.superdan.athena;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class MapUtilTest {

    @Test
    void entriesSortedByValues() {
        Map<String, Integer> map = new TreeMap<>();
        map.put("A", 2);
        map.put("B", 3);
        map.put("C", 1);
        var actual = map.toString();
        assertEquals("{A=2, B=3, C=1}", actual, "Map not as expected");
        var sorted = MapUtil.entriesSortedByValues(map, false).toString();
        assertEquals("[C=1, A=2, B=3]", sorted, "Sorted map not as expected");
        var sortedReverse = MapUtil.entriesSortedByValues(map, true).toString();
        assertEquals("[B=3, A=2, C=1]", sortedReverse, "Reverse sort of map not as expected");
    }
}