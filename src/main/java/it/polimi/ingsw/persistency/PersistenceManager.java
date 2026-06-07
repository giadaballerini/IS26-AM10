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

public class PersistenceManager {
    private static final Logger LOG = Logger.getLogger(PersistenceManager.class.getName());

    private final Map<Integer, GameManager> activeGames  = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "persistence-scheduler");
                t.setDaemon(true);
                return t;
            });


    public synchronized void register(int matchId, GameManager gameManager) {
        activeGames.put(matchId, gameManager);
    }
        public synchronized void unregister(int matchId) {
        activeGames.remove(matchId);
        GameStateDAO.delete(matchId);
        LOG.info("Salvataggio rimosso per match " + matchId + " (partita conclusa)");
    }

    public void saveNow(int matchId, GameManager gameManager) {
        GameSnapshot snapshot = buildSnapshot(matchId, gameManager);

        if(snapshot.getCurrentPhase() == GamePhaseEnum.END_GAME){
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

    public GameSnapshot buildSnapshot(int matchId, GameManager gameManager) {
        return gameManager.toSnapshot(matchId);
    }

    public synchronized void shutdown() {
        LOG.info("Shutdown PersistenceManager – salvataggio finale di tutte le partite...");
        for (Map.Entry<Integer, GameManager> entry : activeGames.entrySet()) {
            saveNow(entry.getKey(), entry.getValue());
        }
        scheduler.shutdownNow();
    }

}
