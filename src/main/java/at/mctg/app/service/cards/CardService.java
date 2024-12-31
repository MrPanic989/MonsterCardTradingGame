package at.mctg.app.service.cards;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class CardService implements Service {
    private final CardController cardController;

    public CardService() {
        this.cardController = new CardController();
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("HANDLE REQUEST: ");
        System.out.println("TEST:" + request.getMethod());
        System.out.println("TEST:" + request.getPathname());
        if (request.getMethod() == Method.GET &&
                request.getPathname().equals("/cards")) {
            return cardController.getUserCards(request);
        }

        return new Response(
                HttpStatus.NOT_FOUND,
                ContentType.JSON,
                "{ \"message\" : \"Endpoint not found.\" }"
        );
    }
}