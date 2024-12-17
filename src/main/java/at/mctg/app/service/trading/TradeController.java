package at.mctg.app.service.trading;

import at.mctg.app.model.TradeDeal;
import at.mctg.app.model.User;
import at.mctg.app.controller.Controller;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;


public class TradeController extends Controller {

    public TradeController() {
    }

    // GET /tradings
    public Response getAllTrades(Request request) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }

    // POST /tradings
    public Response createTrade(Request request) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }

    // DELETE /tradings/ :tradingdealid
    public Response deleteTrade(String tradeId) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }

    // POST /tradings/ :tradingdealid
    public Response executeTrade(String tradeId) {
        return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"

        );
    }
}