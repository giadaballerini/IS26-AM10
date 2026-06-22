package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.network.server.DisconnectionListener;
import it.polimi.ingsw.visitors.ClientMessageVisitor;
import it.polimi.ingsw.visitors.GameMessageVisitor;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server-side RMI handler for a connected client.
 *
 * <p>Wraps the client's {@link VirtualViewRmi} stub and serializes all outgoing
 * callbacks through the inherited {@code messageSender} executor, preventing
 * concurrent RMI invocations on the same stub.
 *
 * <p>A background health check thread periodically pings the client via
 * {@link VirtualViewRmi#ping()}. After {@link #MAX_FAILURES} consecutive failures,
 * the handler marks the client as disconnected and notifies the
 * {@link DisconnectionListener}.
 *
 * <h2>Design</h2>
 * <p>Delegates to {@link ClientHandler#sendAsync(CheckedRunnable)} for each
 * outgoing callback, reducing every {@code onXxx()} method to a single line.
 * Idempotent disconnection and base health check logic are in the base class;
 * this class adds only RMI-specific behavior (timeout-guarded ping, remote exceptions).
 */
public class ClientHandlerRmi extends ClientHandler {

    /**
     *  Logger for this class.
     */
    private static final Logger LOG = java.util.logging.Logger.getLogger(ClientHandlerRmi.class.getName());

    /** RMI stub for invoking methods on the remote client. */
    private final VirtualViewRmi clientStub;

    /** Listener notified when disconnection is detected. */
    private final DisconnectionListener disconnectionListener;

    /**
     * Dedicated single-thread executor that runs ping tasks in isolation,
     * preventing a slow or hung RMI call from blocking the health check thread.
     *
     * <p>A single thread is enough because only one ping is in flight at a time;
     * using an unbounded cached pool would allow zombie threads to accumulate when
     * RMI blocks indefinitely on a dead client (RMI is not interruptible via
     * {@link Future#cancel(boolean)}).
     */
    private final ExecutorService pingExecutor = Executors.newSingleThreadExecutor();

    /** Maximum time in milliseconds to wait for a ping response. */
    private static final int PING_TIMEOUT = 3000;

    /** Number of consecutive ping failures before marking the client as disconnected. */
    private static final int MAX_FAILURES = 2;

    /**
     * Creates a new RMI handler, binds it to the client stub, and starts
     * the health check.
     *
     * @param nickname the player's nickname
     * @param clientStub the remote client stub
     * @param disconnectionListener listener to notify on disconnection
     */
    public ClientHandlerRmi(String nickname,
                            VirtualViewRmi clientStub,
                            DisconnectionListener disconnectionListener) {
        super(nickname);
        this.clientStub = clientStub;
        this.disconnectionListener = disconnectionListener;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Starts a daemon thread that pings the client every {@link #PING_INTERVAL} ms.
     * Each ping is submitted to {@link #pingExecutor} with a {@link #PING_TIMEOUT}.
     * {@link TimeoutException} or {@link ExecutionException} increments the failure
     * counter; reaching {@link #MAX_FAILURES} triggers disconnection.
     */
    @Override
    protected void startHealthCheck() {
        Thread healthCheck = new Thread(() -> {
            int failures = 0;
            while (!disconnected) {
                try {
                    Future<Void> ping = pingExecutor.submit(() -> {
                        clientStub.ping();
                        return null;
                    });
                    try {
                        ping.get(PING_TIMEOUT, TimeUnit.MILLISECONDS);
                        failures = 0;
                        Thread.sleep(PING_INTERVAL);
                    } catch (TimeoutException | ExecutionException e) {
                        ping.cancel(true);
                        failures++;
                        LOG.warning(String.format("[HEALTH CHECK FAILED] %s (%d/%d)", nickname, failures, MAX_FAILURES));
                    }

                    if (failures >= MAX_FAILURES && tryMarkDisconnected()) {
                        disconnectionListener.handleDisconnection(nickname);
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            pingExecutor.shutdownNow();
        }, "HealthCheck-" + nickname);

        healthCheck.setDaemon(true);
        healthCheck.start();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Handles a {@link RemoteException} that occurred during async sending:
     * calls {@link #tryMarkDisconnected()} and notifies the disconnection listener.
     * Idempotent.
     */
    @Override
    protected void handleTransportError(Exception e) {
        LOG.log(Level.WARNING, "[RMI] Client " + nickname + " unreachable", e);
        if (tryMarkDisconnected()) {
            disconnectionListener.handleDisconnection(nickname);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>In addition to the base cleanup, shuts down the {@link #pingExecutor}.
     */
    @Override
    protected synchronized boolean tryMarkDisconnected() {
        if (!super.tryMarkDisconnected()) return false;
        pingExecutor.shutdownNow();
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Guards against {@code null} visitor: the visitor is set externally via
     * {@link #setVisitor(ClientMessageVisitor)} after construction, so a race between
     * visitor injection and game start could otherwise cause a
     * {@link NullPointerException}.
     */
    @Override
    public void injectGameVisitor(GameMessageVisitor gameVisitor) {
        if (visitor != null) visitor.setGameVisitor(gameVisitor);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Guards against {@code null} visitor for the same reason as
     * {@link #injectGameVisitor(GameMessageVisitor)}.
     */
    @Override
    public void resetGameVisitor() {
        if (visitor != null) visitor.setGameVisitor(null);
    }

    /** {@inheritDoc} */
    @Override
    public void onReconnection(int matchId) {
        sendAsync(() -> clientStub.reconnect(matchId));
    }

    /** {@inheritDoc} */
    @Override
    public void onCurrPlayerUpdate(String nickname) {
        sendAsync(() -> clientStub.onCurrPlayerUpdate(nickname));
    }


    /** {@inheritDoc} */
    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        sendAsync(() -> clientStub.onGameEnding(stats, rankingPos, globalRankingPos));
    }

    /** {@inheritDoc} */
    @Override
    public void onMoveUpdate(TileDTO tile, String nextPlayer) {
        sendAsync(() -> clientStub.onMoveUpdate(tile, nextPlayer));
    }

    /** {@inheritDoc} */
    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        sendAsync(() -> clientStub.onReturnToQueue(tileDTO, playerStatsDTO));
    }

    /** {@inheritDoc} */
    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        sendAsync(() -> clientStub.onDrawUpdate(c, nickname));
    }

    /** {@inheritDoc} */
    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        sendAsync(() -> clientStub.onStatusUpdate(status));
    }

    /** {@inheritDoc} */
    @Override
    public void onStatsUpdate(PlayerStatsDTO stats) {
        sendAsync(() -> clientStub.onStatsUpdate(stats));
    }

    /** {@inheritDoc} */
    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        sendAsync(() -> clientStub.onPhaseUpdate(phaseDTO));
    }

    /** {@inheritDoc} */
    @Override
    public void showBoard(BoardDTO board) {
        sendAsync(() -> clientStub.showBoard(board));
    }

    /** {@inheritDoc} */
    @Override
    public void notifyDrawable(ActionsDTO actions) {
        sendAsync(() -> clientStub.notifyDrawable(actions));
    }

    /** {@inheritDoc} */
    @Override
    public void notifySkip(String nickname) {
        sendAsync(() -> clientStub.notifySkip(nickname));
    }

    /** {@inheritDoc} */
    @Override
    public void onChangeAge(ChangeAgeDTO dto) {
        sendAsync(() -> clientStub.onChangeAge(dto));
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(EventDTO events) {
        sendAsync(() -> clientStub.onEvent(events));
    }

    /** {@inheritDoc} */
    @Override
    public void onQuitServer(String reason) {
        sendAsync(() -> clientStub.onQuitServer(reason));
    }
}