package at.mctg.app.service.transaction;

import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.CardRepository;
import at.mctg.app.dal.repository.PackageRepository;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.model.Card;
import at.mctg.app.model.CardPackage;
import at.mctg.app.model.User;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;

import java.util.Collection;

public class TransactionController extends Controller {
    public TransactionController() {
        super();
    }

    // POST /transactions/packages
    public Response buyPackage(Request request) {
        //Check for tokern. If a token was sended, extract the User from it.
        String tokenHeader = request.getHeaderMap().getHeader("Authorization");
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\": \"Missing or invalid token\" }"
            );
        }
        String token = tokenHeader.substring("Bearer ".length());  // e.g. "kienboec-mtcgToken"

        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            User userFromToken = new UserRepository(unitOfWork).findByAuthToken(token);
            if (userFromToken == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\": \"Invalid token (no matching user)\" }"
                );
            }

            //Check if user has >= 5 cons
            if (userFromToken.getCoins() < 5) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\": \"Not enough coins to buy a package\" }"
                );
            }

            // Find an awailable (unpurchased) package
            CardPackage pack = new PackageRepository(unitOfWork).findAnyUnpurchasedPackage();
            if (pack == null) {
                // no unpurchased package left
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\": \"No packages available\" }"
                );
            }

            //We have an unpurchased package => now get its cards
            //and assign those cards to the user (owner = user's username)
            Collection<Card> cardsInPackage = new CardRepository(unitOfWork).findCardsByPackageId(pack.getPackageId());
            for (Card c : cardsInPackage) {
                c.setOwnerUsername(userFromToken.getUsername());
                c.setPackageId(null);     // "detach" from package
                new CardRepository(unitOfWork).updateCard(c);
            }

            //Now we have to  reduce user coins by 5
            userFromToken.setCoins(userFromToken.getCoins() - 5);
            new UserRepository(unitOfWork).updateUser(userFromToken);

            //Set package to "purchased"
            new PackageRepository(unitOfWork).markPackagePurchased(pack.getPackageId());

            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"message\": \"Package bought successfully\" }"
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
}
