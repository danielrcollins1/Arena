package net.superdan.athena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterDatabaseTest {

    @Test
    void dataLoaded() {
        Dice.initialize();
        MonsterDatabase db = MonsterDatabase.getInstance();
        boolean monstersExist = false;
        String race = "";
        Dice hitDice = null;
        for (var monster : db) {
            monstersExist = true;
            race = monster.race;
            hitDice = monster.hitDice;
            break;
        }
        assertEquals(true, monstersExist, "No monsters in db");
        assertTrue(!race.isBlank(), "Monster race is unexpectedly blank");
        assertNotNull(hitDice, String.format("Monster %s hitDice unexpectedly blank", race));
    }
}