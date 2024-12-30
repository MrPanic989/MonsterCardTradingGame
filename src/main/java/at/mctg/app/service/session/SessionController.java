package at.mctg.app.service.session;

import at.mctg.app.controller.Controller;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.model.User;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;

public class SessionController extends Controller {

    public SessionController() {
        super();
    }

     // POST /sessions
     // Expects JSON: { "Username": "...", "Password": "..." }
     // If valid, returns 200 and { "token": "...-mtcgToken" }
     // else 401

    public Response loginUser(Request request) {
            UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            // Parse JSON from request body => e.g. { "Username": "kienboec", "Password": "daniel" }
            String body = request.getBody();
            User loginData = this.getObjectMapper().readValue(body, User.class);
            if (loginData.getUsername() == null || loginData.getPassword() == null) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"message\": \"Missing username or password\" }"
                );
            }

            //Check if user exists in DB
            User storedUser = new UserRepository(unitOfWork).findUserByUsername(loginData.getUsername());

            if (storedUser == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Login failed (user not found)\" }"
                );
            }

            //Compare passwords
            if (!storedUser.getPassword().equals(loginData.getPassword())) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Login failed (wrong password)\" }"
                );
            }

            //Generate token => "username-mtcgToken"
            String token = storedUser.getUsername() + "-mtcgToken";
            // store it in DB => storedUser.authtoken = token
            storedUser.setAuthtoken(token);

            // update DB
            User updatedUser = new UserRepository(unitOfWork).updateUser(storedUser);
            unitOfWork.commitTransaction();

            String userDataJSON = "{ \"token\" : \"" + updatedUser.getAuthtoken() + "\" }";
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON
            );

        }catch(Exception e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }
}
