package at.mctg.app.service.trading;

import at.mctg.app.controller.Controller;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.CardRepository;
import at.mctg.app.dal.repository.DeckRepository;
import at.mctg.app.dal.repository.TradeRepository;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.dto.CardDTO;
import at.mctg.app.dto.TradeDTO;
import at.mctg.app.model.Card;
import at.mctg.app.model.TradeDeal;
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
public class TradeController extends Controller {

    public TradeController() {
        super();
    }

    // helper methods
    private boolean isValidTokenHeader(String header) {
        return header != null && header.startsWith("Bearer ");
    }
    private String extractToken(String header) {
        return header.substring("Bearer ".length());
    }

    // GET /tradings
    public Response getAllTrades(Request request) {

        String authHeader = request.getHeaderMap().getHeader("Authorization");

        if (!isValidTokenHeader(authHeader)) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Missing or invalid token\" }"
            );
        }
        String token = extractToken(authHeader);

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {

            User user = new UserRepository(unitOfWork).findByAuthToken(token);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"Token not assigned to user\" }"
                );
            }

            List<TradeDeal> deals = new TradeRepository(unitOfWork).findAllDeals();
            unitOfWork.commitTransaction();

            if (deals.isEmpty()) {
                return new Response(
                        HttpStatus.NO_CONTENT,
                        ContentType.JSON,
                        ""
                );
            } else {
                //Mapping of each full TradeDeal to TradeDTO
                List<TradeDTO> tradeDTOList = new ArrayList<TradeDTO>();
                for (TradeDeal trade : deals) {
                    tradeDTOList.add(new TradeDTO(
                            trade.getId(),
                            trade.getCardToTradeId(),
                            trade.getRequiredCardType(),
                            trade.getMinimumDamage()
                    ));
                }
                String tradeJSON = this.getObjectMapper().writeValueAsString(tradeDTOList);
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        tradeJSON
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

    // POST /tradings => create new Trade
    public Response createTrade(Request request) {
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (!isValidTokenHeader(authHeader)) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Missing or invalid token\" }"
            );
        }
        String token = extractToken(authHeader);

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {

            User user = new UserRepository(unitOfWork).findByAuthToken(token);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"Token not assigned to user\" }"
                );
            }

            String requestBody = request.getBody();
            // expected JSON => { "Id": "...", "CardToTrade":"...", "Type":"monster/spell", "MinimumDamage":15.0 }
            TradeDeal dealInput = this.getObjectMapper().readValue(requestBody, TradeDeal.class);

            // check if user owns that card, and card is not in deck
            Card cardToTrade = new CardRepository(unitOfWork).findByID(dealInput.getCardToTradeId());
            if (cardToTrade == null) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\":\"CardToTrade does not exist\" }"
                );
            }
            if (!user.getUsername().equals(cardToTrade.getOwnerUsername())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"You don't own this card\" }"
                );
            }

            // Checking if the card is in the deck. If so, can't trade
            Collection<UUID> deckCardIds = new DeckRepository(unitOfWork).getDeckByUser(user.getUsername());
            if (deckCardIds.contains(cardToTrade.getCardId())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"Card is in your deck; can't trade it\" }"
                );
            }

            // create tradeDeal
            dealInput.setUsername(user.getUsername()); // the user who created the deal
            //Ssafety measures
            if (dealInput.getId() == null) {
                dealInput.setId(UUID.randomUUID());
            }
            //System.out.println(dealInput.getId());
            // ensure there's no existing deal with same trade_id
            TradeDeal existing = new TradeRepository(unitOfWork).findDealById(dealInput.getId());
            if (existing != null) {
                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"message\":\"A deal with this Id already exists\" }"
                );
            }

            new TradeRepository(unitOfWork).createDeal(dealInput);
            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"message\" : \"Trading deal successfully created\" }"
            );

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

    // DELETE /tradings/ :tradingdealid
    public Response deleteTrade(Request request, String tradeId) {
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (!isValidTokenHeader(authHeader)) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Missing or invalid token\" }"
            );
        }
        String token = extractToken(authHeader);

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            User user = new UserRepository(unitOfWork).findByAuthToken(token);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"Token not assigned to user\" }"
                );
            }

            UUID tradeUUID = UUID.fromString(tradeId);
            TradeDeal deal = new TradeRepository(unitOfWork).findDealById(tradeUUID);
            if (deal == null) {
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"message\":\"Deal not found\" }"
                );
            }
            // only if user is the deal's owner
            if (!deal.getUsername().equals(user.getUsername())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"You are not the owner of this deal\" }"
                );
            }

            new TradeRepository(unitOfWork).deleteDeal(tradeUUID);
            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\":\"Trading deal successfully deleted\" }"
            );

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

    // POST /tradings/ :tradingdealid
    public Response executeTrade(Request request, String tradeId) {
        String authHeader = request.getHeaderMap().getHeader("Authorization");
        if (!isValidTokenHeader(authHeader)) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Missing or invalid token\" }"
            );
        }
        String token = extractToken(authHeader);

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {

            User user = new UserRepository(unitOfWork).findByAuthToken(token);
            if (user == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\":\"Token not assigned to user\" }"
                );
            }

            UUID tradeUUID = UUID.fromString(tradeId);
            TradeDeal deal = new TradeRepository(unitOfWork).findDealById(tradeUUID);
            if (deal == null) {
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"message\":\"Deal not found\" }"
                );
            }

            // parse the cardId the user (who's calling the deal => POST/tradings/:tradingdealid
            // offers, e.g. "\"951e886a-0fbf-425d-8df5-af2ee4830d85\""
            String requestBody = request.getBody();
            String offeredCardId = this.getObjectMapper().readValue(requestBody, String.class);
            UUID offeredUUID = UUID.fromString(offeredCardId);

            // can't trade with yourself
            if (deal.getUsername().equals(user.getUsername())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"You cannot trade with yourself.\" }"
                );
            }



            // Check if the user really owns the offered card AND
            // it is not in his deck
            Card offeredCard = new CardRepository(unitOfWork).findByID(offeredUUID);
            if (offeredCard == null) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\":\"Offered card does not exist\" }"
                );
            }

            if (!offeredCard.getOwnerUsername().equals(user.getUsername())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"You don't own this card\" }"
                );
            }

            Collection<UUID> userDeckCardsIds = new DeckRepository(unitOfWork).getDeckByUser(user.getUsername());
            if (userDeckCardsIds.contains(offeredCard.getCardId())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"Offered card is in your deck\" }"
                );
            }



            // check requirements
            // e.g. "monster" or "spell", and minimumDamage
            String dealCardType = deal.getRequiredCardType().toLowerCase();
            double dealMinDamage = deal.getMinimumDamage();

            // check offeredCard's type
            String offeredCardType = offeredCard.getCardType() != null
                    ? offeredCard.getCardType().toLowerCase()
                    : "monster";
            double offeredMinDamage = offeredCard.getDamage();

            /* I'm not sure what "unique feature" I'm going to implement, but I tend
                to a new CardType, e.g. "legendary" or "dual" or something
            if (!("monster".equals(dealCardType) || "spell".equals(dealCardType))) {

            }
            */

            // if the user says dealCardType is "monster or spell"
            boolean typeOk = offeredCardType.contains(dealCardType);
            if (!typeOk) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"Your offered card does not match required type\" }"
                );
            }

            if (offeredMinDamage < dealMinDamage) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"Your offered card damage is below required min damage\" }"
                );
            }



            // check the cardFromTradeDeal from the deal
            // the user with deal.username must really own that card
            // AND the Card must not be in the deck from the user who created the deal!
            Card cardFromTradeDeal = new CardRepository(unitOfWork).findByID(deal.getCardToTradeId());
            if (cardFromTradeDeal == null) {
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"message\":\"Deal's card doesn't exist anymore\" }"
                );
            }
            // also check if it's still owned by the original user.
            // If not, you can't trade
            if (!cardFromTradeDeal.getOwnerUsername().equals(deal.getUsername())) {
                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"message\":\"Deal's card is no longer owned by the deal creator\" }"
                );
            }

            // check if that card is in the deck. If so, you can't trade
            Collection<UUID> dealOwnerDeckCardsIds = new DeckRepository(unitOfWork).getDeckByUser(deal.getUsername());
            if (dealOwnerDeckCardsIds.contains(cardFromTradeDeal.getCardId())) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\":\"Deal's card is locked in owner's deck\" }"
                );
            }


            // If all requirements have been made, it's time
            // to execute the trade and swap Cards and ownership
            // user => gets cardFromTradeDeal
            cardFromTradeDeal.setOwnerUsername(user.getUsername());
            new CardRepository(unitOfWork).updateCard(cardFromTradeDeal);

            // original owner => gets offeredCard
            offeredCard.setOwnerUsername(deal.getUsername());
            new CardRepository(unitOfWork).updateCard(offeredCard);

            // remove the deal from trades
            new TradeRepository(unitOfWork).deleteDeal(deal.getId());

            unitOfWork.commitTransaction();

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\":\"Trade successful\" }"
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