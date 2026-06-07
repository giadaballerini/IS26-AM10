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

public class ClientHandlerSocket extends ClientHandler implements VirtualView, Runnable, DisconnectionListener {
    private final Socket clientSocket;
    private final MatchManager matchManager;
    private volatile ClientMessageVisitor visitor;
    private volatile String nickname;
    private volatile ObjectOutputStream out;
    private volatile ObjectInputStream in;
    private volatile boolean connected = true;

    private final ExecutorService sender = Executors.newSingleThreadExecutor();

    private volatile long lastMessageReceivedTime;
    private static final int PING_INTERVAL = 2000;
    private static final int TIMEOUT_LIMIT = 6000;

    public ClientHandlerSocket(Socket socket, MatchManager matchManager) {
        this.clientSocket = socket;
        this.matchManager = matchManager;
        this.visitor = new ClientMessageVisitorImpl(matchManager,this);
    }
    public void setup() throws IOException {
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

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

    private void startHealthCheck() {
        Thread healthCheckThread = new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    sendMessage(new PingMessage());
                    System.out.println("Ping inviato a " + nickname);

                    long timeSinceLastMessage = System.currentTimeMillis() - lastMessageReceivedTime;

                    if (timeSinceLastMessage > TIMEOUT_LIMIT) {
                        System.out.println("[HEALTH CHECK SOCKET FALLITO] " + nickname + " ha superato il timeout.");
                        handleDisconnection(nickname);
                        break;
                    }

                } catch (InterruptedException e) {
                    System.out.println("[HEALTH CHECK SOCKET] Thread interrotto per " + nickname);
                    Thread.currentThread().interrupt();
                    handleDisconnection(nickname);
                    break;
                }
            }
        }, "HealthCheckSocket-" + (nickname != null ? nickname : clientSocket.getPort()));

        healthCheckThread.setDaemon(true);
        healthCheckThread.start();
    }

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

    private void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[SOCKET] Errore durante la disconnessione.");
        }
    }

    public void onErrorMessage(String errorMsg) { sendMessage(new ErrorMessage(errorMsg)); }
    public String getNickname() { return nickname; }
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) { sendMessage(new ReturnToQueueUpdateMessage(tileDTO, playerStatsDTO)); }
    public void onCurrPlayerUpdate(String nickname) { sendMessage(new CurrPlayerUpdateMessage(nickname)); }
    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) { sendMessage(new RequestLeaderboardUpdateMessage(ranks)); }
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) { sendMessage(new GameEndingUpdateMessage(stats,rankingPos, globalRankingPos)); }
    public void onMoveUpdate(TileDTO tile, String nextPlayer) { sendMessage(new MoveUpdateMessage(tile, nextPlayer)); }
    public void onDrawUpdate(CardDTO c, String nickname) { sendMessage(new DrawUpdateMessage(c, nickname)); }
    public void onStatusUpdate(PlayerStatusDTO status) { sendMessage(new StatusUpdateMessage(status)); }
    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) { sendMessage(new StatsUpdateMessage(stats, cardId)); }
    public void onLogin(String nickname) {
        this.nickname = nickname;
        this.lastMessageReceivedTime = System.currentTimeMillis();
        startHealthCheck();}

    public void onLoginSuccess(String nickname){
        sendMessage(new LoginSuccessMessage(nickname));
    }

    public void onLoginFailed(String error){
        sendMessage(new LoginFailedMessage(error));
    }

    public void onPhaseUpdate(PhaseDTO phaseDTO) { sendMessage(new PhaseUpdateMessage(phaseDTO)); }
    public void notifySkip(String nickname) { sendMessage(new NotifySkipMessage(nickname)); }
    public void notifyDrawable(ActionsDTO actions) { sendMessage(new NotifyDrawableMessage(actions)); }
    public void onQuitServer(String reason){
        System.out.println("[HEALTH CHECK SOCKET] " + reason);
        sendMessage(new QuitAckMessage(reason));
    }
    @Override
    public void onClientMessage(ClientMessage m) { m.accept(visitor); }
    public void setVisitor(ClientMessageVisitor visitor) { this.visitor = visitor; }
    public void showBoard(BoardDTO b) { sendMessage(new ShowBoardMessage(b)); }
    public void onChangeAge(ChangeAgeDTO age) { sendMessage(new ChangeAgeUpdateMessage(age)); }
    public void onEvent(EventDTO e) { sendMessage(new EventMessage(e)); }
    public void onLobbiesRequested(Map<Integer, List<LobbyDTO>> lobbies) { sendMessage(new AvailableLobbiesMessage(lobbies)); }
    public void onGameCreated(int gameId) { sendMessage(new GameCreatedMessage(gameId)); }

    public void onJoinGame(int id){sendMessage(new GameJoinedMessage(id));}

    @Override
    public void injectGameVisitor(GameMessageVisitor gameVisitor){
        visitor.setGameVisitor(gameVisitor);
    }

    @Override
    public void resetGameVisitor(){
        visitor.setGameVisitor(null);
    }

    @Override
    public void onReconnection(int matchId){
        sendMessage(new ReconnectionMessage(matchId));
    }

    public void onRankingResponse(Map<String, Integer> ranking) { sendMessage(new RankingResponseMessage(ranking));}
}