package at.mctg.app.service.cards;

import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.CardRepository;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.dto.CardDTO;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;
import at.mctg.app.model.Card;
import at.mctg.app.model.User;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CardController extends Controller {

    public CardController() {
        super(); //callse Controller's Consturctor and sets objectMapper
    }

    // GET /cards
    public Response getUserCards(Request request) {
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Missing or invalid token\" }"
            );
        }
        String realToken = authHeader.substring("Bearer ".length()); // e.g. "admin-mtcgToken"

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            // Check if token belongs to a valid user
            User user = new UserRepository(unitOfWork).findByAuthToken(realToken);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Invalid token (no matching user)\" }"
                );
            }

            //Create a Collection of User's Cards
            Collection<Card> userCards = new CardRepository(unitOfWork).findCardsByOwner(user.getUsername());
            unitOfWork.commitTransaction();

            if (userCards.isEmpty()) {
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{}"
                );
            } else {
                //Mapping of each full Card to CardDTO
                List<CardDTO> cardDTOList = new ArrayList<CardDTO>();
                for (Card card : userCards) {
                    cardDTOList.add(new CardDTO(card.getCardId(), card.getName(), card.getDamage()));
                }
                String cardsJSON = this.getObjectMapper().writeValueAsString(cardDTOList);
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        cardsJSON
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }
}
