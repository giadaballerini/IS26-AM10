package it.polimi.ingsw.network.client.socket;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.server.ServerMessage;
import it.polimi.ingsw.visitors.ServerMessageVisitor;
import it.polimi.ingsw.visitors.ServerMessageVisitorImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;

/**
 * Socket (TCP) implementation of {@link Client}.
 *
 * <p>Handles only the socket transport layer: serialises outgoing
 * {@link ClientMessage} objects via {@link #sendMessage} and dispatches
 * incoming {@link ServerMessage} objects through a {@link ServerMessageVisitor}
 * on a dedicated reader thread.
 * All input validation logic lives in {@link Client}.
 */
public class ClientSocket extends Client implements Runnable {

    /** The underlying TCP socket connected to the server. */
    private Socket socket;

    /** Output stream used to serialize and send {@link ClientMessage} objects. */
    private ObjectOutputStream out;

    /** Input stream used to deserialize incoming {@link ServerMessage} objects. */
    private ObjectInputStream in;

    /**
     * Visitor that dispatches each received {@link ServerMessage} to the
     * appropriate handler on the virtual model and the UI.
     */
    private ServerMessageVisitor visitor;

    /** Whether the socket connection is currently active. */
    private volatile boolean connected;

    /**
     * Set to {@code true} before an intentional disconnection so that the
     * reader thread does not trigger the server-crash UI callback when the
     * socket is closed from this side.
     */
    private volatile boolean intentionalDisconnect = false;

    /**
     * Future used to synchronize the login flow: completed with {@code true}
     * on success (see {@link #onLoginSuccess()}) or {@code false} on failure
     * (see {@link #onLoginFailed()}).
     */
    private CompletableFuture<Boolean> loginFuture;

    /**
     * Socket read timeout in milliseconds. If no data (including a ping) is
     * received within this window, the connection is considered lost.
     */
    private static final int PING_TIMEOUT = 6000;

