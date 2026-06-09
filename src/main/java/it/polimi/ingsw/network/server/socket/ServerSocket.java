package it.polimi.ingsw.network.server.socket;

import it.polimi.ingsw.network.server.MatchManager;
import it.polimi.ingsw.visitors.ClientMessageVisitorImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP socket listener that accepts incoming client connections.
 *
 * <p>Binds to the configured port and, for each accepted connection, creates
 * a {@link ClientHandlerSocket}, assigns it a fresh
 * {@link ClientMessageVisitorImpl}, and submits it to a fixed thread pool for
 * concurrent execution.
 *
 * <p>The thread pool is capped at 1000 threads to bound resource usage while
 * still supporting a large number of simultaneous connections.
 */
public class ServerSocket {

    /** Port on which the server listens for incoming connections. */
    private final int port;

    /** Shared match coordinator passed to each new {@link ClientHandlerSocket}. */
    private final MatchManager matchManager;

    /**
     * Creates a {@code ServerSocket} that will accept connections on the given
     * port and route them through the given match manager.
     *
     * @param port         TCP port to listen on
     * @param matchManager the shared match coordinator
     */
    public ServerSocket(int port, MatchManager matchManager) {
        this.port = port;
        this.matchManager = matchManager;
    }

    /**
     * Starts the acceptance loop.
     *
     * <p>Opens a {@link java.net.ServerSocket} on {@link #port} and enters an
     * infinite loop that blocks on {@code accept()}. For each new connection a
     * {@link ClientHandlerSocket} is created, its visitor is set, and it is
     * submitted to the thread pool.
     *
     * <p>If the server socket fails to bind or encounters a fatal I/O error,
     * the error is logged to stderr and the method returns.
     */
    public void start() {
        try (java.net.ServerSocket server = new java.net.ServerSocket(port)) {
            ExecutorService pool = Executors.newFixedThreadPool(1000);
            while (true) {
                Socket clientSocket = server.accept();
                ClientHandlerSocket handler = new ClientHandlerSocket(clientSocket, matchManager);
                ClientMessageVisitorImpl visitor = new ClientMessageVisitorImpl(matchManager, handler);
                handler.setVisitor(visitor);
                pool.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'avvio del server: " + e.getMessage());
        }
    }
}