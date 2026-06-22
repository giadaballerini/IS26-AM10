package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract base class for server-side client handlers.
 *
 * <p>Centralizes shared state between RMI and Socket implementations:
 * <ul>
 *   <li>Player nickname</li>
 *   <li>{@link ClientMessageVisitor} for routing incoming messages</li>
 *   <li>Idempotent disconnection flag and handling</li>
 *   <li>Single-threaded executor for serialized outgoing callbacks</li>
 * </ul>
 *
 * <p>{@link #sendAsync(CheckedRunnable)} is the design's core pattern,
 * encapsulating: availability check, executor submission, and transport error handling.
 * This avoids repetition across subclass callback methods.
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #startHealthCheck()} — transport-specific ping mechanism</li>
 *   <li>{@link #handleTransportError(Exception)} — handle network errors</li>
 * </ul>
 * and may override {@link #tryMarkDisconnected()} for additional cleanup
 * (e.g., closing executors or streams).
 */
public abstract class ClientHandler implements ModelObserver {

    /** Player's nickname. */
    protected volatile String nickname;

    /** Visitor that routes incoming {@link ClientMessage} to the controller. */
    protected volatile ClientMessageVisitor visitor;

    /**
     * Indicates whether the client has been marked as disconnected.
     * Set exactly once via {@link #tryMarkDisconnected()}.
     */
    protected volatile boolean disconnected = false;

    /**
     * Single-threaded executor that serializes all outgoing callbacks,
     * ensuring order and thread safety on client writes.
     */
    protected final ExecutorService messageSender = Executors.newSingleThreadExecutor();

    /** Interval in milliseconds between consecutive health check pings. */
    protected static final int PING_INTERVAL = 2000;

    /**
     * Initializes the handler with a player nickname.
     * The nickname is immutable for the handler's lifetime.
     *
     * @param nickname the player's nickname; may be {@code null} for socket handlers
     *                 before login
     */
    protected ClientHandler(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the player's nickname.
     *
     * @return the nickname
     */
    @Override
    public final String getNickname() {
        return nickname;
    }

    /**
     * Routes an incoming message to the current visitor, if set.
     *
     * @param m the client message
     */
    @Override
    public final void onClientMessage(ClientMessage m) {
        if (visitor != null) m.accept(visitor);
    }

    /**
     * Sets the visitor for incoming message handling.
     *
     * @param visitor the visitor to set
     */
    @Override
    public final void setVisitor(ClientMessageVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Checks if the client is still reachable.
     *
     * @return {@code true} if not marked as disconnected
     */
    protected final boolean isAvailable() {
        return !disconnected;
    }

    /**
     * Marks the handler as disconnected in an idempotent manner and shuts down
     * the {@link #messageSender}.
     *
     * <p>Subclasses owning additional executors or resources must override this method,
     * call {@code super.tryMarkDisconnected()} first, and proceed only if it returns true:
     *
     * <pre>{@code
     * @Override
     * protected synchronized boolean tryMarkDisconnected() {
     *     if (!super.tryMarkDisconnected()) return false;
     *     myExecutor.shutdownNow();
     *     return true;
     * }
     * }</pre>
     *
     * @return {@code true} if this thread was first to mark disconnection;
     *         {@code false} if already disconnected
     */
    protected synchronized boolean tryMarkDisconnected() {
        if (disconnected) return false;
        disconnected = true;
        messageSender.shutdownNow();
        return true;
    }

    /**
     * Sends a callback to the client asynchronously and thread-safely.
     *
     * <p>Encapsulates the common pattern: check availability, submit to executor,
     * catch transport errors. This avoids boilerplate in every callback method.
     *
     * <p>Usage in a subclass:
     * <pre>{@code
     * public void onEvent(EventDTO e) {
     *     sendAsync(() -> clientStub.onEvent(e));  // RMI
     *     // or:
     *     sendAsync(() -> out.writeObject(new EventMessage(e)));  // Socket
     * }
     * }</pre>
     *
     * @param action the send action; may throw any {@link Exception}
     */
    protected final void sendAsync(CheckedRunnable action) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try {
                action.run();
            } catch (Exception e) {
                handleTransportError(e);
            }
        });
    }

    /**
     * Starts the transport-specific health check thread.
     *
     * <p>Called by subclass constructors after initializing network resources
     * (e.g., RMI stubs, socket streams).
     */
    protected abstract void startHealthCheck();

    /**
     * Handles a transport error during asynchronous send.
     *
     * <p>Typically marks the client as disconnected and notifies the
     * appropriate listener (e.g., {@link DisconnectionListener},
     * {@link it.polimi.ingsw.server.MatchManager}).
     *
     * @param e the exception ({@code RemoteException} for RMI, {@code IOException}
     *          for socket)
     */
    protected abstract void handleTransportError(Exception e);

    /**
     * Functional interface for actions that may throw checked exceptions.
     *
     * <p>Allows lambda expressions in {@link #sendAsync(CheckedRunnable)} without
     * wrapping checked exceptions.
     */
    @FunctionalInterface
    public interface CheckedRunnable {
        /** Executes the action.
         * @throws Exception if the action throws a checked exception
         */
        void run() throws Exception;
    }
}