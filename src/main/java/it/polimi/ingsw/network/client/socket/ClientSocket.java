package it.polimi.ingsw.network.client.socket;

import it.polimi.ingsw.client.CardRegistry;
import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.dto.TileDTO;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.server.ServerMessage;
import it.polimi.ingsw.visitors.ServerMessageVisitor;
import it.polimi.ingsw.visitors.ServerMessageVisitorImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientSocket extends Client implements Runnable{
    private boolean hasAvailableLobbies = false;
    private int matchId;
    private VirtualModel model;
    private UserInterface ui;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerMessageVisitor visitor;
    private volatile boolean connected = false;
    private CompletableFuture<Boolean> loginFuture;
    private volatile boolean gameEnded = false;

    private final int PING_TIMEOUT = 6000;
    private volatile boolean intentionalDisconnect = false;

    public ClientSocket(String ip, int port, VirtualModel model) throws IOException{
        this.matchId = 0;
        this.model = model;
        this.socket = new Socket(ip, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.connected = true;
    }

    @Override
    public VirtualModel getModel() { return model;}

    @Override
    public void setUi(UserInterface ui) {
        if (this.ui == null) {
            this.ui = ui;
            this.visitor = new ServerMessageVisitorImpl(model, ui, this);
        }
    }

    public synchronized void sendMessage(ClientMessage message){
        try{
            if(connected && out != null) {
                out.writeObject(message);
                out.flush();
                out.reset();
            }
        }catch (IOException e){
            disconnect();
        }
    }

    @Override
    public void run() {
        try{
            while(connected){
                ServerMessage message = (ServerMessage) in.readObject();
                if (message != null)
                    message.accept(visitor);
            }
        }catch (IOException | ClassNotFoundException e){
            disconnect();
            if(ui != null && !intentionalDisconnect) {
                matchId = 0;
                model = ui.quit();
                ui.onServerCrash();
            }
        }catch (Exception e){
            if(ui != null)
                ui.printError(e);
        }
    }

    private void disconnect(){
        connected = false;
        try {
            if(in != null)
                in.close();
            if(out != null)
                out.close();
            if(socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            if(ui != null)
                ui.printError(e);
            else
                e.printStackTrace();
        }
    }

    @Override
    public String getNickname() {
        return model.getNickname();
    }

    @Override
    public boolean hasAvailableLobbies() {
        return hasAvailableLobbies;
    }

    public void setHasAvailableLobbies(boolean hasAvailableLobbies){
        this.hasAvailableLobbies = hasAvailableLobbies;
    }

    @Override
    public void start() {
        Thread readerThread = new Thread(this, "Socket-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
        ui.start();
    }

    @Override
    public boolean isInGame() {
        return matchId != 0;
    }

    @Override
    public void help() {
        ui.displayHelpMessage();
    }

    @Override
    public void info(int cardId) {
        if (isInGame())
            ui.info(cardId);
        else
            ui.printError(new InvalidTimingException("Non è consentito richiedere informazioni prima che la partita sia iniziata."));
    }

    @Override
    public boolean login(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            ui.printError(new AlreadyExistingUsernameException("Il nickname non può essere vuoto."));
            return false;
        }

        model.setNickname(nickname);
        loginFuture = new CompletableFuture<>();
        sendMessage(new LoginMessage(nickname));
        try{
            return loginFuture.get();
        }catch(Exception e){
            return false;
        }
    }

    public void onLoginSuccess(){
        if(loginFuture != null && !loginFuture.isDone()){
            loginFuture.complete(true);
            try {
                this.socket.setSoTimeout(PING_TIMEOUT);
            } catch (SocketException e) {
                ui.printError(e);
            }
        }
    }

    public void onLoginFailed(){
        if(loginFuture != null && !loginFuture.isDone())
            loginFuture.complete(false);
    }

    @Override
    public void createGame(String nickname, int numPlayers) {
        if (numPlayers < 2 || numPlayers > 5) {
            ui.printError(new InvalidLobbySizeException("Il numero di giocatori deve essere tra 2 e 5."));
            return;
        }
        if (isInGame()) {
            ui.printError(new InvalidTimingException("Sei già in una partita."));
            return;
        }
        gameEnded = false;
        sendMessage(new CreateGameMessage(numPlayers, nickname));
    }

    @Override
    public void joinGame(String nickname, int id) {
        if (isInGame()) {
            ui.printError(new InvalidTimingException("Sei già in una partita."));
            return;
        }
        if(id <= 0){
            ui.printError(new InvalidLobbyException("Codice lobby non valido."));
            return;
        }
        gameEnded = false;
        sendMessage(new JoinGameMessage(id, nickname));
    }

    @Override
    public void move(int tileId) {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è possibile muoversi prima che la partita sia iniziata."));
            return;
        }
        if (model.getCurrentPhase() != GamePhaseEnum.SETUP_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!model.getNickname().equals(model.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        List<TileDTO> board = model.getBoard();
        if (tileId < 0 || tileId >= board.size()) {
            ui.printError(new InvalidMoveException("Tile non esistente."));
            return;
        }
        if (board.get(tileId).isOccupied()) {
            ui.printError(new OccupiedTileException("Tile già occupata."));
            return;
        }
        sendMessage(new MoveMessage(model.getNickname(), tileId));
    }

    @Override
    public void draw(int card) {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è consentito pescare prima che la partita sia iniziata."));
            return;
        }
        GamePhaseEnum phase = model.getCurrentPhase();
        if (phase != GamePhaseEnum.DRAW_PHASE && phase != GamePhaseEnum.OPTIONAL_DRAW_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!model.getNickname().equals(model.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        boolean inUpper = model.getUpperList().stream().anyMatch(c -> c.getId() == card);
        boolean inLower = model.getLowerList().stream().anyMatch(c -> c.getId() == card);
        if (!inUpper && !inLower) {
            ui.printError(new InvalidDrawException("Carta non presente."));
            return;
        }
        ActionsDTO actions = model.getToDoActions();
        if (inUpper && actions.getUpDraws() <= 0) {
            ui.printError(new InvalidDrawException("Nessuna pescata disponibile dalla fila superiore."));
            if(model.getToDoActions().getUpDraws() + model.getToDoActions().getDownDraws() > 0)
                ui.showDrawable();
            return;
        }
        if (inLower && actions.getDownDraws() <= 0) {
            ui.printError(new InvalidDrawException("Nessuna pescata disponibile dalla fila inferiore."));
            if(model.getToDoActions().getUpDraws() + model.getToDoActions().getDownDraws() > 0)
                ui.showDrawable();
            return;
        }

        if (CardRegistry.getType(card) == CardTypeEnum.BUILDING) {
            PlayerStatsDTO myStats = model.getPlayerStats().stream()
                    .filter(s -> s.getNickname().equals(model.getNickname()))
                    .findFirst().orElse(null);
            if (myStats != null) {
                int actualCost = Math.max(0, CardRegistry.getCost(card) - myStats.getTotBuildDisc());
                if (myStats.getnFood() < actualCost) {
                    ui.printError(new InvalidDrawException("Cibo insufficiente per acquistare questo edificio."));
                    return;
                }
            }
        }

        sendMessage(new DrawMessage(card, model.getNickname()));
    }

    @Override
    public void skip() {
        if (!isInGame()) {
            ui.printError(new InvalidTimingException("Non è consentito saltare la fase di pesca prima che la partita sia iniziata."));
            return;
        }
        GamePhaseEnum phase = model.getCurrentPhase();
        if (phase != GamePhaseEnum.DRAW_PHASE && phase != GamePhaseEnum.OPTIONAL_DRAW_PHASE) {
            ui.printError(new InvalidPhaseException("FASE INVALIDA"));
            return;
        }
        if (!model.getNickname().equals(model.getCurrPlayer())) {
            ui.printError(new InvalidPlayerException("NON E' IL TUO TURNO"));
            return;
        }
        if (!model.getToDoActions().isOptionalFlag()) {
            ui.printError(new InvalidSkipException("Non è possibile saltare la pesca adesso."));
            return;
        }
        sendMessage(new SkipMessage(model.getNickname()));
    }
    @Override
    public void requestJoin() {
        sendMessage(new AskLobbiesMessage());
    }

    @Override
    public void showStatus() {
        if (isInGame())
            ui.showStatusScreen();
        else
            ui.printError(new InvalidTimingException("Non è possibile richiedere informazioni prima che la partita sia iniziata."));
    }

    @Override
    public void quit() {
        if(!isInGame()) return;
        sendMessage(new QuitMessage());
        matchId = 0;
        this.model = ui.quit();
        this.visitor = new ServerMessageVisitorImpl(model,ui,this);
        ui.onQuit("Hai abbandonato la partita.");
    }

    @Override
    public void exit() {
        intentionalDisconnect = true;
        sendMessage(new ExitMessage());
        ui.exit();
        disconnect();
    }

    public void setVisitor(ServerMessageVisitor visitor){
        this.visitor = visitor;
    }

    public void setMatchId(int matchId){this.matchId = matchId;}

    public boolean isConnected(){return connected;}

    public void setGameEnded(){gameEnded = true;}

    public void requestRanking(){
        if(!gameEnded){
            ui.printError(new InvalidTimingException("Il ranking è disponibile solo a fine partita."));
            return;
        }
        sendMessage(new RankingRequestMessage(model.getNickname()));
    }
}