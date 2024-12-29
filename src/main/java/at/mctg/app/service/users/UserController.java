package at.mctg.app.service.users;

import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dal.repository.RepositoryInterface;
import at.mctg.app.dal.repository.UserRepository;
import at.mctg.app.dto.UserDTO;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;
import at.mctg.app.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserController extends Controller {
    //For later: refactor the code to get it to use the interface
    //private RepositoryInterface repository = new UserRepository(new UnitOfWork());

    public UserController() {
        super();
    }

    // PUT /users/ :username
    public Response updateUsers(Request request, String username) {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork) {
            String requestBody = request.getBody();
            User userInput = this.getObjectMapper().readValue(requestBody, User.class);

            // ID aus der URL übernehmen, da PUT meist ein bestimmtes Objekt updated
            userInput.setUsername(username);

            User updatedUser = new UserRepository(unitOfWork).update(userInput);
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
            new UserRepository(unitOfWork).delete(username);
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
    public Response getUser(String username)
    {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork){
            User userData = new UserRepository(unitOfWork).findByID(username);

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
            Collection<User> userData = new UserRepository(unitOfWork).findAll();
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
    /*
    Man könnte auch die Funktion benutzen, ich möchte es lieber zuerst mit den gegebenen
    Funktionalitäten versuchen
    public boolean userExists(String username) {
        try (PreparedStatement ps = unitOfWork.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username = ?"
        )) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("User existence check failed", e);
        }
        return false;
    }
    */
    public Response registerUser(Request request) {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork){
            // request.getBody() => " { \"username\": \"John\", \"password\": \"1234\", {...}  }
            // JSON aus dem Request Body holen
            String requestBody = request.getBody();
            // JSON in User-Objekt umwandeln
            User userInput = this.getObjectMapper().readValue(requestBody, User.class);
            System.out.println("userInput: " + userInput.toString());
            User userToCheck = new UserRepository(new UnitOfWork()).findByID(userInput.getUsername());
            if(userInput.getUsername() != null && userToCheck == null) {
                // Speichern über Repository
                User savedUser = new UserRepository(unitOfWork).save(userInput);
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