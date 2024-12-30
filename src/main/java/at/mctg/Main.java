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

        router.addService("/users", new UserService());         // Register, GET user, update user
        router.addService("/sessions", new SessionService());   // POST /sessions => login
        router.addService("/packages", new PackageService());   // Admin: create packages
        router.addService("/transactions/packages", new TransactionService()); // buy packages
        router.addService("/cards", new CardService());         // GET /cards => list all user's cards
        //router.addService("/deck", new DeckService());          // GET /deck, PUT /deck
        //router.addService("/stats", new StatsService());        // GET /stats
        //router.addService("/scoreboard", new StatsService());   // GET /scoreboard
        //router.addService("/battles", new BattleService());     // POST /battles
        router.addService("/tradings", new TradeService());

        return router;
    }
}
