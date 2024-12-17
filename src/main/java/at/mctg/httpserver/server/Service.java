package at.mctg.httpserver.server;

public interface Service {
    Response handleRequest(Request request);
}
