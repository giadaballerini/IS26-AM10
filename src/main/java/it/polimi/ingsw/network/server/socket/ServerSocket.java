package it.polimi.ingsw.network.server.socket;


import it.polimi.ingsw.network.server.MatchManager;
import it.polimi.ingsw.visitors.ClientMessageVisitorImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocket {

    private final int port;
    private final MatchManager matchManager;
    public ServerSocket(int port, MatchManager matchManager) {
        this.port = port;
        this.matchManager = matchManager;
    }
    public void start(){
        try(java.net.ServerSocket server = new java.net.ServerSocket(port)) {
            ExecutorService pool = Executors.newFixedThreadPool(1000);
            while(true){
                Socket clientSocket = server.accept();

                ClientHandlerSocket handler = new ClientHandlerSocket(clientSocket,matchManager);
                ClientMessageVisitorImpl visitor = new ClientMessageVisitorImpl(matchManager, handler);
                handler.setVisitor(visitor);

                pool.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'avvio del server: " + e.getMessage());
        }
    }
}
