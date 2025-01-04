package at.mctg.app.service.deck;

import at.mctg.app.controller.Controller;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.CardRepository;
import at.mctg.app.dal.repository.DeckRepository;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.dto.CardDTO;
import at.mctg.app.model.Card;
import at.mctg.app.model.User;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DeckController extends Controller {

    public DeckController() {
        super(); // calls Controller's constructor and sets objectMapper
    }

    //GET /deck
    public Response getUserDeck(Request request) {
        // Token-check
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\":\"Invalid token\" }"
            );
        }
        String token = authHeader.substring("Bearer ".length());



        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            User user = new UserRepository(unitOfWork).findByAuthToken(token);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"User not found for token\" }"
                );
            }

            //Load a deck (card_ids) from DB
            Collection<UUID> deckCardIds = new DeckRepository(unitOfWork).getDeckByUser(user.getUsername());
            if (deckCardIds.isEmpty()) {
                return new Response(
                        HttpStatus.NO_CONTENT,
                        ContentType.JSON,
                        ""
                );
            }

            // Load the actual Card objects and transform to CardDTOs
            Collection<Card> deckCards = new ArrayList<>();
            for (UUID cardId : deckCardIds) {
                Card currentCard = new CardRepository(unitOfWork).findByID(cardId);
                if (currentCard != null) {
                    deckCards.add(currentCard);
                }
            }

            unitOfWork.commitTransaction();

            // check query param if it contains something like e.g. "format=plain"
            String queryParam = request.getParams();
            if (queryParam != null && queryParam.contains("format=plain")) {
                // return text representation
                //StringBuilder is a efficient way of building Strings, especially
                //for loops
                StringBuilder sb = new StringBuilder();
                for (Card c : deckCards) {
                    sb.append(String.format(
                            "Card: %s (Damage: %.1f) \n",
                            c.getName(), c.getDamage()
                    ));
                }
                return new Response(
                        HttpStatus.OK,
                        ContentType.PLAIN_TEXT,
                        sb.toString()
                );
            } else {
                // Out default output: JSON
                Collection<CardDTO> dtos = new ArrayList<>();
                for (Card card : deckCards) {
                    CardDTO dto = new CardDTO(
                            card.getCardId(),
                            card.getName(),
                            card.getDamage()
                    );
                    dtos.add(dto);
                }
                String cardJSON = this.getObjectMapper().writeValueAsString(dtos);
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        cardJSON
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\":\"Internal Server Error\" }"
            );
        }
    }

    //PUT /deck
     //Expects JSON array of 4 Card IDs => e.g. ["845f0dc7-37d0-426e-994e-43fc3ac83c08", ... ]
    public Response configureUserDeck(Request request) {
        // token
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\":\"Invalid token\" }"
            );
        }
        String token = authHeader.substring("Bearer ".length());

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            User user = new UserRepository(unitOfWork).findByAuthToken(token);

            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"User not found for token\" }"
                );
            }

            // parsing 4 card IDs from the request body
            String requestBody = request.getBody();
            List<UUID> cardIds = this.getObjectMapper().readValue(requestBody, new TypeReference<List<UUID>>() {});
            if (cardIds.size() != 4) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\":\"You must provide exactly 4 card IDs\" }"
                );
            }

            // checking if the card_id exists and the user owns these cards
            for (UUID cId : cardIds) {
                Card currentCard = new CardRepository(unitOfWork).findByID(cId);
                if (currentCard == null) {
                    return new Response(
                            HttpStatus.BAD_REQUEST,
                            ContentType.JSON,
                            "{ \"message\":\"One of the given card IDs doesn't exist\" }"
                    );
                }

                if (!user.getUsername().equals(currentCard.getOwnerUsername())) {
                    return new Response(
                            HttpStatus.FORBIDDEN,
                            ContentType.JSON,
                            "{ \"message\":\"You don't own this card: " + cId + "\" }"
                    );
                }
            }

            // Removing the old cards and Overwriting the  deck
            DeckRepository deckRepo = new DeckRepository(unitOfWork);
            deckRepo.clearDeck(user.getUsername());
            for (UUID cId : cardIds) {
                deckRepo.addCardToDeck(user.getUsername(), cId);
            }

            unitOfWork.commitTransaction();

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\":\"Deck updated successfully\" }"
            );

        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\":\"Internal Server Error\" }"
            );
        }
    }
}
