package at.mctg.app.service.battle;

import at.mctg.app.controller.Controller;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.BattleRepository;
import at.mctg.app.dal.repository.CardRepository;
import at.mctg.app.dal.repository.DeckRepository;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.model.Card;
import at.mctg.app.model.User;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;

import java.util.*;

public class BattleController extends Controller {

    public BattleController() {
        super();
    }
    private static User userInTheLoby = null;

    private List<Card> loadCards(Collection<UUID> cardIds, UnitOfWork uow) throws Exception {
        List<Card> deck = new ArrayList<>();

        for (UUID cardId : cardIds) {
            Card cardFromIdList = new CardRepository(uow).findByID(cardId);
            if (cardFromIdList != null) {
                deck.add(cardFromIdList);
            }
        }
        return deck;
    }

    private void applyElo(User challenger, User opponent, boolean challengerWon) {
        if (challengerWon) {
            challenger.setElo(challenger.getElo() + 3);
            opponent.setElo(opponent.getElo() - 5);
            challenger.setWins(challenger.getWins() + 1);
            opponent.setLosses(opponent.getLosses() + 1);
        } else {
            // opponent won
            opponent.setElo(opponent.getElo() + 3);
            challenger.setElo(challenger.getElo() - 5);
            opponent.setWins(opponent.getWins() + 1);
            challenger.setLosses(challenger.getLosses() + 1);
        }
    }

    private double computeDamage(Card attacker, Card defender) {
        // Check special combos that set damage to 0 or cause instant defeat (from the specs)

        String attackersName = attacker.getName().toLowerCase();
        String defendersName = defender.getName().toLowerCase();
        final double youCannotTouchMe = 0.0;

        // "Goblins are too afraid of Dragons to attack " => Goblin does 0 damage
        if (attackersName.contains("goblin") && defendersName.contains("dragon")) {
            return youCannotTouchMe;
        }

        // "Wizzard can control Orks so they are not able to damage them" => Ork does 0 damage
        if (attackersName.contains("ork") && defendersName.contains("wizzard")) {
            return youCannotTouchMe;
        }

        // "The armor of Knights is so heavy that WaterSpells make them drown instantly."
        // => Knight instantly loses
        if (attackersName.contains("knight") && defendersName.contains("waterspell")) {
            return youCannotTouchMe; // "drown instantly"
        }

        // "The Kraken is immune against spells" => if attacker is Spell vs. defender is "kraken" => 0
        if (attacker.getCardType() != null && attacker.getCardType().toLowerCase().contains("spell")
                && defendersName.contains("kraken")) {
            return youCannotTouchMe;
        }

        // "The FireElves know Dragons since they were little and can evade their attacks."
        if (attackersName.contains("dragon") && defendersName.contains("fireelf")) {
            return youCannotTouchMe;
        }


        double baseDamage = attacker.getDamage();

        // It it is a pure monster fight, no element effect is added, so the
        // base Damage of the attacking Card is returned
        boolean attackerIsSpell = (attacker.getCardType() != null
                && attacker.getCardType().toLowerCase().contains("spell"));
        boolean defenderIsSpell = (defender.getCardType() != null
                && defender.getCardType().toLowerCase().contains("spell"));

        if (!attackerIsSpell && !defenderIsSpell) {
            return baseDamage;
        }

        // If at least one Card is spell, the following  advantage logic is added:
        // water -> fire => x2
        // fire -> normal => x2
        // normal -> water => x2
        // if not effective => x0.5
        // else normal => same

        String attackingElement =
                (attacker.getElementType() != null)
                        ? attacker.getElementType().toLowerCase() : "";

        String defindingElement =
                (defender.getElementType() != null)
                        ? defender.getElementType().toLowerCase() : "";

        // water -> fire => attacker *2
        if (attackingElement.equals("water") && defindingElement.equals("fire")) {
            return baseDamage * 2;
        }

        // fire -> normal => attacker *2
        if (attackingElement.equals("fire") && defindingElement.equals("normal")) {
            return baseDamage * 2;
        }

        // normal -> water => attacker *2
        if (attackingElement.equals("normal") && defindingElement.equals("water")) {
            return baseDamage * 2;
        }

        // not effective => half damage => (the inverse pairs)
        // fire -> water => attacker *0.5
        if (attackingElement.equals("fire") && defindingElement.equals("water")) {
            return baseDamage * 0.5;
        }

        // normal -> fire => attacker *0.5
        if (attackingElement.equals("normal") && defindingElement.equals("fire")) {
            return baseDamage * 0.5;
        }

        // water -> normal => attacker *0.5
        if (attackingElement.equals("water") && defindingElement.equals("normal")) {
            return baseDamage * 0.5;
        }

        // else no effect => baseDamage
        return baseDamage;
    }

