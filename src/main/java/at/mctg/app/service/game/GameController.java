package at.mctg.app.service.game;

import at.mctg.app.model.User;
import at.mctg.app.controller.Controller;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;


public class GameController extends Controller {

    public GameController() {}

        // GET /stats

    public Response getUserStats(Request request) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }

    // GET /scoreboard
    public Response getScoreboard(Request request) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }

    // POST /battles
    public Response startBattle(Request request) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }
}