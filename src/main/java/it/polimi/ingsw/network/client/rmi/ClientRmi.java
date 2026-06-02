package it.polimi.ingsw.network.client.rmi;

import it.polimi.ingsw.client.CardRegistry;
import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.server.VirtualServerRmi;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRmi extends Client implements VirtualViewRmi {

    private final VirtualServerRmi serverStub;
    private String nickname;
    private VirtualModel vm;
    private UserInterface ui;
    private int matchId;
    private static final int PORT = 1099;
    private static final int PING_INTERVAL = 2000;

    /**
     * Executor single-threaded che serializza gli aggiornamenti UI sensibili
     * all'ordine (onEvent prima di onGameEnding).
     * Risolve la race condition per cui il banner degli eventi di fine partita
     * non veniva visualizzato nella GUI perché onGameEnding arrivava prima
     * sulla coda di Platform.runLater.
     */
    private final ExecutorService uiEventExecutor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "rmi-ui-event-sequencer"));
    private boolean lobbiesAvailable = false;
    private volatile boolean connected = true;
    private volatile boolean gameEnded = false;

    public ClientRmi(String ip, VirtualModel model) throws RemoteException, NotBoundException, UnknownHostException {
        this.matchId = 0;
        this.vm = model;
        try{
            try(java.net.DatagramSocket socket = new java.net.DatagramSocket()){
                socket.connect(java.net.InetAddress.getByName(ip),1099);

            } }catch (Exception e){
            System.setProperty("java.rmi.server.hostname", java.net.InetAddress.getLocalHost().getHostAddress());
        }

        Registry registry = LocateRegistry.getRegistry(ip, PORT);
        this.serverStub = (VirtualServerRmi) registry.lookup("GameServer");
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public VirtualModel getModel() { return vm; }

    @Override
    public void setUi(UserInterface ui) {
        if (this.ui == null)
            this.ui = ui;
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
                    matchId = 0;
                    vm = ui.quit();
                    ui.onServerCrash();
                    break;
                }
            }
        }, "HealthCheck-Client-RMI");
        t.setDaemon(true);
        t.start();
    }

    public boolean login(String nickname) {
        if(nickname == null || nickname.isBlank()){
            ui.printError(new AlreadyExistingUsernameException("Il nickname non può essere vuoto."));
            return false;
        }

        try {
            serverStub.login(nickname, this);
            this.nickname = nickname;
            this.vm.setNickname(nickname);
            ui.onLogin(this.nickname);
            startHealthCheck();
            return true;
        } catch (RemoteException | AlreadyExistingUsernameException | InvalidTimingException e) {
            ui.printError(e);
            return false;
        }
    }

    public void createGame(String nickname, int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) {
            ui.printError(new InvalidLobbySizeException("Il numero di giocatori deve essere tra 2 e 5."));
            return;
        }
        try {
            if(!isInGame()){
                matchId = serverStub.createGame(nickname, numPlayers);
                gameEnded = false;
                onCreate();
            }
            else
                ui.printError(new InvalidTimingException("Sei gia in una partita."));
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    public void joinGame(String nickname, int id) {
        if (isInGame()) {
            ui.printError(new InvalidTimingException("Sei già in una partita."));
            return;
        }
        if (id <= 0) {
            ui.printError(new InvalidLobbyException("Codice lobby non valido."));
            return;
        }
        try {
            serverStub.joinGame(nickname, id);
            matchId = id;
            gameEnded = false;
            onJoin();
        } catch (RemoteException | InvalidLobbyException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    @Override
    public void showStatus(){
        if (this.isInGame())
            ui.showStatusScreen();
        else
            ui.printError(new InvalidTimingException("Non è possibile richiedere informazioni prima che la partita sia iniziata"));

    }
    public void onCreate() { ui.onCreate(matchId); }
    public void onJoin() { ui.onJoin(matchId); }


    public void requestJoin() {
        Map<Integer, List<LobbyDTO>> lobbies = askLobbies();
        lobbiesAvailable = lobbies != null && lobbies.values().stream().anyMatch(l -> !l.isEmpty());
        ui.displayLobbies(lobbies);
    }

    public boolean hasAvailableLobbies() { return lobbiesAvailable; }

    private Map<Integer, List<LobbyDTO>> askLobbies() {
        try {
            return serverStub.getLobbies(nickname);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void info(int cardId) {
        if (this.isInGame())
            ui.info(cardId);
        else
            ui.printError(new InvalidTimingException("Non è consentito richiedere informazioni prima che la partita sia iniziata"));
    }

    public void move(int tileId) {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è possibile muoversi prima che la partita sia iniziata."));
            return;
        }
        if (vm.getCurrentPhase() != GamePhaseEnum.SETUP_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!vm.getNickname().equals(vm.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        List<TileDTO> board = vm.getBoard();
        if (tileId < 0 || tileId >= board.size()) {
            ui.printError(new InvalidMoveException("Tile non esistente."));
            return;
        }
        if (board.get(tileId).isOccupied()) {
            ui.printError(new OccupiedTileException("Tile già occupata."));
            return;
        }
        try {
            serverStub.move(nickname, tileId);
        } catch (RemoteException | InvalidMoveException | InvalidPhaseException | InvalidPlayerException | OccupiedTileException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    public void draw(int card) {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è consentito pescare prima che la partita sia iniziata."));
            return;
        }
        GamePhaseEnum phase = vm.getCurrentPhase();
        if (phase != GamePhaseEnum.DRAW_PHASE && phase != GamePhaseEnum.OPTIONAL_DRAW_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!vm.getNickname().equals(vm.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }

        boolean inUpper = vm.getUpperList().stream().anyMatch(c -> c.getId() == card);
        boolean inLower = vm.getLowerList().stream().anyMatch(c -> c.getId() == card);

        if (!inUpper && !inLower) {
            ui.printError(new InvalidDrawException("Carta non presente."));
            return;
        }

        ActionsDTO actions = vm.getToDoActions();
        if (inUpper && actions.getUpDraws() <= 0) {
            ui.printError(new InvalidDrawException("Nessuna pescata disponibile dalla fila superiore."));
            if(vm.getToDoActions().getUpDraws() + vm.getToDoActions().getDownDraws() > 0)
                ui.showDrawable();

            return;
        }
        if (inLower && actions.getDownDraws() <= 0) {
            ui.printError(new InvalidDrawException("Nessuna pescata disponibile dalla fila inferiore."));
            if(vm.getToDoActions().getUpDraws() + vm.getToDoActions().getDownDraws() > 0)
                ui.showDrawable();
            return;
        }

        if (CardRegistry.getType(card) == CardTypeEnum.BUILDING) {
            PlayerStatsDTO myStats = vm.getPlayerStats().stream()
                    .filter(s -> s.getNickname().equals(vm.getNickname()))
                    .findFirst().orElse(null);
            if (myStats != null) {
                int actualCost = Math.max(0, CardRegistry.getCost(card) - myStats.getTotBuildDisc());
                if (myStats.getnFood() < actualCost) {
                    ui.printError(new InvalidDrawException("Cibo insufficiente per acquistare questo edificio."));
                    return;
                }
            }
        }

        try {
            serverStub.draw(card, nickname);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }catch (InvalidDrawException e){
            ui.printError(e);
            if(vm.getToDoActions().getUpDraws() + vm.getToDoActions().getDownDraws() > 0)
                ui.showDrawable();
        }

    }

    public void skip() {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è consentito saltare la fase di pesca prima che la partita sia iniziata."));
            return;
        }
        GamePhaseEnum phase = vm.getCurrentPhase();
        if (phase != GamePhaseEnum.DRAW_PHASE && phase != GamePhaseEnum.OPTIONAL_DRAW_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!vm.getNickname().equals(vm.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        if (!vm.getToDoActions().isOptionalFlag()) {
            ui.printError(new InvalidSkipException("Non è possibile saltare la pesca adesso."));
            return;
        }
        try {
            serverStub.skip(nickname);
        } catch (RemoteException | InvalidSkipException | InvalidTimingException e) {
            ui.printError(e);
        }
    }

    public void quit() {
        if(!isInGame()) return;
        try {
            serverStub.quit(nickname);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
        }
        matchId = 0;
        vm = ui.quit();
        ui.onQuit("Hai abbandonato la partita.");
    }

    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) throws RemoteException {
        ui.showLeaderboard(ranks);
    }

    public void onMoveUpdate(TileDTO tile, String currPlayer) throws RemoteException {
        vm.onMoveUpdate(tile);
        ui.onMoveUpdate(tile, currPlayer);
    }

    public void onCurrPlayerUpdate(String nickname) throws RemoteException {
        vm.onCurrPlayerUpdate(nickname);
        ui.onCurrPlayerUpdate(nickname);
    }

    public void onPhaseUpdate(PhaseDTO phaseDTO) throws RemoteException {
        vm.onPhaseUpdate(phaseDTO);
        ui.onPhaseUpdate(phaseDTO);
    }

    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) throws RemoteException {
        gameEnded = true;
        uiEventExecutor.execute(() -> ui.onGameEnding(stats, rankingPos, globalRankingPos));
    }

    public void onDrawUpdate(CardDTO c, String nickname) throws RemoteException {
        vm.onDrawUpdate(c, nickname);
        ui.onDrawUpdate(c, nickname);
    }

    public void onStatusUpdate(PlayerStatusDTO status) throws RemoteException {
        vm.onStatusUpdate(status);
        ui.onStatusUpdate(status);
    }

    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) throws RemoteException {
        vm.onStatsUpdate(stats);
        ui.onStatsUpdate(stats);
    }

    public void refresh(List<PlayerDTO> listPlayers, BoardDTO board) throws RemoteException {
        vm.update(board);
        ui.showBoard();
    }

    public void notifySkip(String nickname) throws RemoteException {
        vm.skip();
        ui.notifySkip(nickname);
    }

    public void notifyDrawable(ActionsDTO actions) throws RemoteException {
        vm.updateToDoActions(actions);
        ui.showDrawable();
    }

    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) throws RemoteException {
        vm.onReturnToQueue(tileDTO, playerStatsDTO);
        ui.onReturnToQueue(tileDTO, playerStatsDTO);
    }

    public void onChangeAge(ChangeAgeDTO dto) throws RemoteException {
        vm.onChangeAge(dto);
        ui.onChangeAge(dto.getAge());
    }

    public void requestRanking() {
        if(!gameEnded){
            ui.printError(new InvalidTimingException("Il ranking è disponibile solo a fine partita."));
            return;
        }
        new Thread(() -> {
            try {
                Map<String, Integer> ranking = serverStub.requestRanking(nickname);
                ui.showRanking(ranking);
            } catch(RemoteException e) {
                ui.printError(e);
            }
        }, "End Game UI").start();
    }

    @Override
    public void onEvent(EventDTO events) throws RemoteException {
        List<PlayerStatsDTO> statsBefore = new ArrayList<>(vm.getPlayerStats());
        vm.updateAllStats(events.getStats());
        uiEventExecutor.execute(() -> {
            try {
                ui.onEvent(events, statsBefore);
            }
            catch(Exception e){
                ui.printError(e);
            }
        });
    }

    @Override
    public String getNickname() { return nickname; }

    public void showBoard(BoardDTO board) throws RemoteException {
        this.vm.update(board);
        this.ui.showBoard();
    }

    public void start() { ui.start(); }

    @Override
    public void onQuitServer(String reason) throws RemoteException {
        if(!isInGame()) return;
        matchId = 0;
        vm = ui.quit();
        ui.onQuit(reason);
    }

    public void printError(String e) throws RemoteException {
        ui.printError(new Exception(e));
    }

    @Override
    public void ping() throws RemoteException {
    }

    public boolean isInGame() { return matchId != 0; }

    public void exit() {
        try {
            serverStub.handleDisconnection(nickname);
        } catch (RemoteException e) {
            ui.printError(e);
        }
        matchId = 0;
        vm = ui.quit();
        ui.exit();
    }

    public void help() { ui.displayHelpMessage(); }

    public void reconnect(int matchid){
        matchId = matchid;
        ui.reconnect(matchid);
    }


}