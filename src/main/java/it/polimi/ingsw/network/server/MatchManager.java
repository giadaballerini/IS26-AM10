package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.model.persistence.RankingDAO;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.messages.client.DrawMessage;
import it.polimi.ingsw.network.messages.client.MoveMessage;
import it.polimi.ingsw.network.messages.client.SkipMessage;
import it.polimi.ingsw.observer.ModelObserver;   // ← cambiato da client.VirtualView

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MatchManager {
    private Map<String, ModelObserver> clients = new HashMap<>();
    private Map<Integer, Controller> controllers = new HashMap<>();
    private Map<Integer, Lobby> lobbies = new HashMap<>();
    private Map<String, Integer> clientToMatch = new HashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    //int nextMatchId = RankingDAO.requestLastId() + 1;
    int nextMatchId = 1;

    public synchronized void login(String nickname, ModelObserver client)
            throws AlreadyExistingUsernameException, InvalidTimingException {
        if (clients.containsKey(nickname)) {
            throw new AlreadyExistingUsernameException(
                    "Username già esistente nella lobby, inserire un nuovo username");
        }
        clients.put(nickname, client);
        System.out.println("Ciao, lista partite presenti " + lobbies);
    }

    public synchronized int createGame(String nickname, int numPlayers)
            throws InvalidTimingException {
        if (clientToMatch.containsKey(nickname)) {
            throw new InvalidTimingException("Non puoi usare questo comando al momento");
        }
        int currentId = nextMatchId;
        System.out.println("Creato partita numero " + nextMatchId);
        nextMatchId++;
        Lobby newLobby = new Lobby(currentId, numPlayers);
        clientToMatch.put(nickname, currentId);
        newLobby.addPlayer(nickname);
        lobbies.put(currentId, newLobby);
        return currentId;
    }

    private void startGame(Lobby lobby) {
        List<String> players = lobby.getNicknames();
        List<ModelObserver> observers = new ArrayList<>();
        for (String nickname : players) {
            observers.add(clients.get(nickname));
        }
        GameManager gm = new GameManager(observers, lobby.buildPlayers(), lobby.getCapacity());
        Controller controller = new Controller(gm, lobby.getCapacity());
        controllers.put(lobby.getId(), controller);
        for (ModelObserver obs : observers) {
            obs.setVisitor(controller);
        }
        lobbies.remove(lobby.getId());
        new Thread(gm::initGame).start();
    }

    public synchronized void joinGame(String nickname, int id)
            throws InvalidLobbyException, InvalidSkipException, InvalidTimingException {
        if (clientToMatch.containsKey(nickname))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        if (!lobbies.containsKey(id)) {
            throw new InvalidLobbyException("Codice lobby non valido!");
        }
        Lobby lobby = lobbies.get(id);
        lobby.addPlayer(nickname);
        clientToMatch.put(nickname, id);

        if (lobby.checkFullLobby()) {
            scheduler.schedule(() -> {
                synchronized (this) {
                    if (lobby.checkFullLobby()) {
                        startGame(lobby);
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    public synchronized void move(String nickname, int tileId)
            throws InvalidMoveException, InvalidPlayerException, InvalidPhaseException, OccupiedTileException {
        if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        int matchId = clientToMatch.get(nickname);
        System.out.println("Tile selezionata " + tileId + " nella partita " + matchId);
        MoveMessage m = new MoveMessage(nickname, tileId);
        controllers.get(matchId).visit(m);
    }

    public synchronized void drawCard(int id, String nickname) throws InvalidDrawException, InvalidTimingException {
        if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        int matchId = clientToMatch.get(nickname);
        System.out.println("Carta " + id + " pescata da " + nickname + " nella partita " + matchId);
        DrawMessage m = new DrawMessage(id, nickname);
        controllers.get(matchId).visit(m);
    }

    public synchronized void skip(String nickname) throws InvalidSkipException, InvalidTimingException {
        if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        int matchId = clientToMatch.get(nickname);
        System.out.println("Giocatore " + nickname + " ha skippato nella partita " + matchId);
        SkipMessage m = new SkipMessage(nickname);
        controllers.get(matchId).visit(m);
    }

    public synchronized Map<Integer, List<LobbyDTO>> getLobbies(String nickname) throws InvalidTimingException {
        if (clientToMatch.containsKey(nickname))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        Map<Integer, List<LobbyDTO>> map = new HashMap<>();
        for (Lobby lobby : lobbies.values()) {
            LobbyDTO dto = new LobbyDTO(lobby.getId(), lobby.getCapacity(), lobby.getNicknames());
            map.computeIfAbsent(lobby.getCapacity(), k -> new ArrayList<>()).add(dto);
        }
        return map;
    }

    public synchronized Map<String, Integer> requestRanking(String nickname) {
        if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        int matchId = clientToMatch.get(nickname);
        return RankingDAO.requestRanking(nickname, controllers.get(matchId).getNumPlayers());
    }

    public synchronized void quit(String nickname) {

        if (clientToMatch.containsKey(nickname) && !lobbies.containsKey(clientToMatch.get(nickname))) { // giocatore in partita
            int matchId = clientToMatch.get(nickname);
            List<String> players = clientToMatch.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(matchId))
                    .map(Map.Entry::getKey)
                    .toList();
            for (String player : players) {
                ModelObserver obs = clients.get(player);
                if(obs != null && !nickname.equals(player)){
                    obs.onErrorMessage("La partita è terminata in modo anomalo perchè il giocatore " + nickname + " ha abbandonato la partita.");
                }
                else if(obs != null){
                    obs.onErrorMessage("Ti sei disconnesso dalla partita.");
                }
                clientToMatch.remove(player);
            }
            controllers.remove(matchId);
            System.out.println("Giocatore " + nickname + " rimosso dalla partita.");
        }
        else if(clientToMatch.containsKey(nickname)){
            int matchId = clientToMatch.get(nickname);
            Lobby lobby = lobbies.get(matchId);
            for (String player : lobby.getNicknames()) {
                ModelObserver obs = clients.get(player);
                if(obs != null && !nickname.equals(player)){
                    obs.onErrorMessage("La lobby è stata chiusa perchè il giocatore " + nickname + " ha abbandonato.");
                }
                else if(obs != null){
                    obs.onErrorMessage("Ti sei disconnesso dalla lobby.");
                }
                clientToMatch.remove(player);
            }
            lobbies.remove(matchId);
            System.out.println("Giocatore " + nickname + " rimosso dalla lobby.");
        }
        else
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");

    }

    public synchronized void disconnect(String nickname){
        if(clientToMatch.containsKey(nickname) && !lobbies.containsKey(clientToMatch.get(nickname))){ // giocatore in partita
            int matchId = clientToMatch.get(nickname);
            List<String> players = clientToMatch.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(matchId))
                    .map(Map.Entry::getKey)
                    .toList();
            for (String player : players) {
                ModelObserver obs = clients.get(player);
                if(obs != null && !nickname.equals(player)){
                    obs.onErrorMessage("La partita è terminata in modo anomalo perchè il giocatore " + nickname + " ha abbandonato la partita.");
                }
                else if(obs != null){
                    obs.onErrorMessage("Ti sei disconnesso dalla partita.");
                }
                clientToMatch.remove(player);
            }
            controllers.remove(matchId);
            System.out.println("Giocatore " + nickname + " rimosso dalla partita.");
        }
        else if(clientToMatch.containsKey(nickname)){
            int matchId = clientToMatch.get(nickname);
            Lobby lobby = lobbies.get(matchId);
            for (String player : lobby.getNicknames()) {
                ModelObserver obs = clients.get(player);
                if(obs != null && !nickname.equals(player)){
                    obs.onErrorMessage("La lobby è stata chiusa perchè il giocatore " + nickname + " ha abbandonato.");
                }
                else if(obs != null){
                    obs.onErrorMessage("Ti sei disconnesso dalla lobby.");
                }
                clientToMatch.remove(player);
            }
            lobbies.remove(matchId);
            System.out.println("Giocatore " + nickname + " rimosso dalla lobby.");
        }
        clients.remove(nickname);
    }
}