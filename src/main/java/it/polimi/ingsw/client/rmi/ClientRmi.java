package it.polimi.ingsw.client.rmi;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.server.VirtualServerRmi;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RMI implementation of {@link Client}.
 *
 * <p>Handles only the RMI transport layer: forwards each game action to the
 * remote server stub and receives server callbacks via {@link VirtualViewRmi}.
 * All input validation logic lives in {@link Client}.
 */
public class ClientRmi extends Client implements VirtualViewRmi {

    /** The remote server stub used to invoke game actions on the server. */
    private final VirtualServerRmi serverStub;

    /** Whether the connection to the server is currently active. */
    private volatile boolean connected = true;

    /** Port on which the RMI registry is expected to be running. */
    private static final int PORT = 1099;

    /** Interval in milliseconds between consecutive ping attempts. */
    private static final int PING_INTERVAL = 2000;

    /**
     * Single-threaded executor that serialises order-sensitive UI updates,
     * ensuring {@code onEvent} is always processed before {@code onGameEnding}.
     * This prevents a race condition where the end-game banner would not appear
     * in the GUI because {@code onGameEnding} arrived first on the JavaFX queue.
     */
    private final ExecutorService uiEventExecutor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "rmi-ui-event-sequencer"));

    /**
     * Connects to the RMI registry at the given host and exports this object
     * as a remote callback endpoint.
     *
     * @param ip    the server IP address
     * @param model the local virtual model to populate with server updates
     * @throws RemoteException      if the RMI registry cannot be reached
     * @throws NotBoundException    if the server stub is not registered
     * @throws UnknownHostException if the host cannot be resolved
     */
    public ClientRmi(String ip, VirtualModel model)
            throws RemoteException, NotBoundException, UnknownHostException {
        super(model);
        try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
            socket.connect(java.net.InetAddress.getByName(ip), PORT);
            System.setProperty("java.rmi.server.hostname",
                    socket.getLocalAddress().getHostAddress());
        } catch (Exception e) {
            System.setProperty("java.rmi.server.hostname",
                    java.net.InetAddress.getLocalHost().getHostAddress());
        }

        Registry registry = LocateRegistry.getRegistry(ip, PORT);
        this.serverStub = (VirtualServerRmi) registry.lookup("GameServer");
        UnicastRemoteObject.exportObject(this, 0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends the login request to the server via RMI, updates the virtual
     * model with the chosen nickname, notifies the UI, and starts the
     * health check thread.
     */
    @Override
    protected boolean doLogin(String nickname) {
        try {
            serverStub.login(nickname, this);
            vm.setNickname(nickname);
            ui.onLogin(nickname);
            startHealthCheck();
            return true;
        } catch (RemoteException | InvalidUsernameException | InvalidTimingException e) {
            ui.printError(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Forwards the create-game request to the server and notifies the UI
     * with the assigned match ID upon success.
     */
    @Override
    protected void doCreateGame(String nickname, int numPlayers) {
        try {
            matchId = serverStub.createGame(nickname, numPlayers);
            ui.onCreate(matchId);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Forwards the join-game request to the server and stores the match ID
     * locally upon success.
     */
    @Override
    protected void doJoinGame(String nickname, int id) {
        try {
            serverStub.joinGame(nickname, id);
            matchId = id;
            ui.onJoin(matchId);
        } catch (RemoteException | InvalidLobbyException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Forwards the tile-placement move to the server via RMI.
     */
    @Override
    protected void doMove(int tileId) {
        try {
            serverStub.move(vm.getNickname(), tileId);
        } catch (RemoteException | GameException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Forwards the draw request to the server. If the draw is invalid and
     * further draws are still available, the UI is prompted to show the
     * drawable options again.
     */
    @Override
    protected void doDraw(int card) {
        try {
            serverStub.draw(card, vm.getNickname());
        } catch (InvalidDrawException e) {
            ui.printError(e);
            ActionsDTO a = vm.getToDoActions();
            if (a.getUpDraws() + a.getDownDraws() > 0)
                ui.showDrawable();
        }catch (GameException | RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Forwards the skip-turn request to the server via RMI.
     */
    @Override
    protected void doSkip() {
        try {
            serverStub.skip(vm.getNickname());
        } catch (RemoteException | GameException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fetches the end-of-game ranking from the server on a dedicated thread
     * to avoid blocking the caller.
     */
    @Override
    protected void doRequestRanking() {
        new Thread(() -> {
            try {
                Map<String, Integer> ranking = serverStub.requestRanking(vm.getNickname());
                ui.showRanking(ranking);
            } catch (RemoteException | InvalidTimingException e) {
                ui.printError(e);
            }
        }, "End Game UI").start();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Retrieves the list of available lobbies from the server and forwards
     * it to the UI. Sets {@code lobbiesAvailable} accordingly.
     */
    @Override
    public void requestJoin() {
        Map<Integer, List<LobbyDTO>> lobbies = askLobbies();
        lobbiesAvailable = lobbies != null && lobbies.values().stream().anyMatch(l -> !l.isEmpty());
        ui.displayLobbies(lobbies);
    }

    /**
     * Queries the server for the current list of open lobbies.
     *
     * @return a map from match ID to the list of lobbies, or an empty map on error
     */
    private Map<Integer, List<LobbyDTO>> askLobbies() {
        try {
            return serverStub.getLobbies(vm.getNickname());
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
            return Collections.emptyMap();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Notifies the server that the player is voluntarily leaving the match,
     * resets the local match state, and triggers the quit UI callback.
     */
    @Override
    public void quit() {
        if (!isInGame()){
            ui.printError(new InvalidTimingException("Impossibile uscire dalla partita se non sei in gioco."));
            return;
        }
        try {
            serverStub.quit(vm.getNickname());
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
        resetMatch();
        if(gameEnded){
            ui.onQuit("Sei tornato al menu principale.");
        }
        else
            ui.onQuit("Hai abbandonato la partita.");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Notifies the server of the disconnection, resets the local state, and
     * exits the application.
     */
    @Override
    public void exit() {
        try {
            serverStub.handleDisconnection(vm.getNickname());
        } catch (RemoteException e) {
            ui.printError(e);
        }
        resetMatch();
        ui.exit();
    }

    /**
     * Called by the server when a tile has been placed on the board.
     *
     * @param tile       the DTO representing the placed tile
     * @param currPlayer the nickname of the player who made the move
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) throws RemoteException {
        vm.onMoveUpdate(tile);
        ui.onMoveUpdate(tile, currPlayer);
    }

    /**
     * Called by the server when the active player changes.
     *
     * @param nickname the nickname of the new current player
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onCurrPlayerUpdate(String nickname) throws RemoteException {
        vm.onCurrPlayerUpdate(nickname);
        ui.onCurrPlayerUpdate(nickname);
    }

    /**
     * Called by the server when the game phase changes.
     *
     * @param phaseDTO the DTO representing the new phase
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) throws RemoteException {
        vm.onPhaseUpdate(phaseDTO);
        ui.onPhaseUpdate(phaseDTO);
    }

    /**
     * Called by the server when the game ends.
     * Enqueues the UI callback on the {@link #uiEventExecutor} to guarantee it
     * runs after any pending {@code onEvent} callbacks.
     *
     * @param stats             the final statistics for all players
     * @param rankingPos        the local player's position in the match ranking
     * @param globalRankingPos  the local player's position in the global leaderboard
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos)
            throws RemoteException {
        gameEnded = true;
        uiEventExecutor.execute(() -> ui.onGameEnding(stats, rankingPos, globalRankingPos));
    }

    /**
     * Called by the server when a card has been drawn.
     *
     * @param c        the DTO of the drawn card
     * @param nickname the nickname of the player who drew
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onDrawUpdate(CardDTO c, String nickname) throws RemoteException {
        vm.onDrawUpdate(c, nickname);
        ui.onDrawUpdate(c, nickname);
    }

    /**
     * Called by the server when a player's status changes.
     *
     * @param status the updated player status DTO
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onStatusUpdate(PlayerStatusDTO status) throws RemoteException {
        vm.onStatusUpdate(status);
        ui.onStatusUpdate(status);
    }

    /**
     * Called by the server when a player's statistics are updated.
     *
     * @param stats  the updated player statistics DTO
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onStatsUpdate(PlayerStatsDTO stats) throws RemoteException {
        vm.onStatsUpdate(stats);
        ui.onStatsUpdate(stats);
    }

    /**
     * Called by the server to notify all clients that a player has skipped
     * their turn.
     *
     * @param nickname the nickname of the player who skipped
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void notifySkip(String nickname) throws RemoteException {
        vm.skip();
        ui.notifySkip(nickname);
    }

    /**
     * Called by the server when the player is allowed to draw a card.
     *
     * @param actions the DTO describing how many draws are available
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void notifyDrawable(ActionsDTO actions) throws RemoteException {
        vm.updateToDoActions(actions);
        ui.showDrawable();
    }

    /**
     * Called by the server when the player is returned to the queue.
     *
     * @param tileDTO        the tile returned to the pool
     * @param playerStatsDTO the player's updated statistics
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) throws RemoteException {
        vm.onReturnToQueue(tileDTO, playerStatsDTO);
        ui.onReturnToQueue(tileDTO, playerStatsDTO);
    }

    /**
     * Called by the server when the game age advances.
     *
     * @param dto the DTO containing the new age value
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onChangeAge(ChangeAgeDTO dto) throws RemoteException {
        vm.onChangeAge(dto);
        ui.onChangeAge(dto.getAge());
    }

    /**
     * Called by the server when one or more game events occur.
     * Snapshots the current player stats before applying the update
     * so the UI can animate the delta, then enqueues the UI callback on the
     * {@link #uiEventExecutor} to preserve ordering with respect to
     * {@link #onGameEnding}.
     *
     * @param events the DTO carrying the event list and updated stats
     */
    @Override
    public void onEvent(EventDTO events){
        List<PlayerStatsDTO> statsBefore = new ArrayList<>(vm.getPlayerStats());
        vm.updateAllStats(events.getStats());
        uiEventExecutor.execute(() -> {
            try {
                ui.onEvent(events, statsBefore);
            } catch (Exception e) {
                ui.printError(e);
            }
        });
    }

    /**
     * Called by the server to send the full board state to the client,
     * typically at the start of the game or after a reconnection.
     *
     * @param board the DTO representing the entire board state
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void showBoard(BoardDTO board) throws RemoteException {
        vm.update(board);
        ui.showBoard();
    }

    /**
     * Called by the server to forcefully disconnect the client from the
     * current match.
     *
     * @param reason a human-readable explanation for the disconnection
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void onQuitServer(String reason) throws RemoteException {
        if (!isInGame()) return;
        resetMatch();
        ui.onQuit(reason);
    }

    /**
     * No-op ping method used by the server to verify that this client is still
     * reachable.
     *
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void ping() throws RemoteException { }

    /**
     * Restores the client into an active match after a reconnection.
     *
     * @param matchId the ID of the match to rejoin
     */
    public void reconnect(int matchId) {
        this.matchId = matchId;
        ui.reconnect(matchId);
    }

    /**
     * Starts a daemon thread that periodically pings the server to detect
     * connection loss. If a {@link RemoteException} is thrown, the connection
     * is marked as lost, the match state is reset, and the UI is notified via
     * {@link UserInterface#onServerCrash()}.
     */
    private void startHealthCheck() {
        Thread t = new Thread(() -> {
            java.util.concurrent.ExecutorService pingExecutor =
                    java.util.concurrent.Executors.newSingleThreadExecutor();

            final long TIMEOUT_LIMIT = 6000;

            while (connected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    java.util.concurrent.Future<?> future = pingExecutor.submit(() -> {
                        try {
                            serverStub.ping();
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    future.get(TIMEOUT_LIMIT, java.util.concurrent.TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (java.util.concurrent.TimeoutException | java.util.concurrent.ExecutionException e) {
                    connected = false;
                    pingExecutor.shutdownNow();
                    resetMatch();
                    ui.onServerCrash();
                    break;
                }
            }
            pingExecutor.shutdown();
        }, "HealthCheck-Client-RMI");
        t.setDaemon(true);
        t.start();
    }
}