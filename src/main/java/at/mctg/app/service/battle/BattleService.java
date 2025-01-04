package at.mctg.app.service.battle;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class BattleService implements Service {
    private final BattleController gameController;

    public BattleService() {
        this.gameController = new BattleController();
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("HANDLE REQUEST: ");
        System.out.println("TEST:" + request.getMethod());
        System.out.println("TEST:" + request.getPathname());
        if (request.getMethod() == Method.POST &&
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