package net.superdan.athena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AbilityTest {

    @Test
    void getBonus() {
        int[] expected = {-2, -2, -2, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2};
        int[] actual = new int[expected.length];
        for (int i = 3; i <= 18; i++) {
            // offset the array, since we didn't pad with the "missing" values from 0 .. 2
            actual[i - 3] = Ability.getBonus(i);
        }
        assertArrayEquals( expected, actual, "ODD Bonuses not as expected");

    }

    @Test
    void getBonus_BX() {
        int[] expected = {-3, -2, -2, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3};
        int[] actual = new int[expected.length];
        for (int i = 3; i <= 18; i++) {
            // offset the array, since we didn't pad with the "missing" values from 0 .. 2
            actual[i - 3] = Ability.getBonus_BX(i);
        }
        assertArrayEquals(expected, actual, "BX Bonuses not as expected");
    }

    @Test
    void getBonus_OED() {
        int[] expected = {-2, -2, -2, -1, -1, -1, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2};
        int[] actual = new int[expected.length];
        for (int i = 3; i <= 18; i++) {
            // offset the array, since we didn't pad with the "missing" values from 0 .. 2
            actual[i - 3] = Ability.getBonus_OED(i);
        }
        assertArrayEquals(expected, actual, "OED Bonuses not as expected");
    }
}