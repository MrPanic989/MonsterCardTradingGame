package at.mctg.app.service.game;

import at.mctg.app.service.game.GameController;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class GameService implements Service {
    private final GameController gameController;

    public GameService() {
        this.gameController = new GameController();
    }

    @Override
    public Response handleRequest(Request request) {

        if (request.getMethod() == Method.GET &&
                request.getPathname().equals("/stats")) {
            return gameController.getUserStats(request);
        } else if (request.getMethod() == Method.GET &&
                request.getPathname().equals("/scoreboard")) {
            return gameController.getScoreboard(request);
        } else if (request.getMethod() == Method.POST &&
                request.getPathname().equals("/battles")) {
            return gameController.startBattle(request);
        }
        return new Response(
                HttpStatus.NOT_FOUND,
                ContentType.JSON,
                "{ \"message\" : \"Endpoint not found.\" }"
        );
    }
}