    //This is a helper Method for the Unique Feature
    // potential card level-up and increase in strength
    private void incrementUsageAndMaybeLevelUp(
            Card card,
            Map<UUID, Integer> usageMap,
            StringBuilder log,
            UnitOfWork unitOfWork
    ) {
        UUID cardId = card.getCardId();
        int oldCount = usageMap.getOrDefault(cardId, 0);
        int newCount = oldCount + 1;
        usageMap.put(cardId, newCount);

        // check if newCount is multiple of 5
        if (newCount % 5 == 0) {
            double oldDamage = card.getDamage();
            int oldLevel = card.getLevel();

            double newDamage = oldDamage * 1.2;
            int newLevel = oldLevel + 1;

            card.setDamage(newDamage);
            card.setLevel(newLevel);

            log.append("Card ").append(card.getName())
                    .append(" used ").append(newCount)
                    .append(" times => damage boosted by 20%! Now: ")
                    .append(newDamage).append(", level=")
                    .append(newLevel).append("\n");

            // store changes permanently => immediate effect
            new CardRepository(unitOfWork).updateCard(card);
        }
    }

    // The core Logic of the whole Project in terms of battel:
    //Up to 100 rounds
    // each round pick random card from each deck
    //if monster vs. monster => normal damage
    //if spell or monster => element advantage/ disadvantage
    // special rules: Goblin vs. Dragon, Wizzard vs. Ork, Knight vs. WaterSpell, Kraken vs. Spell, FireElves vs. Dragon
    // winner takes the loser's card
    //if one deck becomes empty => that user lost
    //or if 100 rounds => draw
    //ELO: +3 for winner, -5 for loser, no change if draw
    private String letsBattle(
            User challenger,
            User opponent,
            List<Card> deckChallenger,
            List<Card> deckOpponent,
            StringBuilder log,
            UnitOfWork unitOfWork)
    {
        // The fight last for 100 rounds max
        final int MAX_ROUNDS = 100;
        int currentRound = 1;

        // random generator for picking a card
        Random rand = new Random();

        // usageMap: how many times a given card has been used in this match
        Map<UUID, Integer> usageMap = new HashMap<>();

        while (currentRound <= MAX_ROUNDS) {

            // check if someone has 0 cards
            if (deckChallenger.isEmpty()) {
                log.append("Challenger has no cards left => Opponent wins!\n");
                // challenger lost
                applyElo(challenger, opponent, false);
                return "opponent";
            }
            if (deckOpponent.isEmpty()) {
                log.append("Opponent has no cards left => Challenger wins!\n");
                // challenger wins
                applyElo(challenger, opponent, true);
                return "challenger";
            }

            //Start new round
            log.append("\n--- Round ").append(currentRound).append(" ---\n");
            // pick random card from each deck
            Card cardChallanger = deckChallenger.get(rand.nextInt(deckChallenger.size()));
            Card cardOpponent = deckOpponent.get(rand.nextInt(deckOpponent.size()));

            log.append(challenger.getUsername())
                    .append(" plays [").append(cardChallanger.getName())
                    .append(" / ").append(cardChallanger.getDamage())
                    .append(", lvl=").append(cardChallanger.getLevel()).append("]\n");

            log.append(opponent.getUsername())
                    .append(" plays [").append(cardOpponent.getName())
                    .append(" / ").append(cardOpponent.getDamage())
                    .append(", lvl=").append(cardOpponent.getLevel()).append("]\n");

            //Increment usage for both cards and check if the card can level-up
            incrementUsageAndMaybeLevelUp(cardChallanger, usageMap, log, unitOfWork);
            incrementUsageAndMaybeLevelUp(cardOpponent, usageMap, log, unitOfWork);

            double firstDamage = computeDamage(cardChallanger, cardOpponent); // cardChallanger's effective damage vs cardOpponent
            double secondDamage = computeDamage(cardOpponent, cardChallanger); // cardOpponent's effective damage vs cardChallanger

            log.append(" => ").append(cardChallanger.getName()).append(" deals ").append(firstDamage).append(" vs. ")
                    .append(cardOpponent.getName()).append(" deals ").append(secondDamage).append("\n");

            if (Math.abs(firstDamage - secondDamage) < 0.0001) {
                // draw => no cards move
                log.append(" => Round is a DRAW!\n");
            } else if (firstDamage > secondDamage) {
                // cardChallanger wins => challenger gets cardOpponent
                log.append(" => ").append(challenger.getUsername()).append(" wins this round!\n");
                deckOpponent.remove(cardOpponent);
                deckChallenger.add(cardOpponent); // move cardOpponent to challenger's deck

                // usageMap reset for cardOpponent because new owner => usage=0
                usageMap.put(cardOpponent.getCardId(), 0);

                log.append("Card ").append(cardOpponent.getName())
                        .append(" is now owned by the challenger!\n");
            } else {
                // cardOpponent wins => opponent gets cardChallanger
                log.append(" => ").append(opponent.getUsername()).append(" wins this round!\n");
                deckChallenger.remove(cardChallanger);
                deckOpponent.add(cardChallanger);

                usageMap.put(cardChallanger.getCardId(), 0);

                log.append("Card ").append(cardChallanger.getName())
                        .append(" is now owned by the opponent!\n");
            }

            currentRound++;
        }

        // if we exit the while => 100 rounds done => draw
        log.append("\n=== 100 Rounds => BATTLE DRAW! ===\n");
        // ELO no change
        return "draw";
    }

