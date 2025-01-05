package at.mctg;

import at.mctg.app.model.User;
import at.mctg.app.service.battle.BattleLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EloLogicTest {
    private BattleLogic logic;

    @BeforeEach
    void setup() {
        logic = new BattleLogic();
    }

    @Test
    void testChallengerWinsElo() {
        // Arrange
        User c = makeUser("Challenger", 100);
        User o = makeUser("Opponent",   100);

        // Act => challenger wins
        logic.applyElo(c, o, true);

        // Assert => c=103, o=95
        assertEquals(103, c.getElo());
        assertEquals(95,  o.getElo());
        assertEquals(1,   c.getWins());
        assertEquals(1,   o.getLosses());
    }

    @Test
    void testOpponentWinsElo() {
        // Arrange
        User c = makeUser("C", 100);
        User o = makeUser("O", 100);

        // Act => opponent wins
        logic.applyElo(c, o, false);

        // Assert => c=95, o=103
        assertEquals(95,  c.getElo());
        assertEquals(103, o.getElo());
        assertEquals(1,   o.getWins());
        assertEquals(1,   c.getLosses());
    }

    @Test
    void testNoEloChangeIfDraw() {
        // Arrange
        User c = makeUser("C", 100);
        User o = makeUser("O", 100);

        // Act => no applyElo => draw => no changes

        // Assert
        assertEquals(100, c.getElo());
        assertEquals(100, o.getElo());
        assertEquals(0, c.getWins());
        assertEquals(0, o.getWins());
        assertEquals(0, c.getLosses());
        assertEquals(0, o.getLosses());
    }

    @Test
    void testChallengerWinsMultiple() {
        // Arrange
        User c = makeUser("C", 100);
        User o = makeUser("O", 100);

        // Act => c wins 2 times
        logic.applyElo(c, o, true);  // c => +3 => 103, o => -5 => 95
        logic.applyElo(c, o, true);  // c => 106, o => 90

        // Assert
        assertEquals(106, c.getElo());
        assertEquals(90,  o.getElo());
        assertEquals(2,   c.getWins());
        assertEquals(2,   o.getLosses());
    }

    @Test
    void testChallengerLoseThenWin() {
        // Arrange
        User c = makeUser("C", 100);
        User o = makeUser("O", 100);

        // Act => c loses => c=95, o=103; then c wins => c=98, o=98
        logic.applyElo(c, o, false); // c lost
        logic.applyElo(c, o, true);  // c won

        // Assert
        assertEquals(98, c.getElo());
        assertEquals(98, o.getElo());
        assertEquals(1, c.getWins());
        assertEquals(1, c.getLosses());
        assertEquals(1, o.getWins());
        assertEquals(1, o.getLosses());
    }

    @Test
    void testOpponentLoseThenWin() {
        // Arrange
        User c = makeUser("C", 100);
        User o = makeUser("O", 100);

        // Act => first c wins => c=103, o=95 => then o wins => c=98, o=98
        logic.applyElo(c, o, true);  // c wins
        logic.applyElo(c, o, false); // o wins

        // Assert
        assertEquals(98, c.getElo());
        assertEquals(98, o.getElo());
        assertEquals(1, c.getWins());
        assertEquals(1, c.getLosses());
        assertEquals(1, o.getWins());
        assertEquals(1, o.getLosses());
    }

    // Helper Method, just an easier way to create a user
    private User makeUser(String username, int elo) {
        User u = new User();
        u.setUsername(username);
        u.setElo(elo);
        u.setWins(0);
        u.setLosses(0);
        return u;
    }
}
