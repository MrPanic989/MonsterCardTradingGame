package at.mctg.app.service.deck;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class DeckService implements Service {
    private final DeckController deckController;

    public DeckService() {
        this.deckController = new DeckController();
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("HANDLE REQUEST: ");
        System.out.println("TEST:" + request.getMethod());
        System.out.println("TEST:" + request.getPathname());
        if (request.getMethod() == Method.GET) {
            return this.deckController.getUserDeck(request);
        } else if (request.getMethod() == Method.PUT) {
            return this.deckController.configureUserDeck(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