    // POST /battles
    public Response startBattle(Request request) {

        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Invalid token\" }"
            );
        }
        String token = authHeader.substring("Bearer ".length());

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            User currentUser = new UserRepository(unitOfWork).findByAuthToken(token);
            if (currentUser == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"No user for token\" }"
                );
            }

            if (userInTheLoby == null) {
                // if no one is waiting in the loby
                userInTheLoby = currentUser;

                unitOfWork.commitTransaction();

                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"message\":\"You are waiting for a second player...\" }"
                );

            } else if (userInTheLoby.getUsername().equals(currentUser.getUsername())) {
                // if the same user calls startBattle() twice
                unitOfWork.commitTransaction();

                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"message\":\"You are already waiting in the lobby\" }"
                );

            } else {
                // a challenger waiting
                User challenger = userInTheLoby;
                // the second user that calls startBattle()
                User opponent   = currentUser;

                // clear the "loby" for next battles
                userInTheLoby = null;

                // load decks
                Collection<UUID> challengerIds = new DeckRepository(unitOfWork).getDeckByUser(challenger.getUsername());
                Collection<UUID> opponentIds   = new DeckRepository(unitOfWork).getDeckByUser(opponent.getUsername());

                List<Card> deckChallenger = loadCards(challengerIds, unitOfWork);
                List<Card> deckOpponent   = loadCards(opponentIds, unitOfWork);

                // starting the baatle
                StringBuilder battleLog = new StringBuilder();
                battleLog.append("=== Battle Start ===\n");
                battleLog.append(challenger.getUsername()).append(" VS ").append(opponent.getUsername()).append("\n");

                String result = letsBattle(challenger, opponent,
                        deckChallenger, deckOpponent, battleLog, unitOfWork);

                // store final stats
                // ELO, Wins, Losses might have changed
                new UserRepository(unitOfWork).updateUser(challenger);
                new UserRepository(unitOfWork).updateUser(opponent);

                // store in battles
                new BattleRepository(unitOfWork).saveBattle(challenger.getUsername(), opponent.getUsername(), result, battleLog.toString());

                unitOfWork.commitTransaction();

                return new Response(
                        HttpStatus.OK,
                        ContentType.PLAIN_TEXT,
                        battleLog.toString()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\":\"Internal Server Error\" }");
        }

    }
}