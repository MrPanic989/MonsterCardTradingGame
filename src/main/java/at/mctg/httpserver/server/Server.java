package at.mctg.httpserver.server;

import at.mctg.httpserver.utils.RequestHandler;
import at.mctg.httpserver.utils.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private Router router;

    public Server(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws IOException {
        //Option auf Multi-threading
        final ExecutorService executorService = Executors.newFixedThreadPool(10);

        System.out.println("Start http-server...");
        System.out.println("http-server running at: http://localhost:" + this.port);

        //Dann öffnen wir einen ServerSocket auf den übergebenen Port
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            while(true) {
                //Hier blockiert das Programm bis etwas ankommt
                final Socket clientConnection = serverSocket.accept();
                //Dann erstellen wir einen RequestHandler, welcher die Anfrage richtig weiterleitet
                final RequestHandler socketHandler = new RequestHandler(clientConnection, this.router);
                executorService.submit(socketHandler);  //Multi-Threading
                //socketHandler.run();      //Single-Threading
            }
        }
    }
}
