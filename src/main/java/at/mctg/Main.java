package at.mctg;

import at.mctg.app.service.cards.CardService;
import at.mctg.app.service.game.GameService;
import at.mctg.app.service.packages.PackageService;
import at.mctg.app.service.packages.TransactionService;
import at.mctg.app.service.trading.TradeService;
import at.mctg.httpserver.server.Server;
import at.mctg.httpserver.utils.Router;

import at.mctg.app.service.users.UserService;
import at.mctg.app.service.session.SessionService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, configureRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter()
    {
        Router router = new Router();

        router.addService("/users", new UserService());
        router.addService("/sessions", new SessionService());
        router.addService("/packages", new PackageService());
        router.addService("/transactions/packages", new TransactionService());
        router.addService("/cards", new CardService());
        router.addService("/deck", new CardService());
        router.addService("/stats", new GameService());
        router.addService("/scoreboard", new GameService());
        router.addService("/battles", new GameService());
        router.addService("/tradings", new TradeService());

        return router;
    }
}
