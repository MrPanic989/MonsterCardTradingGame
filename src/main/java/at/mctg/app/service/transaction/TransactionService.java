package at.mctg.app.service.transaction;

import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class TransactionService implements Service {
    private final TransactionController transactionController;
    public TransactionService() { this.transactionController = new TransactionController(); }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("HANDLE REQUEST: ");
        System.out.println("TEST:" + request.getMethod());
        System.out.println("TEST:" + request.getPathname());
        String path = request.getPathname();
        if (request.getMethod() == Method.POST &&
                "/transactions/packages".equals(path)) {
            return transactionController.buyPackage(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}
