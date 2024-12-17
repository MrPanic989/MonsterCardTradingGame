package at.mctg.app.service.session;

import at.mctg.app.service.users.UserDummyDAL;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.app.controller.Controller;
import at.mctg.app.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.Collection;
import java.util.List;

/* Die Logik funktioniert noch nicht richtig, da
/ doppelte UserDummyDAL vorhanden ist! Ich habe es
/ versucht wie der Kollege, der letztens in der Vorlesung
/ sein Projekt präsentiert hat, UserDummyDAL einmal in der
/ UserDummyDAL.java statisch zu machen, doch ich habe es schließlich
/ nicht geschafft. Das Problem sollte leicht zu lösen sein, wenn
/ wir einmal eine richtige Datenbank implementieren.
*/
public class SessionController extends Controller {
    private UserDummyDAL userDAL;

    public SessionController() {

        // Nur noch für die Dummy-JUnit-Tests notwendig. Stattdessen ein RepositoryPattern verwenden.
        this.userDAL = new UserDummyDAL();
    }

    // POST /session
    public Response loginUser(Request request) {
        try {

            // request.getBody() => "{ \"username\": \"John\", \"password\": \"12334\", ... }
            User user = this.getObjectMapper().readValue(request.getBody(), User.class);
            User storedUser = userDAL.getUser(user.getUsername());

            // Sind die Login-Daten ident wird ein Tokern erstellt
            if(storedUser != null && storedUser.getPassword().equals(user.getPassword())) {
                String token = user.getUsername() + "-mtcgToken";
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"token\":  \""+ token +"\" }"
                );
            } else {
                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"message\" : \"Login failed\" }"
                );
            }

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