    /**
     * Opens a TCP connection to the given host and initializes the object streams.
     *
     * @param ip    the server IP address
     * @param port  the server port
     * @param model the local virtual model to populate with server updates
     * @throws IOException if the socket or streams cannot be opened
     */
    public ClientSocket(String ip, int port, VirtualModel model) throws IOException {
        super(model);
        this.socket = new Socket(ip, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.connected = true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Also initializes the {@link ServerMessageVisitor} that requires a
     * reference to this {@code ClientSocket}.
     */
    @Override
    public void setUi(UserInterface ui) {
        if (this.ui == null) {
            this.ui = ui;
            this.visitor = new ServerMessageVisitorImpl(vm, ui, this);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link LoginMessage} to the server and blocks until the server
     * replies with either a success or a failure confirmation, which is delivered
     * via {@link #onLoginSuccess()} or {@link #onLoginFailed()} respectively.
     */
    @Override
    protected boolean doLogin(String nickname) {
        vm.setNickname(nickname);
        loginFuture = new CompletableFuture<>();
        sendMessage(new LoginMessage(nickname));
        try {
            return loginFuture.get();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Called by {@link ServerMessageVisitorImpl} when a login-success message arrives.
     * Completes the login future and activates the ping timeout on the socket.
     */
    public void onLoginSuccess() {
        if (loginFuture != null && !loginFuture.isDone()) {
            loginFuture.complete(true);
            try {
                socket.setSoTimeout(PING_TIMEOUT);
            } catch (SocketException e) {
                ui.printError(e);
            }
        }
    }

    /**
     * Called by {@link ServerMessageVisitorImpl} when a login-failure message arrives.
     * Completes the login future with {@code false} so that {@link #doLogin} can
     * return and let the player retry.
     */
    public void onLoginFailed() {
        if (loginFuture != null && !loginFuture.isDone())
            loginFuture.complete(false);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link CreateGameMessage} to the server with the desired
     * number of players.
     */
    @Override
    protected void doCreateGame(String nickname, int numPlayers) {
        sendMessage(new CreateGameMessage(numPlayers, nickname));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link JoinGameMessage} to the server for the specified lobby.
     */
    @Override
    protected void doJoinGame(String nickname, int id) {
        sendMessage(new JoinGameMessage(id, nickname));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link MoveMessage} to the server with the chosen tile ID.
     */
    @Override
    protected void doMove(int tileId) {
        sendMessage(new MoveMessage(vm.getNickname(), tileId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link DrawMessage} to the server with the chosen card index.
     */
    @Override
    protected void doDraw(int card) {
        sendMessage(new DrawMessage(card, vm.getNickname()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link SkipMessage} to the server to skip the current turn.
     */
    @Override
    protected void doSkip() {
        sendMessage(new SkipMessage(vm.getNickname()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link RankingRequestMessage} to the server to retrieve the
     * end-of-game ranking.
     */
    @Override
    protected void doRequestRanking() {
        sendMessage(new RankingRequestMessage(vm.getNickname()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends an {@link AskLobbiesMessage} to the server to request the list
     * of currently available lobbies.
     */
    @Override
    public void requestJoin() {
        sendMessage(new AskLobbiesMessage());
    }

    /**
     * Updates the lobbies-available flag.
     * Called by {@link ServerMessageVisitorImpl} after receiving an
     * {@code AvailableLobbiesMessage} from the server.
     *
     * @param value {@code true} if at least one joinable lobby exists
     */
    public void setHasAvailableLobbies(boolean value) {
        this.lobbiesAvailable = value;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@link QuitMessage}, resets the local match state, refreshes
     * the visitor instance, and notifies the UI.
     */
    @Override
    public void quit() {
        if (!isInGame()) return;
        sendMessage(new QuitMessage());
        resetMatch();
        this.visitor = new ServerMessageVisitorImpl(vm, ui, this);
        ui.onQuit("Hai abbandonato la partita.");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sets the intentional-disconnect flag, sends an {@link ExitMessage} to
     * the server, notifies the UI, and closes the socket.
     */
    @Override
    public void exit() {
        intentionalDisconnect = true;
        sendMessage(new ExitMessage());
        ui.exit();
        disconnect();
    }

    /**
     * Marks the current game as ended so that {@link #requestRanking()} becomes
     * available. Called by {@link ServerMessageVisitorImpl} upon receiving a
     * game-ending message from the server.
     */
    public void setGameEnded() { gameEnded = true; }

    /**
     * Sets the active match ID.
     * Called by {@link ServerMessageVisitorImpl} when a game-created or
     * game-joined confirmation arrives from the server.
     *
     * @param matchId the assigned match ID
     */
    public void setMatchId(int matchId) { this.matchId = matchId; }

    /**
     * Returns whether the socket connection is still open.
     *
     * @return {@code true} if the connection is active
     */
    public boolean isConnected() { return connected; }

    /**
     * Replaces the active visitor. Useful when reconnecting to swap in a fresh
     * visitor instance that references the updated {@link VirtualModel}.
     *
     * @param visitor the new visitor to install
     */
    public void setVisitor(ServerMessageVisitor visitor) { this.visitor = visitor; }

    /**
     * Sends a message to the server. Thread-safe; silently drops the message
     * and triggers a {@link #disconnect()} if the output stream has been closed.
     *
     * @param message the message to send
     */
    public synchronized void sendMessage(ClientMessage message) {
        try {
            if (connected && out != null) {
                out.writeObject(message);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            disconnect();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Spawns the daemon reader thread that runs {@link #run()} and then
     * delegates to {@link UserInterface#start()} to show the initial UI.
     */
    @Override
    public void start() {
        Thread readerThread = new Thread(this, "Socket-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
        ui.start();
    }

    /**
     * Reader-thread body. Continuously reads {@link ServerMessage} objects from
     * the input stream and dispatches each one to the {@link #visitor}.
     *
     * <p>If an {@link IOException} or {@link ClassNotFoundException} is caught
     * and the disconnect was not intentional, the match state is reset and
     * {@link UserInterface#onServerCrash()} is invoked to inform the player.
     */
    @Override
    public void run() {
        try {
            while (connected) {
                ServerMessage message = (ServerMessage) in.readObject();
                if (message != null)
                    message.accept(visitor);
            }
        } catch (IOException | ClassNotFoundException e) {
            disconnect();
            if (ui != null && !intentionalDisconnect) {
                resetMatch();
                ui.onServerCrash();
            }
        } catch (Exception e) {
            if (ui != null)
                ui.printError(e);
        }
    }

    /**
     * Closes all I/O streams and the underlying socket, setting
     * {@link #connected} to {@code false}. Any {@link IOException} during
     * cleanup is forwarded to the UI if available, or printed to stderr
     * otherwise.
     */
    private void disconnect() {
        connected = false;
        try {
            if (in != null)  in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            if (ui != null) ui.printError(e);
            else e.printStackTrace();
        }
    }
}