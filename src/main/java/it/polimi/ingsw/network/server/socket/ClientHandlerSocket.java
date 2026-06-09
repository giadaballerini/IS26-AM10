package it.polimi.ingsw.network.server.socket;

import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.network.server.MatchManager;
import it.polimi.ingsw.network.server.rmi.DisconnectionListener;
import it.polimi.ingsw.visitors.ClientMessageVisitor;
import it.polimi.ingsw.visitors.ClientMessageVisitorImpl;
import it.polimi.ingsw.visitors.GameMessageVisitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server-side socket representative for a single connected client.
 *
 * <p>Implements {@link Runnable} so that the {@link ServerSocket} thread pool
 * can execute it directly. The {@link #run()} method initializes the streams
 * and then enters a read loop that deserializes incoming {@link ClientMessage}
 * objects and dispatches them to the {@link ClientMessageVisitor}.
 *
 * <p>All outbound {@link it.polimi.ingsw.network.messages.server.ServerMessage}
 * objects are queued through a single-threaded {@link #sender} executor to
 * guarantee ordered, non-concurrent writes to the output stream.
 *
 * <p>A background health check thread periodically sends {@link PingMessage}
 * objects. If {@link #TIMEOUT_LIMIT} milliseconds pass without any inbound
 * data, the connection is treated as lost and
 * {@link #handleDisconnection(String)} is called.
 *
 * <p>Also implements {@link DisconnectionListener} so that it can be used as
 * its own disconnection callback.
 */
public class ClientHandlerSocket extends ClientHandler
        implements VirtualView, Runnable, DisconnectionListener {

    /** The underlying TCP socket for this client. */
    private final Socket clientSocket;

    /** The shared match coordinator that processes all game actions. */
    private final MatchManager matchManager;

    /** Visitor that dispatches incoming {@link ClientMessage} objects to the controller. */
    private volatile ClientMessageVisitor visitor;

    /** The player's nickname, set after a successful login. */
    private volatile String nickname;

    /** Output stream for sending serialised {@link it.polimi.ingsw.network.messages.server.ServerMessage} objects. */
    private volatile ObjectOutputStream out;

    /** Input stream for receiving serialised {@link ClientMessage} objects. */
    private volatile ObjectInputStream in;

    /** Whether this handler is still connected. */
    private volatile boolean connected = true;

    /**
     * Single-threaded executor that serializes all outbound writes to
     * {@link #out}, preventing concurrent access to the output stream.
     */
    private final ExecutorService sender = Executors.newSingleThreadExecutor();

    /** Timestamp of the last message (including pongs) received from the client. */
    private volatile long lastMessageReceivedTime;

    /** Interval in milliseconds between consecutive health check pings. */
    private static final int PING_INTERVAL = 2000;

    /**
     * Maximum inactivity duration in milliseconds before the connection is
     * considered lost. Must be greater than the client-side socket read timeout
     * ({@code PING_TIMEOUT} in {@code ClientSocket}).
     */
    private static final int TIMEOUT_LIMIT = 6000;

    /**
     * Creates a new handler for the given client socket and registers a default
     * {@link ClientMessageVisitorImpl}.
     *
     * @param socket       the accepted TCP socket
     * @param matchManager the shared match coordinator
     */
    public ClientHandlerSocket(Socket socket, MatchManager matchManager) {
        this.clientSocket = socket;
        this.matchManager = matchManager;
        this.visitor = new ClientMessageVisitorImpl(matchManager, this);
    }

    /**
     * Initializes the object I/O streams from the underlying socket.
     *
     * <p>Must be called before the read loop starts. The output stream is
     * created first because the {@link ObjectInputStream} constructor on the
     * other side blocks until the header bytes from the output stream arrive.
     *
     * @throws IOException if the streams cannot be opened
     */
    public void setup() throws IOException {
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    /**
     * Read-loop body executed by the thread-pool thread.
     *
     * <p>Calls {@link #setup()} to initialize streams, then loops reading
     * {@link ClientMessage} objects and dispatching each one via
     * {@link #onClientMessage(ClientMessage)}. Updates
     * {@link #lastMessageReceivedTime} on every successful read.
     *
     * <p>On {@link IOException} or {@link ClassNotFoundException} the loop
     * exits and {@link #handleDisconnection(String)} is called.
     */
    @Override
    public void run() {
        try {
            setup();
        } catch (IOException e) {
            handleDisconnection(this.nickname);
            return;
        }

        while (connected) {
            try {
                ClientMessage message = (ClientMessage) in.readObject();
                if (message != null) {
                    this.lastMessageReceivedTime = System.currentTimeMillis();
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
     * Starts the health check daemon thread.
     *
     * <p>Every {@link #PING_INTERVAL} milliseconds, sends a {@link PingMessage}
     * and checks whether any data has been received within the last
     * {@link #TIMEOUT_LIMIT} milliseconds. If not, calls
     * {@link #handleDisconnection(String)}.
     *
     * <p>Called from {@link #onLogin(String)} after the player has identified
     * themselves so that the nickname is available for logging.
     */
    private void startHealthCheck() {
        Thread healthCheckThread = new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    sendMessage(new PingMessage());
                    System.out.println("Ping inviato a " + nickname);
                    long timeSinceLastMessage = System.currentTimeMillis() - lastMessageReceivedTime;
                    if (timeSinceLastMessage > TIMEOUT_LIMIT) {
                        System.out.println("[HEALTH CHECK SOCKET FALLITO] " + nickname
                                + " ha superato il timeout.");
                        handleDisconnection(nickname);
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    handleDisconnection(nickname);
                    break;
                }
            }
        }, "HealthCheckSocket-" + (nickname != null ? nickname : clientSocket.getPort()));

        healthCheckThread.setDaemon(true);
        healthCheckThread.start();
    }

    /**
     * Submits a message to the {@link #sender} executor for serialization.
     *
     * <p>Thread-safe; drops the message silently and triggers a disconnection
     * if the output stream has been closed.
     *
     * @param message the message object to send
     */
    private void sendMessage(Object message) {
        if (!connected) return;
        sender.submit(() -> {
            try {
                synchronized (out) {
                    out.writeObject(message);
                    out.reset();
                    out.flush();
                }
            } catch (IOException e) {
                handleDisconnection(this.nickname);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marks the connection as closed, shuts down the sender, notifies the
     * match manager, and closes the streams.
     */
    @Override
    public void handleDisconnection(String nickname) {
        synchronized (this) {
            if (!connected) return;
            connected = false;
            sender.shutdownNow();
        }
        System.err.println("[SOCKET] Client " + nickname + " disconnesso");
        matchManager.disconnect(nickname);
        disconnect();
    }

    /**
     * Closes all I/O streams and the underlying socket.
     * Any {@link IOException} during cleanup is logged to stderr.
     */
    private void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[SOCKET] Errore durante la disconnessione.");
        }
    }

    // ── ModelObserver / VirtualView callbacks ────────────────────────────────

    /** {@inheritDoc} */
    public void onErrorMessage(String errorMsg) { sendMessage(new ErrorMessage(errorMsg)); }

    /** {@inheritDoc} */
    public String getNickname() { return nickname; }

    /** {@inheritDoc} */
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        sendMessage(new ReturnToQueueUpdateMessage(tileDTO, playerStatsDTO));
    }

    /** {@inheritDoc} */
    public void onCurrPlayerUpdate(String nickname) {
        sendMessage(new CurrPlayerUpdateMessage(nickname));
    }

    /** {@inheritDoc} */
    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) {
        sendMessage(new RequestLeaderboardUpdateMessage(ranks));
    }

    /** {@inheritDoc} */
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        sendMessage(new GameEndingUpdateMessage(stats, rankingPos, globalRankingPos));
    }

    /** {@inheritDoc} */
    public void onMoveUpdate(TileDTO tile, String nextPlayer) {
        sendMessage(new MoveUpdateMessage(tile, nextPlayer));
    }

    /** {@inheritDoc} */
    public void onDrawUpdate(CardDTO c, String nickname) {
        sendMessage(new DrawUpdateMessage(c, nickname));
    }

    /** {@inheritDoc} */
    public void onStatusUpdate(PlayerStatusDTO status) {
        sendMessage(new StatusUpdateMessage(status));
    }

    /** {@inheritDoc} */
    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) {
        sendMessage(new StatsUpdateMessage(stats, cardId));
    }

    /**
     * Called after a successful login to store the nickname and start the
     * health check thread.
     *
     * @param nickname the player's registered nickname
     */
    public void onLogin(String nickname) {
        this.nickname = nickname;
        this.lastMessageReceivedTime = System.currentTimeMillis();
        startHealthCheck();
    }

    /**
     * Sends a {@link LoginSuccessMessage} to the client.
     *
     * @param nickname the nickname that was accepted
     */
    public void onLoginSuccess(String nickname) {
        sendMessage(new LoginSuccessMessage(nickname));
    }

    /**
     * Sends a {@link LoginFailedMessage} to the client.
     *
     * @param error human-readable reason for the failure
     */
    public void onLoginFailed(String error) {
        sendMessage(new LoginFailedMessage(error));
    }

    /** {@inheritDoc} */
    public void onPhaseUpdate(PhaseDTO phaseDTO) { sendMessage(new PhaseUpdateMessage(phaseDTO)); }

    /** {@inheritDoc} */
    public void notifySkip(String nickname) { sendMessage(new NotifySkipMessage(nickname)); }

    /** {@inheritDoc} */
    public void notifyDrawable(ActionsDTO actions) { sendMessage(new NotifyDrawableMessage(actions)); }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the reason in a {@link QuitAckMessage} and sends it to the client.
     */
    public void onQuitServer(String reason) {
        System.out.println("[HEALTH CHECK SOCKET] " + reason);
        sendMessage(new QuitAckMessage(reason));
    }

    /** {@inheritDoc} */
    @Override
    public void onClientMessage(ClientMessage m) { m.accept(visitor); }

    /** {@inheritDoc} */
    public void setVisitor(ClientMessageVisitor visitor) { this.visitor = visitor; }

    /** {@inheritDoc} */
    public void showBoard(BoardDTO b) { sendMessage(new ShowBoardMessage(b)); }

    /** {@inheritDoc} */
    public void onChangeAge(ChangeAgeDTO age) { sendMessage(new ChangeAgeUpdateMessage(age)); }

    /** {@inheritDoc} */
    public void onEvent(EventDTO e) { sendMessage(new EventMessage(e)); }

    /**
     * Sends the available-lobbies list to the client in response to a lobby
     * request.
     *
     * @param lobbies map from player capacity to the list of open lobbies
     */
    public void onLobbiesRequested(Map<Integer, List<LobbyDTO>> lobbies) {
        sendMessage(new AvailableLobbiesMessage(lobbies));
    }

    /**
     * Sends a {@link GameCreatedMessage} confirming the creation of a new lobby.
     *
     * @param gameId the ID assigned to the newly created match
     */
    public void onGameCreated(int gameId) { sendMessage(new GameCreatedMessage(gameId)); }

    /**
     * Sends a {@link GameJoinedMessage} confirming that the player has joined
     * the given match.
     *
     * @param id the ID of the joined match
     */
    public void onJoinGame(int id) { sendMessage(new GameJoinedMessage(id)); }

    /**
     * {@inheritDoc}
     *
     * <p>Injects the game-phase visitor into the current
     * {@link ClientMessageVisitor} so that in-game messages are routed to the
     * controller.
     */
    @Override
    public void injectGameVisitor(GameMessageVisitor gameVisitor) {
        visitor.setGameVisitor(gameVisitor);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Removes the game-phase visitor from the current
     * {@link ClientMessageVisitor}, reverting to lobby-only message handling.
     */
    @Override
    public void resetGameVisitor() {
        visitor.setGameVisitor(null);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link ReconnectionMessage} carrying the match ID so the client
     * can restore its local state.
     */
    @Override
    public void onReconnection(int matchId) {
        sendMessage(new ReconnectionMessage(matchId));
    }

    /**
     * Sends the global leaderboard to the client in response to a ranking
     * request.
     *
     * @param ranking map from player nickname to cumulative score
     */
    public void onRankingResponse(Map<String, Integer> ranking) {
        sendMessage(new RankingResponseMessage(ranking));
    }
}