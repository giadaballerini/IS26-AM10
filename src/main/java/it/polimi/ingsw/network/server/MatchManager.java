package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.database.RankingDAO;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.model.gamemanager.RestoredGameManager;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.messages.client.DrawMessage;
import it.polimi.ingsw.network.messages.client.MoveMessage;
import it.polimi.ingsw.network.messages.client.SkipMessage;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;
import it.polimi.ingsw.persistency.GameStateDAO;
import it.polimi.ingsw.persistency.PersistenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MatchManager {
    private Map<String, ModelObserver> clients = new HashMap<>();
    private Map<Integer, Controller> controllers = new HashMap<>();
    private Map<Integer, Lobby> lobbies = new HashMap<>();
    private Map<String, Integer> clientToMatch = new HashMap<>();
    private final Map<Integer, GameSnapshot> pendingRestorations = new HashMap<>();
    private final Map<Integer, Map<String, ModelObserver>> reconnectedPlayers = new HashMap<>();
    private final Map<String, Integer> postMatch = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(MatchManager.class.getName());
    private final PersistenceManager persistenceManager = new PersistenceManager();
    private final Map<Integer,GameManager> gameManagers = new HashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    int nextMatchId = 1;

    public MatchManager() {
        loadSnapshotsFromDisk();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            persistenceManager.shutdown();
            LOG.info("Shutdown hook: salvataggio completato.");
        }));

    }

    private void loadSnapshotsFromDisk() {
        List<GameSnapshot> snapshots = GameStateDAO.loadAll();
        for (GameSnapshot snap : snapshots) {
            pendingRestorations.put(snap.getMatchId(), snap);
            reconnectedPlayers.put(snap.getMatchId(), new HashMap<>());

            if (snap.getMatchId() >= nextMatchId) {
                nextMatchId = snap.getMatchId() + 1;
            }

            LOG.info("Partita " + snap.getMatchId() + " in attesa di "
                    + snap.getNumPlayers() + " giocatori per riprendersi.");
        }
        try {
            int fromDb = RankingDAO.requestLastId() + 1;
            if (fromDb > nextMatchId) {
                nextMatchId = fromDb;
                LOG.info("nextMatchId aggiornato dal DB: " + nextMatchId);
            }
        } catch (Exception e) {
            LOG.warning("DB non raggiungibile al boot, nextMatchId calcolato solo dai file: " + nextMatchId);
        }
    }


    public void login(String nickname, ModelObserver client)
            throws AlreadyExistingUsernameException, InvalidTimingException {
        if (clients.containsKey(nickname)) {
            throw new AlreadyExistingUsernameException(
                    "Username già esistente nella lobby, inserire un nuovo username");
        }
        clients.put(nickname, client);
        LOG.info("Login: " + nickname);
        int matchIdToRestore = -1;
        GameSnapshot snapToRestore = null;
        synchronized (this) {
            for (Map.Entry<Integer, GameSnapshot> entry : pendingRestorations.entrySet()) {
                int matchId = entry.getKey();
                GameSnapshot snap = entry.getValue();

                boolean belongsToMatch = snap.getPlayers().stream()
                        .anyMatch(p -> p.getNickname().equals(nickname));

                if (belongsToMatch) {
                    LOG.info("Giocatore " + nickname + " riconosciuto per partita " + matchId);
                    reconnectedPlayers.get(matchId).put(nickname, client);
                    clientToMatch.put(nickname, matchId);

                    client.onReconnection(matchId);

                    if (reconnectedPlayers.get(matchId).size() == snap.getNumPlayers()) {
                        matchIdToRestore = matchId;
                        snapToRestore = snap;
                    }
                    break;
                }
            }
            LOG.info("Nuovo giocatore connesso: " + nickname);
        }

        if(matchIdToRestore != -1) {
            restoreGame(matchIdToRestore,  snapToRestore);
        }

    }

    private void restoreGame(int matchId, GameSnapshot snap) {
        LOG.info("Ripristino partita " + matchId + " – tutti i giocatori presenti.");

        Map<String, ModelObserver> reconnected = reconnectedPlayers.get(matchId);

        List<ModelObserver> orderedObservers = snap.getPlayers().stream()
                .map(p -> reconnected.get(p.getNickname()))
                .collect(Collectors.toList());

        RestoredGameManager gm = new RestoredGameManager(snap, orderedObservers, ()-> onGameEnded(matchId));

        Controller controller = new Controller(gm, snap.getNumPlayers());
        controllers.put(matchId, controller);
        gameManagers.put(matchId, gm);

        pendingRestorations.remove(matchId);
        reconnectedPlayers.remove(matchId);

        for (ModelObserver obs : orderedObservers) {
            obs.injectGameVisitor(controller);
        }

        persistenceManager.register(matchId, gm);

        new Thread(gm::resume).start();
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
        LOG.info("Creata lobby " + currentId + " da " + nickname);
        return currentId;
    }

    private void startGame(Lobby lobby) {
        LOG.info("Avvio partita " + lobby.getId());
        List<String> players = lobby.getNicknames();
        List<ModelObserver> observers = new ArrayList<>();
        for (String nickname : players) {
            observers.add(clients.get(nickname));
        }

        GameManager gm = new GameManager(observers, lobby.buildPlayers(), lobby.getCapacity(), ()-> onGameEnded(lobby.getId()));
        Controller controller = new Controller(gm, lobby.getCapacity());
        controllers.put(lobby.getId(), controller);
        gameManagers.put(lobby.getId(), gm);
        lobbies.remove(lobby.getId());
        for (ModelObserver obs : observers) {
            obs.injectGameVisitor(controller);
        }
        persistenceManager.register(lobby.getId(), gm);
        gm.setOnGameStartedCallback(() -> persistenceManager.saveNow(lobby.getId(), gm));
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

    public void move(String nickname, int tileId) throws InvalidMoveException, InvalidPlayerException, InvalidPhaseException, OccupiedTileException {
        final int matchId;
        final GameManager gm;
        synchronized (this) {
            if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            matchId = clientToMatch.get(nickname);
            gm = gameManagers.get(matchId);
            controllers.get(matchId).visit(new MoveMessage(nickname, tileId));
        }
        persistenceManager.saveNow(matchId, gm);
    }

    public void drawCard(int id, String nickname) throws InvalidDrawException, InvalidTimingException {
        final int matchId;
        final GameManager gm;
        synchronized (this) {
            if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            matchId = clientToMatch.get(nickname);
            gm = gameManagers.get(matchId);
            System.out.println("Carta " + id + " pescata da " + nickname + " nella partita " + matchId);
            controllers.get(matchId).visit(new DrawMessage(id, nickname));
        }
        persistenceManager.saveNow(matchId, gm);
    }

    public void skip(String nickname) throws InvalidSkipException, InvalidTimingException {
        final int matchId;
        final GameManager gm;
        synchronized (this) {
            if (!clientToMatch.containsKey(nickname) || lobbies.containsKey(clientToMatch.get(nickname)))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            matchId = clientToMatch.get(nickname);
            gm = gameManagers.get(matchId);
            System.out.println("Giocatore " + nickname + " ha skippato nella partita " + matchId);
            controllers.get(matchId).visit(new SkipMessage(nickname));
        }
        persistenceManager.saveNow(matchId, gm);
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

    public Map<String, Integer> requestRanking(String nickname) throws InvalidTimingException {
        final int numPlayers;
        synchronized (this) {
            if (!postMatch.containsKey(nickname))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            numPlayers = postMatch.get(nickname);
        }
        return RankingDAO.requestRanking(nickname, numPlayers); // DB fuori dal lock
    }

    public void quit(String nickname) {
        final List<ModelObserver> toNotify = new ArrayList<>();
        final String reason;

        synchronized (this) {
            if (clientToMatch.containsKey(nickname) && !lobbies.containsKey(clientToMatch.get(nickname))) {
                int matchId = clientToMatch.get(nickname);

                if (!controllers.containsKey(matchId)) {
                    // riconnessione parziale: partita in attesa
                    clientToMatch.remove(nickname);
                    Map<String, ModelObserver> waiting = reconnectedPlayers.getOrDefault(matchId, new HashMap<>());
                    waiting.remove(nickname);
                    toNotify.addAll(waiting.values());
                    pendingRestorations.remove(matchId);
                    reconnectedPlayers.remove(matchId);
                    persistenceManager.unregister(matchId);
                    reason = "La partita è stata annullata perché " + nickname + " ha abbandonato durante la riconnessione.";

                } else {
                    // partita in corso normale
                    reason = "La partita è terminata perché " + nickname + " ha abbandonato.";

                    List<String> players = clientToMatch.entrySet().stream()
                            .filter(e -> e.getValue().equals(matchId))
                            .map(Map.Entry::getKey)
                            .toList();

                    for (String player : players) {
                        ModelObserver obs = clients.get(player);
                        if (obs != null && !nickname.equals(player)) toNotify.add(obs);
                        clientToMatch.remove(player);
                    }

                    controllers.remove(matchId);
                    gameManagers.remove(matchId);
                    persistenceManager.unregister(matchId);
                    LOG.info("Partita " + matchId + " terminata (causa: " + nickname + ").");
                }

            } else if (clientToMatch.containsKey(nickname)) {
                // giocatore in lobby
                int matchId = clientToMatch.get(nickname);
                Lobby lobby = lobbies.get(matchId);
                reason = "La lobby è stata chiusa perché il giocatore " + nickname + " ha abbandonato.";

                for (String player : lobby.getNicknames()) {
                    ModelObserver obs = clients.get(player);
                    if (obs != null && !nickname.equals(player)) toNotify.add(obs);
                    clientToMatch.remove(player);
                }
                lobbies.remove(matchId);

            } else if (postMatch.containsKey(nickname)) {
                // giocatore in postMatch
                postMatch.remove(nickname);
                ModelObserver obs = clients.get(nickname);
                if (obs != null) toNotify.add(obs);
                reason = "Sei tornato al menu.";

            } else {
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            }
        }

        toNotify.forEach(obs -> obs.onQuitServer(reason));
    }

    public void disconnect(String nickname) {
        final List<ModelObserver> toNotify = new ArrayList<>();
        final String lobbyReason = "La lobby è stata chiusa perché il giocatore " + nickname + " ha abbandonato.";

        synchronized (this) {
            if (clientToMatch.containsKey(nickname) && !lobbies.containsKey(clientToMatch.get(nickname))) {
                int matchId = clientToMatch.get(nickname);

                if (!controllers.containsKey(matchId)) {
                    clientToMatch.remove(nickname);
                    Map<String, ModelObserver> waiting = reconnectedPlayers.getOrDefault(matchId, new HashMap<>());
                    waiting.remove(nickname);
                    toNotify.addAll(waiting.values());
                    pendingRestorations.remove(matchId);
                    reconnectedPlayers.remove(matchId);
                    persistenceManager.unregister(matchId);
                } else {
                    terminateMatch(matchId, nickname, "La partita è terminata perché " + nickname + " si è disconnesso.");
                }

            } else if (clientToMatch.containsKey(nickname)) {
                int matchId = clientToMatch.get(nickname);
                Lobby lobby = lobbies.get(matchId);
                if (lobby != null) {
                    for (String player : lobby.getNicknames()) {
                        ModelObserver obs = clients.get(player);
                        if (obs != null && !nickname.equals(player)) toNotify.add(obs);
                        clientToMatch.remove(player);
                    }
                    lobbies.remove(matchId);
                }
            }
            clients.remove(nickname);
        }

        toNotify.forEach(obs -> obs.onQuitServer(lobbyReason));
    }

    private void terminateMatch(int matchId, String nickname, String reason) {
        List<ModelObserver> toNotify;

        synchronized (this) {
            if (!controllers.containsKey(matchId)) return;

            List<String> players = clientToMatch.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(matchId))
                    .map(Map.Entry::getKey)
                    .toList();

            toNotify = new ArrayList<>();
            for (String player : players) {
                ModelObserver obs = clients.get(player);
                if (obs != null && !nickname.equals(player)) toNotify.add(obs);
                clientToMatch.remove(player);
            }
            controllers.remove(matchId);
            gameManagers.remove(matchId);
            persistenceManager.unregister(matchId);
            LOG.info("Partita " + matchId + " terminata (causa: " + nickname + ").");
        }

        for (ModelObserver obs : toNotify) {
            obs.onQuitServer(reason);
        }
    }

    private void onGameEnded(int matchId) {
        final Map<Player, Integer> rankingPoints;
        final int numPlayers;
        final GameManager gm;

        synchronized (this) {
            gm = gameManagers.get(matchId);
            if (gm == null) return;

            rankingPoints = gm.calculateRankingPoints();
            numPlayers = controllers.get(matchId).getNumPlayers();

            controllers.remove(matchId);
            gameManagers.remove(matchId);
            clientToMatch.entrySet().removeIf(e -> e.getValue().equals(matchId));
            persistenceManager.unregister(matchId);
        }

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        Map<String, Integer> globalPositions = new HashMap<>();

        for (Map.Entry<Player, Integer> entry : rankingPoints.entrySet()) {
            Player player = entry.getKey();
            int score = entry.getValue();
            try {
                RankingDAO.insertMatch(matchId, player.getNickname(), score, numPlayers, today);
            } catch (Exception e) {
                LOG.warning("Errore inserimento ranking per " + player.getNickname() + ": " + e.getMessage());
            }
        }

        for (Player p : rankingPoints.keySet()) {
            try {
                int globalPos = RankingDAO.reportPlayerPlace(p.getNickname(), numPlayers);
                globalPositions.put(p.getNickname(), globalPos);
            } catch (Exception e) {
                LOG.warning("Errore lettura posizione per " + p.getNickname() + ": " + e.getMessage());
                globalPositions.put(p.getNickname(), -1);
            }
        }

        synchronized (this) {
            for (Player p : rankingPoints.keySet()) {
                postMatch.put(p.getNickname(), numPlayers);
            }
        }

        gm.notifyGameEnding(globalPositions);
        LOG.info("Partita " + matchId + " conclusa regolarmente e salvata sul DB.");
    }


}