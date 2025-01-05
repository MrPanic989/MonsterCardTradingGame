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

//communication with DB
public class BattleController extends Controller {

    public BattleController() {
        super();
    }
    private static User userInTheLoby = null;

    //for the "pure" logic
    private final BattleLogic logic = new BattleLogic();


    //Helper for loadich Cards from DB by their ID
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

    // POST /battles
    // The core Class of the whole Project in terms of battel:
    //Up to 100 rounds
    // each round pick random card from each deck
    //if monster vs. monster => normal damage
    //if spell or monster => element advantage/ disadvantage
    // special rules: Goblin vs. Dragon, Wizzard vs. Ork, Knight vs. WaterSpell, Kraken vs. Spell, FireElves vs. Dragon
    // winner takes the loser's card
    //if one deck becomes empty => that user lost
    //or if 100 rounds => draw
    //ELO: +3 for winner, -5 for loser, no change if draw
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


                // The fight last for 100 rounds max
                final int MAX_ROUNDS = 100;
                int currentRound = 1;
                String result = "draw";

                // random generator for picking a card
                Random rand = new Random();

                // usageMap: how many times a given card has been used in this match
                Map<UUID, Integer> usageMap = new HashMap<>();

                boolean fightIsOver = false;
                while (currentRound <= MAX_ROUNDS && !fightIsOver) {

                    // check if someone has 0 cards
                    if (deckChallenger.isEmpty()) {
                        battleLog.append("Challenger has no cards left => Opponent wins!\n\n");
                        // challenger lost
                        logic.applyElo(challenger, opponent, false);
                        result = "opponent";
                        fightIsOver = true;
                        break;
                    }
                    if (deckOpponent.isEmpty()) {
                        battleLog.append("Opponent has no cards left => Challenger wins!\n\n");
                        // challenger wins
                        logic.applyElo(challenger, opponent, true);
                        result =  "challenger";
                        fightIsOver = true;
                        break;
                    }

                    //Start new round
                    battleLog.append("\n--- Round ").append(currentRound).append(" ---\n");
                    // pick random card from each deck
                    Card cardChallanger = deckChallenger.get(rand.nextInt(deckChallenger.size()));
                    Card cardOpponent = deckOpponent.get(rand.nextInt(deckOpponent.size()));

                    battleLog.append(challenger.getUsername())
                            .append(" plays [").append(cardChallanger.getName())
                            .append(" / ").append(cardChallanger.getDamage())
                            .append(", lvl=").append(cardChallanger.getLevel()).append("]\n");

                    battleLog.append(opponent.getUsername())
                            .append(" plays [").append(cardOpponent.getName())
                            .append(" / ").append(cardOpponent.getDamage())
                            .append(", lvl=").append(cardOpponent.getLevel()).append("]\n");

                    //Increment usage for both cards and check if the card can level-up
                    boolean leveledChallanger = logic.incrementUsageAndMaybeLevelUp(cardChallanger, usageMap);
                    if (leveledChallanger) {
                        battleLog.append("Card ").append(cardChallanger.getName())
                                .append(" has been used ").append(usageMap.get(cardChallanger.getCardId()))
                                .append(" times => damage boosted by 20%! Now: ")
                                .append(cardChallanger.getDamage()).append(", level=")
                                .append(cardChallanger.getLevel()).append("\n");
                    }
                    boolean leveledOpponent = logic.incrementUsageAndMaybeLevelUp(cardOpponent, usageMap);
                    if (leveledOpponent) {
                        battleLog.append("Card ").append(cardOpponent.getName())
                                .append(" has been used ").append(usageMap.get(cardOpponent.getCardId()))
                                .append(" times => damage boosted by 20%! Now: ")
                                .append(cardOpponent.getDamage()).append(", level=")
                                .append(cardOpponent.getLevel()).append("\n");
                    }

                    //get damage done
                    // cardChallanger's effective damage vs cardOpponent
                    double firstDamage = logic.computeDamage(cardChallanger, cardOpponent);
                    // cardOpponent's effective damage vs cardChallanger
                    double secondDamage = logic.computeDamage(cardOpponent, cardChallanger);

                    battleLog.append(" => ").append(cardChallanger.getName()).append(" deals ").append(firstDamage).append(" vs. ")
                            .append(cardOpponent.getName()).append(" deals ").append(secondDamage).append("\n");

                    if (Math.abs(firstDamage - secondDamage) < 0.0001) {
                        // draw => no cards move
                        battleLog.append(" => Round is a DRAW!\n");
                    } else if (firstDamage > secondDamage) {
                        // cardChallanger wins => challenger gets cardOpponent
                        battleLog.append(" => ").append(challenger.getUsername()).append(" wins this round!\n");
                        deckOpponent.remove(cardOpponent);
                        deckChallenger.add(cardOpponent); // move cardOpponent to challenger's deck

                        // usageMap reset count for cardOpponent because new owner => usage=0
                        usageMap.put(cardOpponent.getCardId(), 0);

                        battleLog.append("Card ").append(cardOpponent.getName())
                                .append(" is now owned by the challenger!\n");
                    } else {
                        // cardOpponent wins => opponent gets cardChallanger
                        battleLog.append(" => ").append(opponent.getUsername()).append(" wins this round!\n");
                        deckChallenger.remove(cardChallanger);
                        deckOpponent.add(cardChallanger);

                        usageMap.put(cardChallanger.getCardId(), 0);

                        battleLog.append("Card ").append(cardChallanger.getName())
                                .append(" is now owned by the opponent!\n");
                    }

                    currentRound++;
                }

                // if we exit the while => 100 rounds done => draw
                if (result.equals("draw"))
                {
                    battleLog.append("\n=== 100 Rounds => BATTLE DRAW! ===\n\n");

                }

                //After the fight is over, we have to update the ownership and level of the cards
                CardRepository cardR = new CardRepository(unitOfWork);
                for (Card card : deckChallenger) {
                    cardR.updateCard(card);
                }
                for (Card card : deckOpponent) {
                    cardR.updateCard(card);
                }

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