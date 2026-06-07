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
 * {@link it.polimi.ingsw.network.messages.client.ClientMessage} objects via
 * {@link #sendMessage} and dispatches incoming
 * {@link it.polimi.ingsw.network.messages.server.ServerMessage} objects through
 * a {@link ServerMessageVisitor} on a dedicated reader thread.
 * All input validation logic lives in {@link Client}.
 */
public class ClientSocket extends Client implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerMessageVisitor visitor;
    private volatile boolean connected;
    private volatile boolean intentionalDisconnect = false;
    private CompletableFuture<Boolean> loginFuture;

    private static final int PING_TIMEOUT = 6000;

    /**
     * Opens a TCP connection to the given host and initialises the object streams.
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
     * <p>Also initialises the {@link ServerMessageVisitor} that requires a
     * reference to this {@code ClientSocket}.
     */
    @Override
    public void setUi(UserInterface ui) {
        if (this.ui == null) {
            this.ui = ui;
            this.visitor = new ServerMessageVisitorImpl(vm, ui, this);
        }
    }

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
     */
    public void onLoginFailed() {
        if (loginFuture != null && !loginFuture.isDone())
            loginFuture.complete(false);
    }

    @Override
    protected void doCreateGame(String nickname, int numPlayers) {
        sendMessage(new CreateGameMessage(numPlayers, nickname));
    }

    @Override
    protected void doJoinGame(String nickname, int id) {
        sendMessage(new JoinGameMessage(id, nickname));
    }

    @Override
    protected void doMove(int tileId) {
        sendMessage(new MoveMessage(vm.getNickname(), tileId));
    }

    @Override
    protected void doDraw(int card) {
        sendMessage(new DrawMessage(card, vm.getNickname()));
    }

    @Override
    protected void doSkip() {
        sendMessage(new SkipMessage(vm.getNickname()));
    }

    @Override
    protected void doRequestRanking() {
        sendMessage(new RankingRequestMessage(vm.getNickname()));
    }

    @Override
    public void requestJoin() {
        sendMessage(new AskLobbiesMessage());
    }

    /**
     * Updates the lobbies-available flag.
     * Called by {@link ServerMessageVisitorImpl} after receiving an
     * {@code AvailableLobbiesMessage}.
     *
     * @param value {@code true} if at least one joinable lobby exists
     */
    public void setHasAvailableLobbies(boolean value) {
        this.lobbiesAvailable = value;
    }

    @Override
    public void quit() {
        if (!isInGame()) return;
        sendMessage(new QuitMessage());
        resetMatch();
        this.visitor = new ServerMessageVisitorImpl(vm, ui, this);
        ui.onQuit("Hai abbandonato la partita.");
    }

    @Override
    public void exit() {
        intentionalDisconnect = true;
        sendMessage(new ExitMessage());
        ui.exit();
        disconnect();
    }

    /**
     * Marks the current game as ended so that {@link #requestRanking()} becomes available.
     * Called by {@link ServerMessageVisitorImpl} upon receiving a game-ending message.
     */
    public void setGameEnded() { gameEnded = true; }

    /**
     * Sets the active match ID.
     * Called by {@link ServerMessageVisitorImpl} when a game-created or game-joined
     * confirmation arrives.
     *
     * @param matchId the assigned match ID
     */
    public void setMatchId(int matchId) { this.matchId = matchId; }

    /** @return {@code true} if the socket connection is still open */
    public boolean isConnected() { return connected; }

    /**
     * Replaces the active visitor. Useful when reconnecting to swap in a fresh
     * visitor instance that references the updated {@link VirtualModel}.
     *
     * @param visitor the new visitor
     */
    public void setVisitor(ServerMessageVisitor visitor) { this.visitor = visitor; }

    /**
     * Sends a message to the server. Thread-safe; silently drops the message
     * and triggers a disconnect if the stream has been closed.
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

    @Override
    public void start() {
        Thread readerThread = new Thread(this, "Socket-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
        ui.start();
    }

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