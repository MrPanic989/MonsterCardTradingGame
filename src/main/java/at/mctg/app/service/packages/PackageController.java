package at.mctg.app.service.packages;

import at.mctg.app.controller.Controller;
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
import com.fasterxml.jackson.core.type.TypeReference;   //For correct parsing of generics, e.g. List<Card>

import java.util.List;
import java.util.UUID;

public class PackageController extends Controller {

    public PackageController() {
        super(); // calls Controller's constructor -> sets objectMapper
    }

    //POST /packages
    //Expects JSON array of 5 Card objects:
    //[{ "Id":"...", "Name":"...", "Damage": ...}, {...}, ...]

    public Response createPackage(Request request) {
        // Only admin user can create packages => check token => check if user.admin == true.
        // Check Authorization header => Bearer <token>
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"message\" : \"Missing or invalid token\" }"
            );
        }
        String realToken = token.substring("Bearer ".length()); // e.g. "admin-mtcgToken"

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

            //Check if user is admin
            if (!user.isAdmin()) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\" : \"Only admin can create packages\" }"
                );
            }

            //Parse JSON array of 5 cards from request body
            String requestBody = request.getBody();
            //We expect a List<Card> from JSON, e.g.
            //[ { "Id":"...", "Name":"...", "Damage":... }, {...}, ... ]
            List<Card> cardList = this.getObjectMapper().readValue(requestBody,
                    new TypeReference<List<Card>>() {}
            );

            if (cardList.size() != 5) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\" : \"Package must contain exactly 5 cards.\" }"
                );
            }

            //Create a new Package in DB => purchased = false
            CardPackage newPackage = new CardPackage();
            newPackage.setPackageId(UUID.randomUUID());
            newPackage.setPurchased(false);
            newPackage.setCards(cardList); // in memory

            new PackageRepository(unitOfWork).createPackage(newPackage);

            //Insert the 5 cards => each card gets package_id = newPackage.packageId
            CardRepository cardRepo = new CardRepository(unitOfWork);
            for (Card c : cardList) {
                if (c.getCardId() == null) {
                    // generate a random ID if not provided
                    c.setCardId(UUID.randomUUID());
                }
                // set the packageId
                c.setPackageId(newPackage.getPackageId());
                // do an insert into "cards" table
                cardRepo.insertCard(c);
            }
            unitOfWork.commitTransaction();

            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"message\" : \"Package successfully created\" }"
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
