package at.mctg.app.service.users;

import at.mctg.app.service.users.UserController;
import at.mctg.httpserver.http.ContentType;
import at.mctg.httpserver.http.HttpStatus;
import at.mctg.httpserver.http.Method;
import at.mctg.httpserver.server.Request;
import at.mctg.httpserver.server.Response;
import at.mctg.httpserver.server.Service;

public class UserService implements Service {
    private final UserController userController;

    public UserService() {
        this.userController = new UserController();
    }

    @Override
    public Response handleRequest(Request request) {
        System.out.println("HANDLE REQUEST: ");
        System.out.println("TEST:" + request.getMethod());
        System.out.println("TEST:" + request.getPathname());
        if (request.getMethod() == Method.GET &&
                request.getPathParts().size() > 1) {
            return this.userController.getUser(request.getPathParts().get(1));
        } else if (request.getMethod() == Method.GET) {
            return this.userController.getUser();
        } else if (request.getMethod() == Method.POST &&
                request.getPathname().equals("/users")) {
            return this.userController.registerUser(request);
        } else if (request.getMethod() == Method.PUT &&
                request.getPathParts().size() > 1) {
            return this.userController.updateUsers(request, request.getPathParts().get(1));
        } else if (request.getMethod() == Method.DELETE) {
            return this.userController.deleteUser(request.getPathParts().get(1));
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[]"
        );
    }
}