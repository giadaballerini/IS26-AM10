package it.polimi.ingsw.server;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.database.RankingDAO;
import it.polimi.ingsw.exceptions.*;
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

/**
 * Central server-side coordinator for all game sessions.
 *
 * <p>{@code MatchManager} is the single shared object through which both the
 * RMI and socket layers route every player action. It maintains the full
 * lifecycle of a match:
 * <ol>
 *   <li><b>Login</b> — registers a client and, if the player belonged to a
 *       persisted match, tracks them for reconnection.</li>
 *   <li><b>Lobby</b> — tracks open lobbies and starts the game when one fills
 *       up.</li>
 *   <li><b>In-game</b> — delegates move, draw, and skip actions to the
 *       {@link Controller}, then persists the updated state.</li>
 *   <li><b>Post-game</b> — writes match results to the database, computes
 *       global positions, and notifies clients of the final rankings.</li>
 *   <li><b>Quit / Disconnect</b> — cleans up all server-side states and
 *       notifies affected clients.</li>
 * </ol>
 *
 * <p>All methods that mutate shared state are {@code synchronized} (or
 * synchronize on {@code this} internally) to guarantee thread safety across
 * concurrent RMI and socket threads.
 */
public class MatchManager {

    /** Map from player nickname to their server-side observer (handler). */
    private final Map<String, ModelObserver> clients = new HashMap<>();

    /** Map from match ID to the active {@link Controller} for that match. */
    private final Map<Integer, Controller> controllers = new HashMap<>();

    /** Map from match ID to the open {@link Lobby} waiting for players. */
    private final Map<Integer, Lobby> lobbies = new HashMap<>();

    /** Map from player nickname to the match ID they are currently in or waiting for. */
    private final Map<String, Integer> clientToMatch = new HashMap<>();

    /**
     * Persisted snapshots of matches that crashed and are waiting for all
     * players to reconnect before resuming. Keyed by match ID.
     */
    private final Map<Integer, GameSnapshot> pendingRestorations = new HashMap<>();

    /**
     * For each match pending restoration, tracks which players have already
     * reconnected. Keyed by match ID, then by nickname.
     */
    private final Map<Integer, Map<String, ModelObserver>> reconnectedPlayers = new HashMap<>();

    /**
     * Tracks players who have finished a match and are in the post-game screen.
     * Maps nickname to the number of players in their last match (needed to
     * query the correct leaderboard partition).
     */
    private final Map<String, Integer> postMatch = new HashMap<>();

    /**
     *  Logger for this class.
     */
    private static final Logger LOG = Logger.getLogger(MatchManager.class.getName());

    /** Handles snapshotting of active game states to disk. */
    private final PersistenceManager persistenceManager = new PersistenceManager();

    /** Map from match ID to the active {@link GameManager} for that match. */
    private final Map<Integer, GameManager> gameManagers = new HashMap<>();

    /**
     * Single-threaded scheduler used to delay the {@link #startGame} call
     * slightly after the last player joins, avoiding race conditions when
     * the lobby fills up concurrently.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /** The match ID that will be assigned to the next created lobby. */
    int nextMatchId = 1;

