package at.mctg.app.service.users;

import at.mctg.app.service.users.UserDummyDAL;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;
import at.mctg.app.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class UserController extends Controller {
    private UserDummyDAL userDAL;

    public UserController() {

        // Nur noch für die Dummy-JUnit-Tests notwendig. Stattdessen ein RepositoryPattern verwenden.
        this.userDAL = new UserDummyDAL();
    }

    // PUT /users/ :username
    public Response updateUsers(Request request, String username) {
        try {
            User userData = this.userDAL.getUser(username);
            User user = this.getObjectMapper().readValue(request.getBody(), User.class);
            this.userDAL.removeUser(username);
            this.userDAL.addUser(user);
            System.out.println(userData);
            // "[ { \"username\": \"John\", \"city\": \"1234\", {...} } ]"
            String UserDataJSON = this.getObjectMapper().writeValueAsString(userData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    UserDataJSON
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\" : \"User not found\" }"
            );
        }
    }


































    // DELETE /users/ :username
    public Response deleteUser(String username) {

        User userData = this.userDAL.getUser(username);

        if (userData != null) {
            this.userDAL.removeUser(username);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"message\" : \"Deleted\" }"
            );

        } else {
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\" : \"User not found\" }"
            );
        }

        /*Für später, wenn wir die Datenbank einbinden!
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"
        );
         */
    }


    // GET /users/:username
    public Response getUser(String username)
    {
        try {

            User userData = this.userDAL.getUser(username);
            // // "[ { \"username\": \"John\", \"password\": \"1234\", {...} } ]"
            String userDataJSON = this.getObjectMapper().writeValueAsString(userData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\" : \"User not found\" }"
            );
        }
    }
    /*
    // GET /users
    public Response getUser() {
        try {

            //Wir hollen unsere Daten von der DataAccessLayer
            List userData = this.userDAL.getUser();
            // // "[ { \"username\": \"John\", \"password\": \"1234\", {...} } ]"
            String UserDataJSON = this.getObjectMapper().writeValueAsString(userData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    UserDataJSON
            );
        } catch (JsonProcessingException | SQLException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    */
    // POST /users
    public Response registerUser(Request request) {
        try {

            // request.getBody() => " { \"username\": \"John\", \"password\": \"1234\", {...}  }
            User user = this.getObjectMapper().readValue(request.getBody(), User.class);
            if(userDAL.getUser(user.getUsername()) != null) {
                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"message\" : \"User with same username already registered\" }"
                );
            }

            userDAL.addUser(user);
            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ message: \"User successfully created\" }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"message\" : \"Invalid user data\" }"
            );
        }
    }
}