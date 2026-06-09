package it.polimi.ingsw.persistency;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.gamemanager.GameManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the persistence lifecycle of active matches.
 *
 * <p>Keeps track of all running {@link GameManager} instances and provides
 * methods to save and delete their state on demand. On shutdown, a final
 * save is performed for every match still in progress so that games can be
 * resumed after a server restart.</p>
 *
 * <p>Persistence is delegated to {@link GameStateDAO}, which handles the
 * actual reading and writing of JSON save files.</p>
 */
public class PersistenceManager {

    private static final Logger LOG = Logger.getLogger(PersistenceManager.class.getName());

    /** Active matches, keyed by match ID. */
    private final Map<Integer, GameManager> activeGames = new ConcurrentHashMap<>();

    /** Scheduled periodic save tasks, keyed by match ID. */
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * Single-threaded scheduler used to run periodic save tasks.
     * The backing thread is a daemon so it does not prevent JVM shutdown.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "persistence-scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * Registers a match so that its state can be saved by this manager.
     *
     * @param matchId     unique identifier of the match
     * @param gameManager the {@link GameManager} instance handling the match
     */
    public synchronized void register(int matchId, GameManager gameManager) {
        activeGames.put(matchId, gameManager);
    }

    /**
     * Unregisters a completed match and deletes its save file.
     *
     * <p>Should be called when a match ends normally so that its persisted
     * state is cleaned up and it is no longer tracked by this manager.</p>
     *
     * @param matchId unique identifier of the match to unregister
     */
    public synchronized void unregister(int matchId) {
        activeGames.remove(matchId);
        GameStateDAO.delete(matchId);
        LOG.info("Salvataggio rimosso per match " + matchId + " (partita conclusa)");
    }

    /**
     * Immediately saves the current state of the given match.
     *
     * <p>If the match is in the {@link GamePhaseEnum#END_GAME} phase, its save
     * file is deleted instead of being updated, since a finished game does not
     * need to be resumed.</p>
     *
     * @param matchId     unique identifier of the match
     * @param gameManager the {@link GameManager} instance to snapshot
     */
    public void saveNow(int matchId, GameManager gameManager) {
        GameSnapshot snapshot = buildSnapshot(matchId, gameManager);

        if (snapshot.getCurrentPhase() == GamePhaseEnum.END_GAME) {
            GameStateDAO.delete(matchId);
            return;
        }

        try {
            boolean ok = GameStateDAO.save(snapshot);
            if (!ok) {
                LOG.warning("Salvataggio fallito per match " + matchId);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Errore durante il salvataggio del match " + matchId, e);
        }
    }

    /**
     * Builds a {@link GameSnapshot} representing the current state of the given match.
     *
     * @param matchId     unique identifier of the match
     * @param gameManager the {@link GameManager} instance to snapshot
     * @return a snapshot of the current game state; never {@code null}
     */
    public GameSnapshot buildSnapshot(int matchId, GameManager gameManager) {
        return gameManager.toSnapshot(matchId);
    }

    /**
     * Saves all active matches and shuts down the scheduler.
     *
     * <p>Should be called when the server is shutting down to ensure that no
     * in-progress match is lost. After this method returns, the manager should
     * not be used further.</p>
     */
    public synchronized void shutdown() {
        LOG.info("Shutdown PersistenceManager – salvataggio finale di tutte le partite...");
        for (Map.Entry<Integer, GameManager> entry : activeGames.entrySet()) {
            saveNow(entry.getKey(), entry.getValue());
        }
        scheduler.shutdownNow();
    }
}