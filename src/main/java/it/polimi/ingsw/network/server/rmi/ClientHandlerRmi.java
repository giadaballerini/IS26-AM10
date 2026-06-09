package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Server-side RMI representative for a single connected client.
 *
 * <p>Wraps the client's {@link VirtualViewRmi} stub and serializes all
 * outbound callbacks through a single-threaded {@link #messageSender}
 * executor, preventing concurrent RMI calls to the same stub.
 *
 * <p>A background health check thread periodically pings the client via
 * {@link VirtualViewRmi#ping()}. After {@link #MAX_FAILURES} consecutive
 * failures the handler marks the client as disconnected and notifies the
 * {@link DisconnectionListener}.
 */
public class ClientHandlerRmi extends ClientHandler {

    /** The player's nickname. */
    private final String nickname;

    /** RMI stub used to push callbacks to the remote client. */
    private final VirtualViewRmi clientStub;

    /** Visitor that dispatches incoming {@link ClientMessage} objects to the controller. */
    private volatile ClientMessageVisitor visitor;

    /** Listener notified when the client disconnects or becomes unreachable. */
    private final DisconnectionListener disconnectionListener;

    /** Whether this handler has already been marked as disconnected. */
    private volatile boolean disconnected = false;

    /**
     * Single-threaded executor that serializes all outbound RMI callbacks,
     * ensuring they are delivered in order without concurrent invocations.
     */
    private final ExecutorService messageSender = Executors.newSingleThreadExecutor();

    /**
     * Cached thread-pool executor used to time-box individual ping calls so
     * that a hanging RMI call does not block the health check thread
     * indefinitely.
     */
    private final ExecutorService pingExecutor = Executors.newCachedThreadPool();

    /** Interval in milliseconds between consecutive health check pings. */
    private static final int PING_INTERVAL = 2000;

    /** Maximum time in milliseconds to wait for a ping response before counting a failure. */
    private static final int PING_TIMEOUT = 3000;

    /** Number of consecutive ping failures that trigger a disconnection. */
    private static final int MAX_FAILURES = 2;

    /**
     * Creates a new handler, binds it to the given client stub, and starts
     * the health check thread.
     *
     * @param nickname              the player's nickname
     * @param clientStub            the client's RMI callback stub
     * @param disconnectionListener listener to notify on disconnection
     */
    public ClientHandlerRmi(String nickname, VirtualViewRmi clientStub,
                            DisconnectionListener disconnectionListener) {
        this.nickname = nickname;
        this.clientStub = clientStub;
        this.visitor = null;
        this.disconnectionListener = disconnectionListener;
        startHealthCheck();
    }

    /**
     * Starts a daemon thread that periodically pings the client.
     *
     * <p>Each ping is submitted to {@link #pingExecutor} and awaited with a
     * {@link #PING_TIMEOUT} deadline. A {@link TimeoutException} or
     * {@link ExecutionException} increments the failure counter; once
     * {@link #MAX_FAILURES} is reached, the client is considered disconnected.
     */
    private void startHealthCheck() {
        Thread healthCheckThread = new Thread(() -> {
            int failureCount = 0;
            while (!disconnected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    Future<Void> future = pingExecutor.submit(() -> {
                        clientStub.ping();
                        return null;
                    });
                    try {
                        future.get(PING_TIMEOUT, TimeUnit.MILLISECONDS);
                        failureCount = 0;
                        System.out.println("[HEALTH CHECK OK] " + nickname);
                    } catch (TimeoutException e) {
                        future.cancel(true);
                        System.out.println("[HEALTH CHECK FALLITO] " + nickname
                                + " (tentativo " + (++failureCount) + "/" + MAX_FAILURES + ")");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (ExecutionException e) {
                        System.out.println("[HEALTH CHECK FALLITO] " + nickname
                                + " (tentativo " + (++failureCount) + "/" + MAX_FAILURES + ")");
                    }

                    if (failureCount >= MAX_FAILURES) {
                        synchronized (ClientHandlerRmi.this) {
                            if (disconnected) break;
                            disconnected = true;
                            pingExecutor.shutdownNow();
                            messageSender.shutdownNow();
                        }
                        handleDisconnection();
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            pingExecutor.shutdownNow();
        }, "HealthCheck-" + nickname);

        healthCheckThread.setDaemon(true);
        healthCheckThread.start();
    }

    /**
     * Notifies the {@link DisconnectionListener} that this client has
     * disconnected.
     */
    private void handleDisconnection() {
        disconnectionListener.handleDisconnection(nickname);
    }

    /**
     * Handles an unexpected {@link RemoteException} from an outbound callback.
     *
     * <p>Marks the client as disconnected, shuts down both executors, and
     * notifies the {@link DisconnectionListener}. Idempotent: safe to call
     * multiple times concurrently.
     *
     * @param e the exception that was thrown
     */
    private void handleRemoteException(RemoteException e) {
        if (disconnected) return;
        synchronized (this) {
            if (disconnected) return;
            disconnected = true;
            pingExecutor.shutdownNow();
            messageSender.shutdownNow();
        }
        System.err.println("[RMI] Client " + nickname + " non raggiungibile: " + e.getMessage());
        disconnectionListener.handleDisconnection(nickname);
    }

    /**
     * Returns whether this handler is still connected.
     *
     * @return {@code true} if the client has not yet been marked as disconnected
     */
    private boolean isAvailable() {
        return !disconnected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNickname() {
        return nickname;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Dispatches the message to the current {@link #visitor} if one is set.
     */
    @Override
    public void onClientMessage(ClientMessage m) {
        if (visitor != null) m.accept(visitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisitor(ClientMessageVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Submits the error callback to {@link #messageSender} to preserve
     * delivery order.
     */
    @Override
    public void onErrorMessage(String errorMsg) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.printError(errorMsg); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReconnection(int matchId) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.reconnect(matchId); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCurrPlayerUpdate(String nickname) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onCurrPlayerUpdate(nickname); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onRequestLeaderboard(ranks); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onGameEnding(stats, rankingPos, globalRankingPos); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onMoveUpdate(TileDTO tile, String nextPlayer) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onMoveUpdate(tile, nextPlayer); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onReturnToQueue(tileDTO, playerStatsDTO); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onDrawUpdate(c, nickname); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onStatusUpdate(status); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onStatsUpdate(stats, cardId); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onPhaseUpdate(phaseDTO); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showBoard(BoardDTO board) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.showBoard(board); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void notifyDrawable(ActionsDTO actions) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.notifyDrawable(actions); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void notifySkip(String nickname) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.notifySkip(nickname); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onChangeAge(ChangeAgeDTO dto) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onChangeAge(dto); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(EventDTO events) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onEvent(events); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onQuitServer(String reason) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onQuitServer(reason); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }
}