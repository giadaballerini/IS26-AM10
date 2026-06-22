package it.polimi.ingsw.network.server.socket;

import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.network.server.DisconnectionListener;
import it.polimi.ingsw.server.MatchManager;
import it.polimi.ingsw.visitors.ClientMessageVisitor;
import it.polimi.ingsw.visitors.ClientMessageVisitorImpl;
import it.polimi.ingsw.visitors.GameMessageVisitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Server-side socket handler for a connected client.
 *
 * <p>Implements {@link Runnable} to be executed by the server's thread pool.
 * The {@link #run()} method initializes I/O streams, then enters a read loop
 * deserializing incoming {@link ClientMessage} and routing them to
 * {@link ClientMessageVisitor}.
 *
 * <p>All outgoing {@link ServerMessage} pass through
 * {@link ClientHandler#sendAsync(CheckedRunnable)}, queued on the single-threaded
 * executor for ordered, non-concurrent writes to the output stream.
 *
 * <p>A health check thread periodically sends {@link PingMessage}. If no messages
 * arrive within {@link #TIMEOUT_LIMIT} milliseconds, the connection is deemed
 * lost and disconnection is triggered.
 *
 * <h2>Design</h2>
 * <p>The base class entirely manages disconnection flag, message executor, and async pattern.
 * This class handles only socket-specific logic: I/O streams,
 * Java serialization, timestamp-based timeouts.
 */
public class ClientHandlerSocket extends ClientHandler
        implements VirtualView, Runnable, DisconnectionListener {

    /** Underlying TCP socket for this client connection. */
    private final Socket clientSocket;

    /** Shared match coordinator. */
    private final MatchManager matchManager;

    /** Output stream for sending serialized {@link ServerMessage}. */
    private volatile ObjectOutputStream out;

    /** Input stream for receiving serialized {@link ClientMessage}. */
    private volatile ObjectInputStream in;

    /** Timestamp of the last received message (including pongs). */
    private volatile long lastMessageReceivedTime;

    /**
     *  Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(ClientHandlerSocket.class.getName());

    /**
     * Maximum inactivity duration in milliseconds before declaring the
     * connection lost. Should exceed the client's socket read timeout.
     */
    private static final int TIMEOUT_LIMIT = 6000;

    /**
     * Creates a new socket handler. The nickname is initially unknown
     * and will be set by {@link #onLogin(String)} after login.
     *
     * <p>Does not start the health check; it is started by {@link #onLogin(String)}
     * once the nickname is available for logging.
     *
     * @param socket the accepted TCP socket
     * @param matchManager the match coordinator
     */
    public ClientHandlerSocket(Socket socket, MatchManager matchManager) {
        super(null);
        this.clientSocket = socket;
        this.matchManager = matchManager;
        this.visitor = new ClientMessageVisitorImpl(matchManager, this);
    }

    /**
     * Initializes object I/O streams from the underlying socket.
     *
     * <p>The {@link ObjectOutputStream} must be created first, as the
     * {@link ObjectInputStream} constructor on the remote side blocks until
     * the stream header is received.
     *
     * @throws IOException if streams cannot be opened
     */
    public void setup() throws IOException {
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in  = new ObjectInputStream(clientSocket.getInputStream());
    }

    /**
     * Main loop executed by the thread pool thread.
     *
     * <p>Calls {@link #setup()}, then reads {@link ClientMessage} objects in a loop,
     * dispatches them via {@link #onClientMessage(ClientMessage)}, and updates
     * {@link #lastMessageReceivedTime} on each successful read.
     * On {@link IOException} or {@link ClassNotFoundException}, the loop exits,
     * and disconnection is triggered.
     */
    @Override
    public void run() {
        try {
            setup();
        } catch (IOException e) {
            handleDisconnection(this.nickname);
            return;
        }

        while (!disconnected) {
            try {
                ClientMessage message = (ClientMessage) in.readObject();
                if (message != null) {
                    lastMessageReceivedTime = System.currentTimeMillis();
                    onClientMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                handleDisconnection(this.nickname);
                break;
            } catch (Exception e) {
                onErrorMessage(e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Starts a daemon thread that every {@link #PING_INTERVAL} ms sends a
     * {@link PingMessage} and checks for activity within {@link #TIMEOUT_LIMIT} ms.
     * If the timeout is exceeded, disconnection is triggered.
     *
     * <p>Called by {@link #onLogin(String)} after the nickname is known,
     * enabling meaningful thread names for logging.
     */
    @Override
    protected void startHealthCheck() {
        Thread healthCheck = new Thread(() -> {
            while (!disconnected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    sendAsync(() -> {
                        out.writeObject(new PingMessage());
                        out.reset();
                        out.flush();
                    });
                    LOG.fine("Ping sent to " + nickname);
                    long inactivity = System.currentTimeMillis() - lastMessageReceivedTime;
                    if (inactivity > TIMEOUT_LIMIT) {
                        LOG.warning(String.format("[HEALTH CHECK SOCKET FAILED] %s exceeded timeout.%n",
                                nickname));
                        handleDisconnection(nickname);
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "HealthCheckSocket-" + (nickname != null ? nickname : clientSocket.getPort()));

        healthCheck.setDaemon(true);
        healthCheck.start();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #handleDisconnection(String)} with the current nickname,
     * consolidating cleanup logic in a single place.
     */
    @Override
    protected void handleTransportError(Exception e) {
        handleDisconnection(this.nickname);
    }

    /**
     * {@inheritDoc}
     *
     * <p>In addition to base cleanup, closes I/O streams and the TCP socket.
     */
    @Override
    protected synchronized boolean tryMarkDisconnected() {
        if (!super.tryMarkDisconnected()) return false;
        closeStreams();
        return true;
    }

    /**
     * Marks the client as disconnected, notifies the {@link MatchManager},
     * and closes resources. Idempotent.
     *
     * <p>If the client disconnects before completing login the nickname is
     * {@code null}; in that case the socket is closed but {@link MatchManager}
     * is not notified, since the player was never registered.
     *
     * @param nickname the player's nickname, or {@code null} if not yet logged in
     */
    @Override
    public void handleDisconnection(String nickname) {
        if (!tryMarkDisconnected()) return;
        if (nickname == null) {
            LOG.severe("[SOCKET] Anonymous client disconnected (pre-login).");
            return;
        }
        LOG.severe("[SOCKET] Client " + nickname + " disconnected");
        matchManager.disconnect(nickname);
    }

    /**
     * Closes I/O streams and the TCP socket.
     * Any {@link IOException} during cleanup is logged and ignored.
     */
    private void closeStreams() {
        try {
            if (in  != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            LOG.severe("[SOCKET] Error during disconnection cleanup.");
        }
    }

    /**
     * Called after successful login: stores the nickname and starts the health check.
     *
     * <p>Note: the {@code nickname} field is {@code final} in the base class.
     * In the socket protocol the nickname is not known at construction time,
     * so this method must be called exactly once immediately after login.
     *
     * @param nickname the registered player nickname
     */
    public void onLogin(String nickname) {
        this.nickname = nickname;
        this.lastMessageReceivedTime = System.currentTimeMillis();
        startHealthCheck();
    }

    /** Sends a login success confirmation to the client. */
    public void onLoginSuccess(String nickname) {
        sendAsync(() -> {
            out.writeObject(new LoginSuccessMessage(nickname));
            out.reset(); out.flush();
        });
    }

    /** Sends a login failure message to the client. */
    public void onLoginFailed(String error) {
        sendAsync(() -> {
            out.writeObject(new LoginFailedMessage(error));
            out.reset(); out.flush();
        });
    }

    /** Sends the list of available lobbies to the client. */
    public void onLobbiesRequested(Map<Integer, List<LobbyDTO>> lobbies) {
        sendAsync(() -> {
            out.writeObject(new AvailableLobbiesMessage(lobbies));
            out.reset(); out.flush();
        });
    }

    /** Confirms lobby creation to the client. */
    public void onGameCreated(int gameId) {
        sendAsync(() -> {
            out.writeObject(new GameCreatedMessage(gameId));
            out.reset(); out.flush();
        });
    }

    /** Confirms match entry to the client. */
    public void onJoinGame(int id) {
        sendAsync(() -> {
            out.writeObject(new GameJoinedMessage(id));
            out.reset(); out.flush();
        });
    }

    /** Sends the global ranking to the client. */
    public void onRankingResponse(Map<String, Integer> ranking) {
        sendAsync(() -> {
            out.writeObject(new RankingResponseMessage(ranking));
            out.reset(); out.flush();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void injectGameVisitor(GameMessageVisitor gameVisitor) {
        visitor.setGameVisitor(gameVisitor);
    }

    /** {@inheritDoc} */
    @Override
    public void resetGameVisitor() {
        visitor.setGameVisitor(null);
    }

    /**
     * Notifies the client of a server-side error.
     *
     * @param errorMsg human-readable description of the error; never {@code null}
     */
    public void onErrorMessage(String errorMsg) {
        sendAsync(() -> { out.writeObject(new ErrorMessage(errorMsg)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onReconnection(int matchId) {
        sendAsync(() -> { out.writeObject(new ReconnectionMessage(matchId)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onCurrPlayerUpdate(String nickname) {
        sendAsync(() -> { out.writeObject(new CurrPlayerUpdateMessage(nickname)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        sendAsync(() -> { out.writeObject(new GameEndingUpdateMessage(stats, rankingPos, globalRankingPos)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onMoveUpdate(TileDTO tile, String nextPlayer) {
        sendAsync(() -> { out.writeObject(new MoveUpdateMessage(tile, nextPlayer)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        sendAsync(() -> { out.writeObject(new ReturnToQueueUpdateMessage(tileDTO, playerStatsDTO)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        sendAsync(() -> { out.writeObject(new DrawUpdateMessage(c, nickname)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        sendAsync(() -> { out.writeObject(new StatusUpdateMessage(status)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onStatsUpdate(PlayerStatsDTO stats) {
        sendAsync(() -> { out.writeObject(new StatsUpdateMessage(stats)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        sendAsync(() -> { out.writeObject(new PhaseUpdateMessage(phaseDTO)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void showBoard(BoardDTO board) {
        sendAsync(() -> { out.writeObject(new ShowBoardMessage(board)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void notifyDrawable(ActionsDTO actions) {
        sendAsync(() -> { out.writeObject(new NotifyDrawableMessage(actions)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void notifySkip(String nickname) {
        sendAsync(() -> { out.writeObject(new NotifySkipMessage(nickname)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onChangeAge(ChangeAgeDTO dto) {
        sendAsync(() -> { out.writeObject(new ChangeAgeUpdateMessage(dto)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(EventDTO e) {
        sendAsync(() -> { out.writeObject(new EventMessage(e)); out.reset(); out.flush(); });
    }

    /** {@inheritDoc} */
    @Override
    public void onQuitServer(String reason) {
        LOG.info("[HEALTH CHECK SOCKET] " + reason);
        sendAsync(() -> { out.writeObject(new QuitAckMessage(reason)); out.reset(); out.flush(); });
    }
}