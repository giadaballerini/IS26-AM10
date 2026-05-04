package it.polimi.ingsw.network.client.rmi;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.TUI.ViewTUI;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.server.VirtualServerRmi;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ClientRmi extends Client implements VirtualViewRmi{

    private final VirtualServerRmi serverStub;
    private String nickname;
    private VirtualModel vm;
    private final UserInterface ui;
    private int matchId;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 1099;
    private boolean lobbiesAvailable = false;

    public ClientRmi() throws RemoteException, NotBoundException {
        this.matchId = 0;

        Registry registry = LocateRegistry.getRegistry(SERVER_IP, PORT);
        this.serverStub = (VirtualServerRmi) registry.lookup("GameServer");
        UnicastRemoteObject.exportObject(this, 0);

        vm = new VirtualModel();
        ui = new ViewTUI(vm, this);
    }
    public boolean login(String nickname){
        try {
            serverStub.login(nickname, this);
            this.nickname = nickname;
            this.vm.setNickname(nickname);
            ui.onLogin(this.nickname);
            return true;
        }catch (RemoteException | AlreadyExistingUsernameException | InvalidTimingException e){
           ui.printError(e);
           return false;
        }
    }

    public void createGame(String nickname, int numPlayers){
        try {
            matchId = serverStub.createGame(nickname, numPlayers);
            onCreate();
        } catch(RemoteException | InvalidTimingException e){
            ui.printError(e);
        }
    }


    public void joinGame(String nickname, int id){
        try {
            serverStub.joinGame(nickname, id);
            matchId = id;
            onJoin();
        } catch(RemoteException | InvalidLobbyException | InvalidTimingException e){
            ui.printError(e);
        }
    }

    public void onCreate(){
        ui.onCreate(matchId);
    }

    public void onJoin(){
        ui.onJoin(matchId);
    }

    public Map<String, Integer> requestRanking() { // TODO chiamata e funzionamento
        try {
            return serverStub.requestRanking(nickname);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
            return Collections.emptyMap();
        }
    }

    public void requestJoin(){
        Map<Integer, List<LobbyDTO>> lobbies = askLobbies();
        lobbiesAvailable = lobbies != null && lobbies.values().stream().anyMatch(l -> !l.isEmpty());
        ui.displayLobbies(lobbies);
    }

    public boolean hasAvailableLobbies(){
        return lobbiesAvailable;
    }

    private Map<Integer, List<LobbyDTO>> askLobbies(){
        try {
            return serverStub.getLobbies(nickname);
        } catch (RemoteException | InvalidTimingException e) {
            ui.printError(e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void info(int cardId){
        if(this.isInGame())
            ui.info(cardId);
        else
            ui.printError(new InvalidTimingException("Non è possibile richiedere informazioni prima che la partita sia iniziata"));
    }

    public void move(int tileId){
        try {
            serverStub.move(nickname, tileId);
        }catch(RemoteException | InvalidMoveException | InvalidPhaseException | InvalidPlayerException | OccupiedTileException | InvalidTimingException e){
            ui.printError(e);
        }
    }

    public void draw(int card){
        try {
            serverStub.draw(card, nickname);
        }catch(RemoteException | InvalidDrawException | InvalidTimingException e){
            ui.printError(e);
        }
    }

    public  void skip(){
        try {
            serverStub.skip(nickname);
        }catch(RemoteException | InvalidSkipException | InvalidTimingException e){
            System.out.println(e.getMessage());
        }
    }

    public void quit(){
        try{
            serverStub.quit(nickname);
        } catch (RemoteException | InvalidTimingException e){
           ui.printError(e);
        }
    }

    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) throws RemoteException{
        ui.showLeaderboard(ranks);
    }

    public void onMoveUpdate(TileDTO tile, String currPlayer) throws RemoteException{
        vm.onMoveUpdate(tile);
        ui.onMoveUpdate(tile, currPlayer);
        ui.showBoard();
    }

    public void onCurrPlayerUpdate(String nickname) throws RemoteException {
        vm.onCurrPlayerUpdate(nickname);
        ui.onCurrPlayerUpdate(nickname);
    }

    public void onPhaseUpdate(PhaseDTO phaseDTO) throws RemoteException{
        vm.onPhaseUpdate(phaseDTO);
        ui.onPhaseUpdate(phaseDTO);
    }

    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos) throws RemoteException{
        ui.onGameEnding(stats, rankingPos);
    }

    public void onDrawUpdate(CardDTO c, String nickname) throws RemoteException {
        vm.onDrawUpdate(c, nickname);
        ui.onDrawUpdate(c, nickname);
        ui.showBoard();
    }

    public void onStatusUpdate(PlayerStatusDTO status) throws RemoteException{
        vm.onStatusUpdate(status);
        ui.onStatusUpdate(status);
    }

    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) throws RemoteException{
        vm.onStatsUpdate(stats);
        ui.onStatsUpdate(stats);
    }

    public void refresh(List<PlayerDTO> listPlayers, BoardDTO board) throws RemoteException{
        vm.update(board);
        ui.showBoard();
    }

    public void notifySkip(String nickname) throws RemoteException {
        vm.skip();
        ui.notifySkip(nickname);
    }

    public void notifyDrawable(ActionsDTO actions) throws RemoteException{
        vm.updateToDoActions(actions);
        ui.showDrawable();
    }

    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) throws RemoteException{
        vm.onReturnToQueue(tileDTO, playerStatsDTO);
        ui.onReturnToQueue(tileDTO, playerStatsDTO);
        ui.showBoard();
    }

    public void onChangeAge(ChangeAgeDTO dto) throws RemoteException{
        vm.onChangeAge(dto);
        ui.onChangeAge(dto.getAge());
        ui.showBoard();
    }

    @Override
    public void onEvent(String event) throws RemoteException {
        ui.onEvent(event);
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public void showBoard(BoardDTO board) throws RemoteException{
        this.vm.update(board);
        this.ui.showBoard();
    }

    public void start(){
        ui.start();
    }

    public void printError(String e) throws RemoteException{
        vm = ui.quit(); // inserito vm = poichè da aggiornare il modello che è da reinizializzare, reso non più final dentro alla classe
        ui.printError(new Exception(e));
    }

    @Override
    public void ping() throws RemoteException {

    }

    public boolean isInGame(){
        return matchId != 0;
    }

    public void exit(){
        try{
            serverStub.handleDisconnection(nickname);
        }catch (RemoteException e){
            ui.printError(e);
        }

        vm = ui.quit();
        ui.exit();
    }

    public void help(){
        ui.displayHelpMessage();
    }
}
