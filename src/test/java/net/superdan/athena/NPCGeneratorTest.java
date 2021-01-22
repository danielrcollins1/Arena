package net.superdan.athena;

import org.junit.jupiter.api.Test;

import static net.superdan.athena.Alignment.Lawful;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NPCGeneratorTest {

    public static final String WIZARD = "Wizard";
    public static final String LAWFUL = "Lawful";
    public static final String NEUTRAL = "Neutral";
    public static final String CHAOTIC = "Chaotic";
    public static final String THIEF = "Thief";
    public static final String HALFLING = "Halfling";
    public static final String FIGHTER = "Fighter";
    NPCGenerator Gen = new NPCGenerator();

    @Test
    void rollRace() {
        Dice.initialize(3);
        var actual = Gen.rollRace();
        assertEquals(HALFLING, actual);
    }

    @Test
    void rollClass() {
        Dice.initialize(3);
        var actual = Gen.rollClass();
        assertEquals(THIEF, actual);
    }

    @Test
    void rollElfClass() {
        Dice.initialize(1);
        var actual = Gen.rollClass();
        assertEquals(WIZARD, actual);
        Dice.initialize(3);
        actual = Gen.rollClass();
        assertEquals(THIEF, actual);
    }

    @Test
    void rollAlign() {
        Dice.initialize(1);
        var actual = Gen.rollAlign();
        assertEquals(LAWFUL, actual);
        Dice.initialize(2);
        actual = Gen.rollAlign();
        assertEquals(NEUTRAL, actual);
        Dice.initialize(6);
        actual = Gen.rollAlign();
        assertEquals(CHAOTIC, actual);
    }

    @Test
    void makeNPCFromProfile() {
        NPCGenerator.GenProfile profile = new NPCGenerator.GenProfile();
        profile.align = LAWFUL;
        profile.class1 = FIGHTER;
        profile.race = HALFLING;
        profile.level1 = 5;
        Character character = Gen.makeNPCFromProfile (profile);
        assertEquals(Lawful, character.alignment);
        assertEquals(HALFLING, character.race);
        assertEquals("Ftr5", character.classString(false));

    }
}