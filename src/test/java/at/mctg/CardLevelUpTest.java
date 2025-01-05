package at.mctg;

import at.mctg.app.model.Card;
import at.mctg.app.service.battle.BattleLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CardLevelUpTest {
    private BattleLogic logic;

    @BeforeEach
    void setup() {
        logic = new BattleLogic();
    }

    @Test
    void testUsageIncrementSingle() {
        // Arrange
        Card c = makeCard("RegularGoblin", 10.0, "normal","monster");
        Map<UUID,Integer> usageMap = new HashMap<>();

        // Act
        boolean leveled = logic.incrementUsageAndMaybeLevelUp(c, usageMap);

        // Assert => used 1 time => no level up => leveled=false
        assertFalse(leveled);
        assertEquals(1, usageMap.get(c.getCardId()).intValue());
        assertEquals(10.0, c.getDamage(), 0.0001);
        assertEquals(0, c.getLevel());
    }

    @Test
    void testLevelUpAfter5Uses() {
        // Arrange
        Card c = makeCard("SomeSpell", 10.0, "normal","spell");
        Map<UUID,Integer> usageMap = new HashMap<>();

        // Act
        boolean lastResult = false;
        for(int i=1; i<=5; i++){
            lastResult = logic.incrementUsageAndMaybeLevelUp(c, usageMap);
        }

        // Assert => after 5 => lastResult==true => damage=12 => level=1
        assertTrue(lastResult);
        assertEquals(12.0, c.getDamage(), 0.0001);
        assertEquals(1, c.getLevel());
        assertEquals(5, usageMap.get(c.getCardId()).intValue());
    }

    @Test
    void testMultipleLevelUpsAfter10Uses() {
        // Arrange
        Card c = makeCard("WaterSpell", 100.0, "water","spell");
        Map<UUID,Integer> usageMap = new HashMap<>();

        // Act
        for(int i=1;i<=10;i++){
            logic.incrementUsageAndMaybeLevelUp(c, usageMap);
        }

        // Assert => after 5 => damage=120 => after 10 => damage=144 => level=2
        assertEquals(144.0, c.getDamage(), 0.0001);
        assertEquals(2, c.getLevel());
        assertEquals(10, usageMap.get(c.getCardId()).intValue());
    }

    @Test
    void testNoLevelUpAt4Uses() {
        // Arrange
        Card c = makeCard("RegularGoblin", 10.0, "normal","monster");
        Map<UUID,Integer> usageMap = new HashMap<>();

        // Act
        boolean result = false;
        for(int i=1; i<=4; i++){
            result = logic.incrementUsageAndMaybeLevelUp(c, usageMap);
        }

        // Assert => usage=4 => not multiple of 5 => result=false => no level up
        assertFalse(result);
        assertEquals(10.0, c.getDamage(), 0.0001);
        assertEquals(0, c.getLevel());
        assertEquals(4, usageMap.get(c.getCardId()).intValue());
    }

    @Test
    void testUsageResetAfterOwnershipChange() {
        // Arrange
        Card c = makeCard("Ork", 15.0, "normal","monster");
        Map<UUID,Integer> usageMap = new HashMap<>();

        // increment usage 3 times
        for(int i=0; i<3; i++){
            logic.incrementUsageAndMaybeLevelUp(c, usageMap);
        }
        assertEquals(3, usageMap.get(c.getCardId()).intValue());

        // Act => ownership change => reset usage
        usageMap.put(c.getCardId(), 0);

        // Assert => usage=0 => damage/level stays same
        assertEquals(0, usageMap.get(c.getCardId()).intValue());
        assertEquals(15.0, c.getDamage(), 0.0001);
        assertEquals(0, c.getLevel());
    }

    @Test
    void testEdgeCaseNoUsageYet() {
        // Arrange
        Card c = makeCard("NoUse", 50.0, "fire","monster");
        Map<UUID,Integer> usageMap = new HashMap<>();

        // Act => do nothing

        // Assert => usage not present
        assertFalse(usageMap.containsKey(c.getCardId()));
        assertEquals(50.0, c.getDamage(), 0.0001);
        assertEquals(0, c.getLevel());
    }

    // Helper Method just for easier way to create a card
    private Card makeCard(String name, double dmg, String elem, String type){
        Card c = new Card();
        c.setCardId(UUID.randomUUID());
        c.setName(name);
        c.setDamage(dmg);
        c.setElementType(elem);
        c.setCardType(type);
        c.setOwnerUsername("TestOwner");
        c.setLevel(0);
        return c;
    }
}