    /**
     * Creates a new {@code MatchManager}, loading any persisted game snapshots
     * from disk and registering a JVM shutdown hook to flush pending saves.
     */
    public MatchManager() {
        loadSnapshotsFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            persistenceManager.shutdown();
            LOG.info("Shutdown hook: salvataggio completato.");
        }));
    }

    /**
     * Loads all persisted game snapshots from the disk at startup. For each
     * snapshot, registers the match as pending restoration and advances
     * {@link #nextMatchId} past it. Also queries the database for the last
     * known match ID to avoid ID collisions after a restart.
     */
    private void loadSnapshotsFromDisk() {
        List<GameSnapshot> snapshots = GameStateDAO.loadAll();
        for (GameSnapshot snap : snapshots) {
            pendingRestorations.put(snap.getMatchId(), snap);
            reconnectedPlayers.put(snap.getMatchId(), new HashMap<>());
            if (snap.getMatchId() >= nextMatchId)
                nextMatchId = snap.getMatchId() + 1;
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

    /**
     * Registers a new client session.
     *
     * <p>If the player's nickname matches one in a pending restoration, they
     * are recorded as reconnected. When all players of that match have
     * reconnected, the match is resumed via {@link #restoreGame}.
     *
     * <p>State mutations are performed inside a {@code synchronized} block;
     * remote calls ({@link ModelObserver#onReconnection} and game restore) are
     * issued after the lock is released to avoid holding the monitor during
     * potentially blocking network I/O.
     *
     * @param nickname the player's nickname
     * @param client   the server-side observer representing this client
     * @throws InvalidUsernameException if the nickname is blank or already in use
     */
    public void login(String nickname, ModelObserver client) throws InvalidUsernameException {
        int reconnMatchId   = -1;
        int matchIdToRestore = -1;
        GameSnapshot snapToRestore   = null;
        Map<String, ModelObserver> reconnectedCopy = null;

        synchronized (this) {
            if (nickname == null || nickname.isBlank())
                throw new InvalidUsernameException("Nickname non può essere vuoto");
            if (clients.containsKey(nickname))
                throw new InvalidUsernameException(
                        "Username già esistente nella lobby, inserire un nuovo username");
            clients.put(nickname, client);
            LOG.info("Login: " + nickname);

            for (Map.Entry<Integer, GameSnapshot> entry : pendingRestorations.entrySet()) {
                int matchId = entry.getKey();
                GameSnapshot snap = entry.getValue();
                boolean belongsToMatch = snap.getPlayers().stream()
                        .anyMatch(p -> p.getNickname().equals(nickname));
                if (belongsToMatch) {
                    LOG.info("Giocatore " + nickname + " riconosciuto per partita " + matchId);
                    reconnectedPlayers.get(matchId).put(nickname, client);
                    clientToMatch.put(nickname, matchId);
                    reconnMatchId = matchId;
                    if (reconnectedPlayers.get(matchId).size() == snap.getNumPlayers()) {
                        matchIdToRestore  = matchId;
                        snapToRestore     = snap;
                        reconnectedCopy   = new HashMap<>(reconnectedPlayers.get(matchId));
                        pendingRestorations.remove(matchId);
                        reconnectedPlayers.remove(matchId);
                    }
                    break;
                }
            }
            LOG.info("Nuovo giocatore connesso: " + nickname);
        }

        if (reconnMatchId != -1)
            client.onReconnection(reconnMatchId);

        if (matchIdToRestore != -1)
            restoreGame(matchIdToRestore, snapToRestore, reconnectedCopy);
    }

    /**
     * Restores a previously persisted match once all its players have
     * reconnected.
     *
     * <p>Rebuilds the {@link RestoredGameManager} and {@link Controller},
     * re-injects the game visitor into each handler, registers the game with
     * the persistence manager, and resumes execution on a new thread.
     *
     * <p>The {@code reconnected} map is captured by the caller inside the
     * critical section and passed here so that remote calls
     * ({@link ModelObserver#injectGameVisitor}) can be made outside any lock.
     *
     * @param matchId     the ID of the match to restore
     * @param snap        the persisted snapshot to restore from
     * @param reconnected map from nickname to observer, in reconnection order
     */
    private void restoreGame(int matchId, GameSnapshot snap,
                             Map<String, ModelObserver> reconnected) {
        LOG.info("Ripristino partita " + matchId + " – tutti i giocatori presenti.");
        List<ModelObserver> orderedObservers = snap.getPlayers().stream()
                .map(p -> reconnected.get(p.getNickname()))
                .collect(Collectors.toList());

        RestoredGameManager gm = new RestoredGameManager(
                snap, orderedObservers, () -> onGameEnded(matchId));
        Controller controller = new Controller(gm, snap.getNumPlayers());

        synchronized (this) {
            controllers.put(matchId, controller);
            gameManagers.put(matchId, gm);
        }

        for (ModelObserver obs : orderedObservers)
            obs.injectGameVisitor(controller);

        persistenceManager.register(matchId, gm);
        new Thread(gm::resume).start();
    }
    /**
     * Creates a new game lobby and assigns a match ID to it.
     *
     * @param nickname   nickname of the player creating the lobby
     * @param numPlayers number of players for the new match
     * @return the unique match ID assigned to the new lobby
     * @throws InvalidTimingException if the player is already in a match or lobby
     */
    public synchronized int createGame(String nickname, int numPlayers)
            throws InvalidTimingException {
        if (clientToMatch.containsKey(nickname))
            throw new InvalidTimingException("Non puoi usare questo comando al momento");
        int currentId = nextMatchId++;
        LOG.info("Creato partita numero " + currentId);
        Lobby newLobby = new Lobby(currentId, numPlayers);
        clientToMatch.put(nickname, currentId);
        newLobby.addPlayer(nickname);
        lobbies.put(currentId, newLobby);
        LOG.info("Creata lobby " + currentId + " da " + nickname);
        return currentId;
    }

    /**
     * Starts a match from a full lobby.
     *
     * <p>Builds the player list, creates the {@link GameManager} and
     * {@link Controller}, removes the lobby, injects the game visitor into
     * each handler, and launches the game loop on a new thread.
     *
     * @param lobby the full lobby from which to start the match
     */
    private void startGame(Lobby lobby) {
        LOG.info("Avvio partita " + lobby.getId());
        List<String> players = lobby.getNicknames();
        List<ModelObserver> observers = new ArrayList<>();
        for (String nickname : players)
            observers.add(clients.get(nickname));

        GameManager gm = new GameManager(
                observers, lobby.buildPlayers(), lobby.getCapacity(),
                () -> onGameEnded(lobby.getId()));
        Controller controller = new Controller(gm, lobby.getCapacity());
        controllers.put(lobby.getId(), controller);
        gameManagers.put(lobby.getId(), gm);
        lobbies.remove(lobby.getId());

        for (ModelObserver obs : observers)
            obs.injectGameVisitor(controller);

        persistenceManager.register(lobby.getId(), gm);
        gm.setOnGameStartedCallback(() -> persistenceManager.saveNow(lobby.getId(), gm));
        new Thread(gm::initGame).start();
    }

    /**
     * Adds a player to an existing lobby and starts the match if it is now
     * full.
     *
     * @param nickname nickname of the joining player
     * @param id       ID of the target lobby
     * @throws InvalidLobbyException  if no lobby with the given ID exists
     * @throws InvalidTimingException if the player is already in a match or lobby
     */
    public synchronized void joinGame(String nickname, int id)
            throws InvalidLobbyException, InvalidTimingException {
        if (clientToMatch.containsKey(nickname))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        if (!lobbies.containsKey(id))
            throw new InvalidLobbyException("Codice lobby non valido!");

        Lobby lobby = lobbies.get(id);
        lobby.addPlayer(nickname);
        clientToMatch.put(nickname, id);

        if (lobby.checkFullLobby()) {
            scheduler.schedule(() -> {
                synchronized (this) {
                    if (lobby.checkFullLobby()) startGame(lobby);
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Processes a tile-placement move for the given player, then persists the
     * updated game state.
     *
     * @param nickname nickname of the moving player
     * @param tileId   ID of the target tile
     * @throws InvalidMoveException   if the move violates game rules
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if a move is not allowed in the current phase
     * @throws OccupiedTileException  if the target tile is already occupied
     * @throws InvalidTimingException if the player is not in an active match
     */
    public void move(String nickname, int tileId)
            throws InvalidMoveException, InvalidPlayerException,
            InvalidPhaseException, OccupiedTileException, InvalidTimingException {
        final int matchId;
        final GameManager gm;
        synchronized (this) {
            if (!clientToMatch.containsKey(nickname)
                    || lobbies.containsKey(clientToMatch.get(nickname)))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            matchId = clientToMatch.get(nickname);
            gm = gameManagers.get(matchId);
            controllers.get(matchId).visit(new MoveMessage(nickname, tileId));
        }
        persistenceManager.saveNow(matchId, gm);
    }

    /**
     * Processes a card-draw action for the given player, then persists the
     * updated game state.
     *
     * @param id       index of the card to draw
     * @param nickname nickname of the drawing player
     * @throws InvalidDrawException   if the draw is not currently allowed
     * @throws InvalidTimingException if the player is not in an active match
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if a draw is not allowed in the current phase
     */
    public void drawCard(int id, String nickname)
            throws InvalidDrawException, InvalidTimingException, InvalidPlayerException, InvalidPhaseException{
        final int matchId;
        final GameManager gm;
        synchronized (this) {
            if (!clientToMatch.containsKey(nickname)
                    || lobbies.containsKey(clientToMatch.get(nickname)))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            matchId = clientToMatch.get(nickname);
            gm = gameManagers.get(matchId);
            controllers.get(matchId).visit(new DrawMessage(id, nickname));
        }
        persistenceManager.saveNow(matchId, gm);
    }

    /**
     * Processes a skip action for the given player, then persists the updated
     * game state.
     *
     * @param nickname nickname of the skipping player
     * @throws InvalidSkipException   if skipping is not allowed at this moment
     * @throws InvalidTimingException if the player is not in an active match
     * @throws InvalidPlayerException if it is not this player's turn
     */
    public void skip(String nickname)
            throws InvalidSkipException, InvalidTimingException, InvalidPlayerException {
        final int matchId;
        final GameManager gm;
        synchronized (this) {
            if (!clientToMatch.containsKey(nickname)
                    || lobbies.containsKey(clientToMatch.get(nickname)))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            matchId = clientToMatch.get(nickname);
            gm = gameManagers.get(matchId);
            controllers.get(matchId).visit(new SkipMessage(nickname));
        }
        persistenceManager.saveNow(matchId, gm);
    }

    /**
     * Returns the currently available lobbies grouped by player capacity.
     *
     * @param nickname nickname of the requesting player
     * @return map from player capacity to the list of open lobbies with that capacity
     * @throws InvalidTimingException if the player is already in a match or lobby
     */
    public synchronized Map<Integer, List<LobbyDTO>> getLobbies(String nickname)
            throws InvalidTimingException {
        if (clientToMatch.containsKey(nickname))
            throw new InvalidTimingException("Non puoi usare questo comando al momento.");
        Map<Integer, List<LobbyDTO>> map = new HashMap<>();
        for (Lobby lobby : lobbies.values()) {
            LobbyDTO dto = new LobbyDTO(lobby.getId(), lobby.getCapacity(), lobby.getNicknames());
            map.computeIfAbsent(lobby.getCapacity(), k -> new ArrayList<>()).add(dto);
        }
        return map;
    }

    /**
     * Returns the global leaderboard for the player's last match configuration.
     *
     * <p>Queries the database for cumulative scores of all players who
     * participated in matches with the same number of players as the
     * requester's last match. Only available after a match has ended
     * ({@link #postMatch} must contain the player).
     *
     * @param nickname nickname of the requesting player
     * @return map from player nickname to their cumulative score
     * @throws InvalidTimingException if the player is not in the post-game state
     */
    public Map<String, Integer> requestRanking(String nickname)
            throws InvalidTimingException {
        final int numPlayers;
        synchronized (this) {
            if (!postMatch.containsKey(nickname))
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            numPlayers = postMatch.get(nickname);
        }
        return RankingDAO.requestRanking(numPlayers);
    }

    /**
     * Voluntarily removes a player from their current match, lobby, or
     * post-game screen and notifies all affected players.
     *
     * <p>Handles four cases: active match, pending reconnection, open lobby,
     * and post-game. In all cases that affect other players, notifies them via
     * {@link ModelObserver#onQuitServer(String)}.
     *
     * @param nickname nickname of the quitting player
     *
     * @throws InvalidTimingException if the player is not in a match, lobby, or post-game
     */
    public void quit(String nickname) throws InvalidTimingException{
        final List<ModelObserver> toNotify = new ArrayList<>();
        final String reason;

        synchronized (this) {
            if (clientToMatch.containsKey(nickname)
                    && !lobbies.containsKey(clientToMatch.get(nickname))) {
                int matchId = clientToMatch.get(nickname);

                if (!controllers.containsKey(matchId)) {
                    clientToMatch.remove(nickname);
                    Map<String, ModelObserver> waiting =
                            reconnectedPlayers.getOrDefault(matchId, new HashMap<>());
                    waiting.remove(nickname);
                    toNotify.addAll(waiting.values());
                    pendingRestorations.remove(matchId);
                    reconnectedPlayers.remove(matchId);
                    persistenceManager.unregister(matchId);
                    reason = "La partita è stata annullata perché " + nickname
                            + " ha abbandonato durante la riconnessione.";
                } else {
                    reason = "La partita è terminata perché " + nickname + " ha abbandonato.";
                    List<String> players = clientToMatch.entrySet().stream()
                            .filter(e -> e.getValue().equals(matchId))
                            .map(Map.Entry::getKey).toList();
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
                int matchId = clientToMatch.get(nickname);
                Lobby lobby = lobbies.get(matchId);
                reason = "La lobby è stata chiusa perché il giocatore " + nickname
                        + " ha abbandonato.";
                for (String player : lobby.getNicknames()) {
                    ModelObserver obs = clients.get(player);
                    if (obs != null && !nickname.equals(player)) toNotify.add(obs);
                    clientToMatch.remove(player);
                }
                lobbies.remove(matchId);

            }
            else if(postMatch.containsKey(nickname)){
                postMatch.remove(nickname);
                reason = null;
            }
            else {
                throw new InvalidTimingException("Non puoi usare questo comando al momento.");
            }
        }
        if(reason != null)
            toNotify.forEach(obs -> obs.onQuitServer(reason));
    }

    /**
     * Handles an involuntary disconnection for the given player.
     *
     * <p>Behaves like {@link #quit} but also removes the player from the
     * {@link #clients} map and does not require them to be in any particular
     * state. Used by both the RMI health check and the socket reader thread.
     *
     * @param nickname nickname of the disconnected player
     */
    public void disconnect(String nickname) {
        final List<ModelObserver> toNotify = new ArrayList<>();
        final String reason;

        synchronized (this) {
            if (clientToMatch.containsKey(nickname)
                    && !lobbies.containsKey(clientToMatch.get(nickname))) {
                int matchId = clientToMatch.get(nickname);

                if (!controllers.containsKey(matchId)) {
                    clientToMatch.remove(nickname);
                    Map<String, ModelObserver> waiting =
                            reconnectedPlayers.getOrDefault(matchId, new HashMap<>());
                    waiting.remove(nickname);
                    toNotify.addAll(waiting.values());
                    pendingRestorations.remove(matchId);
                    reconnectedPlayers.remove(matchId);
                    persistenceManager.unregister(matchId);
                    reason = "La partita è stata annullata perché " + nickname
                            + " si è disconnesso durante la riconnessione.";
                } else {
                    terminateMatch(matchId, nickname,
                            "La partita è terminata perché " + nickname + " si è disconnesso.");
                    reason = null;
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
                reason = "La lobby è stata chiusa perché " + nickname + " si è disconnesso.";
            } else {
                reason = null;
            }
            clients.remove(nickname);
        }

        if (reason != null)
            toNotify.forEach(obs -> obs.onQuitServer(reason));
    }

    /**
     * Terminates an active match, removing all server-side states and notifying
     * every participant except the player who caused the termination.
     *
     * @param matchId  ID of the match to terminate
     * @param nickname nickname of the player who caused the termination
     * @param reason   human-readable reason sent to the remaining players
     */
    private void terminateMatch(int matchId, String nickname, String reason) {
        List<ModelObserver> toNotify;
        synchronized (this) {
            if (!controllers.containsKey(matchId)) return;
            List<String> players = clientToMatch.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(matchId))
                    .map(Map.Entry::getKey).toList();
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
        for (ModelObserver obs : toNotify)
            obs.onQuitServer(reason);
    }

    /**
     * Callback invoked by the {@link GameManager} when a match ends normally.
     *
     * <p>Computes each player's ranking position, inserts match results into
     * the database, moves players to the post-game state, and triggers the
     * game-ending notification.
     *
     * @param matchId the ID of the match that just ended
     */
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
                LOG.warning("Errore inserimento ranking per " + player.getNickname()
                        + ": " + e.getMessage());
            }
        }

        for (Player p : rankingPoints.keySet()) {
            try {
                int globalPos = RankingDAO.reportPlayerPlace(p.getNickname(), numPlayers);
                globalPositions.put(p.getNickname(), globalPos);
            } catch (Exception e) {
                LOG.warning("Errore lettura posizione per " + p.getNickname()
                        + ": " + e.getMessage());
                globalPositions.put(p.getNickname(), -1);
            }
        }

        synchronized (this) {
            for (Player p : rankingPoints.keySet())
                postMatch.put(p.getNickname(), numPlayers);
        }

        for (Player player : rankingPoints.keySet()) {
            ModelObserver handler = clients.get(player.getNickname());
            if (handler != null)
                handler.resetGameVisitor();
        }

        gm.notifyGameEnding(globalPositions);

        LOG.info("Partita " + matchId + " conclusa regolarmente e salvata sul DB.");
    }

}