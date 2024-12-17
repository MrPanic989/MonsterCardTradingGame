package at.mctg.app.service.trading;

import at.mctg.app.service.trading.TradeController;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class TradeService implements Service {
    private final TradeController tradeController;

    public TradeService() {
        this.tradeController = new TradeController();
    }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.GET &&
                request.getPathname().equals("/tradings")) {
            return tradeController.getAllTrades(request);
        } else if (request.getMethod() == Method.POST &&
                request.getPathname().equals("/tradings")) {
            return tradeController.createTrade(request);
        } else if (request.getMethod() == Method.DELETE) {

            return this.tradeController.deleteTrade(request.getPathParts().get(1));

        } else if (request.getMethod() == Method.POST &&
                request.getPathParts().size() > 1) {

            return this.tradeController.executeTrade(request.getPathParts().get(1));
        }
            return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"message\" : \"Endpoint not found.\" }"
            );

    }
}