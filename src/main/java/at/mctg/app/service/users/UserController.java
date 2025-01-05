package at.mctg.app.service.users;

import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.dto.UserDTO;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;
import at.mctg.app.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserController extends Controller {
    //For later: refactor the code to get it to use the interface
    //private RepositoryInterface repository = new UserRepository(new UnitOfWork());

    public UserController() {
        super(); // calls Controller's constructor -> sets objectMapper
    }

    // PUT /users/ :username
    public Response updateUsers(Request request, String username) {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            // Token
            String authHeader = request.getHeaderMap().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Missing or invalid token\" }"
                );
            }
            String token = authHeader.substring("Bearer ".length());

            // CurrentUser
            User currentUser = new UserRepository(unitOfWork).findByAuthToken(token);
            if (currentUser == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Token not assigned to any user\" }"
                );
            }

            // Check for admin or correct user
            if (!currentUser.isAdmin() &&
                    !currentUser.getUsername().equals(username))
            {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\" : \"You cannot update someone else's data\" }"
                );
            }

            //If everything is ok:
            //Retrive existing User via username
            User existingUser = new UserRepository(unitOfWork).findUserByUsername(username);
            if (existingUser == null) {
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"message\" : \"User not found\" }"
                );
            }
            // Overwrite just the fields "Name", "Bio", "Image"
            // everything else from the currentUser stays the same
            String requestBody = request.getBody();
            User tempUser = this.getObjectMapper().readValue(requestBody, User.class);

            existingUser.setName(tempUser.getName());
            existingUser.setBio(tempUser.getBio());
            existingUser.setImage(tempUser.getImage());

            User updatedUser = new UserRepository(unitOfWork).updateUser(existingUser);
            unitOfWork.commitTransaction();

            if (updatedUser != null) {
                String updatedUserJSON = this.getObjectMapper().writeValueAsString(updatedUser);
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        updatedUserJSON
                );
            } else {
                return new Response(
                        HttpStatus.NO_CONTENT,
                        ContentType.JSON,
                        "{ \"message\" : \"User not found\" }"
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

    // DELETE /users/ :username
    public Response deleteUser(String username) {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            new UserRepository(unitOfWork).deleteUser(username);
            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ message: \"Deleted\" }"
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

    // GET /users/:username
    public Response getUser(Request request, String username)
    {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork){
            //Get token through header
            String authHeader = request.getHeaderMap().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Missing or invalid token\" }"
                );
            }
            String token = authHeader.substring("Bearer ".length());

            //Extract the curren user
            User userData = new UserRepository(unitOfWork).findByAuthToken(token);
            if (userData == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"message\" : \"Token not assigned to any user\" }"
                );
            }

            //Check if admin or correct user
            if (!userData.isAdmin() &&
                    !userData.getUsername().equals(username))
            {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"message\" : \"You are not allowed to see this user's data\" }"
                );
            }

            unitOfWork.commitTransaction();

            if(userData != null) {
                //Convert full User to UserDTO
                UserDTO dto = new UserDTO(
                        userData.getName(),
                        userData.getBio(),
                        userData.getImage()
                );
                String userDataJSON = this.getObjectMapper().writeValueAsString(dto);
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        userDataJSON
                );
            } else {
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"message\" : \"User with that username not found\" }"
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

    // GET /users
    // Array of {"Name", "Bio", "Image" } dto objects for all users
    public Response getUser() {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork){
            Collection<User> userData = new UserRepository(unitOfWork).findAllUsers();
            unitOfWork.commitTransaction();

            //Mapping of each full User to UserDTO
            List<UserDTO> userDTOList = new ArrayList<UserDTO>();
            for (User user : userData) {
                userDTOList.add(new UserDTO(user.getName(), user.getBio(), user.getImage()));
            }
            String userDataJSON = this.getObjectMapper().writeValueAsString(userDTOList);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    userDataJSON
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

    // POST /users
    public Response registerUser(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork){
            // request.getBody() => " { \"username\": \"John\", \"password\": \"1234\", {...}  }
            // JSON aus dem Request Body holen
            String requestBody = request.getBody();
            // JSON in User-Objekt umwandeln
            User userInput = this.getObjectMapper().readValue(requestBody, User.class);
            User userToCheck = new UserRepository(new UnitOfWork()).findUserByUsername(userInput.getUsername());
            if(userInput.getUsername() != null && userToCheck == null) {
                // Speichern über Repository
                User savedUser = new UserRepository(unitOfWork).insertUser(userInput);
                unitOfWork.commitTransaction();

                //Gespeichertes Objekt als JSON zurückgeben
                String savedUserJSON = this.getObjectMapper().writeValueAsString(savedUser);
                return new Response(
                        HttpStatus.CREATED,
                        ContentType.JSON,
                        "{ message: \"User successfully created\" }"
                );
            }
            else {
                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"message\" : \"User with same username already registered\" }"
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