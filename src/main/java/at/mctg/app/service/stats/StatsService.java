package at.mctg.app.service.stats;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class StatsService implements Service {
    private final StatsController statsController;

    public StatsService() {
        this.statsController = new StatsController();
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("HANDLE REQUEST: ");
        System.out.println("TEST:" + request.getMethod());
        System.out.println("TEST:" + request.getPathname());
        if (request.getMethod() == Method.GET &&
                request.getPathname().equals("/stats")) {
            return this.statsController.getUserStats(request);
        } else if (request.getMethod() == Method.GET &&
                request.getPathname().equals("/scoreboard")) {
            return this.statsController.getScoreboard(request);
        }
        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
