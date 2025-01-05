package at.mctg;

import at.mctg.app.model.Card;
import at.mctg.app.service.battle.BattleLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DamageRulesTest {
    private BattleLogic logic;

    @BeforeEach
    void setup() {
        logic = new BattleLogic();
    }

    @Test
    void testGoblinTooAfraidOfDragon() {
        // Arrange
        Card goblin = makeCard("WaterGoblin", 10.0, "water", "monster");
        Card dragon = makeCard("Dragon", 50.0, "normal", "monster");

        // Act
        double dmg = logic.computeDamage(goblin, dragon);

        // Assert => 0.0
        assertEquals(0.0, dmg, 0.0001);
    }

    @Test
    void testWizzardControlsOrk() {
        // Arrange
        Card ork = makeCard("Ork", 40.0, "normal", "monster");
        Card wizzard = makeCard("Wizzard", 35.0, "fire", "monster");

        // Act
        double dmg = logic.computeDamage(ork, wizzard);

        // Assert => 0.0
        assertEquals(0.0, dmg, 0.0001);
    }

    @Test
    void testKnightInstantLossToWaterSpell() {
        // Arrange
        Card knight = makeCard("Knight", 30.0, "normal", "monster");
        Card waterSpell = makeCard("WaterSpell", 25.0, "water", "spell");

        // Act
        double dmg = logic.computeDamage(knight, waterSpell);

        // Assert => 0.0
        assertEquals(0.0, dmg, 0.0001);
    }

    @Test
    void testKrakenImmuneAgainstSpell() {
        // Arrange
        Card kraken = makeCard("Kraken", 70.0, "water", "monster");
        Card fireSpell = makeCard("FireSpell", 40.0, "fire", "spell");

        // Act
        double dmgSpell = logic.computeDamage(fireSpell, kraken);

        // Assert => 0.0
        assertEquals(0.0, dmgSpell, 0.0001);
    }

    @Test
    void testFireElvesEvadeDragon() {
        // Arrange
        Card dragon = makeCard("Dragon", 50.0, "normal", "monster");
        Card fireElf = makeCard("FireElf", 20.0, "fire", "monster");

        // Act
        double dmgDragon = logic.computeDamage(dragon, fireElf);

        // Assert => 0.0
        assertEquals(0.0, dmgDragon, 0.0001);
    }

    @Test
    void testSpellDoubleDamage_FireToNormal() {
        // Arrange
        Card fireSpell = makeCard("FireSpell", 30.0, "fire", "spell");
        Card normalMonster = makeCard("RegularTroll", 35.0, "normal", "monster");

        // Act
        double dmg = logic.computeDamage(fireSpell, normalMonster);

        // Assert => 30.0 * 2 => 60
        assertEquals(60.0, dmg, 0.0001);
    }

    @Test
    void testSpellHalfDamage_FireToWater() {
        // Arrange
        Card fireSpell = makeCard("FireSpell", 30.0, "fire", "spell");
        Card waterMonster = makeCard("WaterGoblin", 15.0, "water", "monster");

        // Act
        double dmg = logic.computeDamage(fireSpell, waterMonster);

        // Assert => 30 * 0.5 => 15
        assertEquals(15.0, dmg, 0.0001);
    }

    @Test
    void testSpellDoubleDamage_WaterToFire() {
        // Arrange
        Card waterSpell = makeCard("WaterSpell", 25.0, "water", "spell");
        Card fireMonster = makeCard("FireTroll", 10.0, "fire", "monster");

        // Act
        double dmg = logic.computeDamage(waterSpell, fireMonster);

        // Assert => 25 * 2 => 50
        assertEquals(50.0, dmg, 0.0001);
    }

    // Helper Method just for an easier way for creating a card
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
