package it.polimi.ingsw.network.client.rmi;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;
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

    private final VirtualServerRmi serverStub;
    private volatile boolean connected = true;

    private static final int PORT = 1099;
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
        try {
            try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
                socket.connect(java.net.InetAddress.getByName(ip), PORT);
            }
        } catch (Exception e) {
            System.setProperty("java.rmi.server.hostname",
                    java.net.InetAddress.getLocalHost().getHostAddress());
        }
        Registry registry = LocateRegistry.getRegistry(ip, PORT);
        this.serverStub = (VirtualServerRmi) registry.lookup("GameServer");
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    protected boolean doLogin(String nickname) {
        try {
            serverStub.login(nickname, this);
            vm.setNickname(nickname);
            ui.onLogin(nickname);
            startHealthCheck();
            return true;
        } catch (RemoteException | AlreadyExistingUsernameException | InvalidTimingException e) {
            ui.printError(e);
            return false;
        }
    }

    @Override
    protected void doCreateGame(String nickname, int numPlayers) {
        try {
            matchId = serverStub.createGame(nickname, numPlayers);
            ui.onCreate(matchId);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

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

    @Override
    protected void doMove(int tileId) {
        try {
            serverStub.move(vm.getNickname(), tileId);
        } catch (RemoteException | InvalidMoveException | InvalidPhaseException
                 | InvalidPlayerException | OccupiedTileException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    @Override
    protected void doDraw(int card) {
        try {
            serverStub.draw(card, vm.getNickname());
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        } catch (InvalidDrawException e) {
            ui.printError(e);
            ActionsDTO a = vm.getToDoActions();
            if (a.getUpDraws() + a.getDownDraws() > 0)
                ui.showDrawable();
        }
    }

    @Override
    protected void doSkip() {
        try {
            serverStub.skip(vm.getNickname());
        } catch (RemoteException | InvalidSkipException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    @Override
    protected void doRequestRanking() {
        new Thread(() -> {
            try {
                Map<String, Integer> ranking = serverStub.requestRanking(vm.getNickname());
                ui.showRanking(ranking);
            } catch (RemoteException e) {
                ui.printError(e);
            }
        }, "End Game UI").start();
    }

    @Override
    public void requestJoin() {
        Map<Integer, List<LobbyDTO>> lobbies = askLobbies();
        lobbiesAvailable = lobbies != null && lobbies.values().stream().anyMatch(l -> !l.isEmpty());
        ui.displayLobbies(lobbies);
    }

    private Map<Integer, List<LobbyDTO>> askLobbies() {
        try {
            return serverStub.getLobbies(vm.getNickname());
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void quit() {
        if (!isInGame()) return;
        try {
            serverStub.quit(vm.getNickname());
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
        resetMatch();
        ui.onQuit("Hai abbandonato la partita.");
    }

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

    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) throws RemoteException {
        vm.onMoveUpdate(tile);
        ui.onMoveUpdate(tile, currPlayer);
    }

    @Override
    public void onCurrPlayerUpdate(String nickname) throws RemoteException {
        vm.onCurrPlayerUpdate(nickname);
        ui.onCurrPlayerUpdate(nickname);
    }

    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) throws RemoteException {
        vm.onPhaseUpdate(phaseDTO);
        ui.onPhaseUpdate(phaseDTO);
    }

    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos)
            throws RemoteException {
        gameEnded = true;
        uiEventExecutor.execute(() -> ui.onGameEnding(stats, rankingPos, globalRankingPos));
    }

    @Override
    public void onDrawUpdate(CardDTO c, String nickname) throws RemoteException {
        vm.onDrawUpdate(c, nickname);
        ui.onDrawUpdate(c, nickname);
    }

    @Override
    public void onStatusUpdate(PlayerStatusDTO status) throws RemoteException {
        vm.onStatusUpdate(status);
        ui.onStatusUpdate(status);
    }

    @Override
    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) throws RemoteException {
        vm.onStatsUpdate(stats);
        ui.onStatsUpdate(stats);
    }

    @Override
    public void notifySkip(String nickname) throws RemoteException {
        vm.skip();
        ui.notifySkip(nickname);
    }

    @Override
    public void notifyDrawable(ActionsDTO actions) throws RemoteException {
        vm.updateToDoActions(actions);
        ui.showDrawable();
    }

    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) throws RemoteException {
        vm.onReturnToQueue(tileDTO, playerStatsDTO);
        ui.onReturnToQueue(tileDTO, playerStatsDTO);
    }

    @Override
    public void onChangeAge(ChangeAgeDTO dto) throws RemoteException {
        vm.onChangeAge(dto);
        ui.onChangeAge(dto.getAge());
    }

    @Override
    public void onEvent(EventDTO events) throws RemoteException {
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

    @Override
    public void showBoard(BoardDTO board) throws RemoteException {
        vm.update(board);
        ui.showBoard();
    }

    @Override
    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) throws RemoteException {
        ui.showLeaderboard(ranks);
    }

    @Override
    public void onQuitServer(String reason) throws RemoteException {
        if (!isInGame()) return;
        resetMatch();
        ui.onQuit(reason);
    }

    @Override
    public void printError(String e) throws RemoteException {
        ui.printError(new Exception(e));
    }

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

    private void startHealthCheck() {
        Thread t = new Thread(() -> {
            while (connected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    serverStub.ping();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (RemoteException e) {
                    connected = false;
                    resetMatch();
                    ui.onServerCrash();
                    break;
                }
            }
        }, "HealthCheck-Client-RMI");
        t.setDaemon(true);
        t.start();
    }
}