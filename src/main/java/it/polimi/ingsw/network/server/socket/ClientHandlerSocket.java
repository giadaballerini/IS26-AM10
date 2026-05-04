package it.polimi.ingsw.network.server.socket;

import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.network.server.MatchManager;
import it.polimi.ingsw.network.server.rmi.DisconnectionListener;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandlerSocket extends ClientHandler implements VirtualView, Runnable, DisconnectionListener {
    private final Socket clientSocket;
    private final MatchManager matchManager;
    private ClientMessageVisitor visitor;
    private String nickname;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean connected = true;

    public ClientHandlerSocket(Socket socket, MatchManager matchManager) {
        this.clientSocket = socket;
        this.matchManager = matchManager;
    }

    public void setup() throws IOException {
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run(){
        try{
            setup();
            while(connected){
                ClientMessage message = (ClientMessage) in.readObject();
                if(message != null)
                    onClientMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            handleDisconnection(this.nickname);
        }
    }

    public void onErrorMessage(String errorMsg){
        sendMessage(new ErrorMessage(errorMsg));
    }

    public String getNickname(){return nickname;}


    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO){
        sendMessage(new ReturnToQueueUpdateMessage(tileDTO, playerStatsDTO));
    }

    public void onCurrPlayerUpdate(String nickname){
        sendMessage(new CurrPlayerUpdateMessage(nickname));
    }

    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks){
        //TODO
        sendMessage(new RequestLeaderboardUpdateMessage(ranks));
    }

    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos){
        //TODO
        sendMessage(new GameEndingUpdateMessage(stats,rankingPos));
    }

    public void onMoveUpdate(TileDTO tile, String nextPlayer){
        sendMessage(new MoveUpdateMessage(tile, nextPlayer));
    }

    private synchronized void sendMessage(Object message) {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            handleDisconnection(this.nickname);
        }
    }


    public void onDrawUpdate(CardDTO c, String nickname){
        sendMessage(new DrawUpdateMessage(c, nickname));
    }

    public void onStatusUpdate(PlayerStatusDTO status){
        sendMessage(new StatusUpdateMessage(status));
    }

    public void onStatsUpdate(PlayerStatsDTO stats, int cardId){
        sendMessage(new StatsUpdateMessage(stats, cardId));
    }

    public void refresh(List<PlayerDTO> listPlayers, BoardDTO board){

    }

    public void onLogin(String nickname){
        this.nickname = nickname;
    }

    public void onPhaseUpdate(PhaseDTO phaseDTO){
        sendMessage(new PhaseUpdateMessage(phaseDTO));
    }
    public void notifySkip(String nickname) {
        sendMessage(new NotifySkipMessage(nickname));
    }

    public void notifyDrawable(ActionsDTO actions) {
        sendMessage(new NotifyDrawableMessage(actions));
    }

    @Override
    public void onClientMessage(ClientMessage m){
        m.accept(visitor);
    }

    public void setVisitor(ClientMessageVisitor visitor){
        this.visitor = visitor;
    }

    public void showBoard(BoardDTO b){
        sendMessage(new ShowBoardMessage(b));
    }

    public void onChangeAge(ChangeAgeDTO age){
        sendMessage(new ChangeAgeUpdateMessage(age));
    }

    public void onEvent(String e){
        sendMessage(new EventMessage(e));
    }

    @Override
    public void handleDisconnection(String nickname) {
        if(connected) {
            connected = false;
            System.err.println("[SOCKET] Client " + nickname + " disconnesso");
            matchManager.disconnect(nickname);
            disconnect();
        }
    }
    private void disconnect(){
        try {
            if(in != null)
                in.close();
            if(out != null)
                out.close();
            if(clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();
        }
        catch(IOException e){
            System.err.println("[SOCKET] Errore durante la disconnessione.");
        }
    }

    public void onLobbiesRequested(Map<Integer, List<LobbyDTO>> lobbies) {
        sendMessage(new AvailableLobbiesMessage(lobbies));
    }

    public void onGameCreated(int gameId) {
        sendMessage(new GameCreatedMessage(gameId));
    }
}
