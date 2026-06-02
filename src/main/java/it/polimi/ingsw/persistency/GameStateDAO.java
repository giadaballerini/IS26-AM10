package it.polimi.ingsw.persistency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameStateDAO {
    private static final Logger LOG = Logger.getLogger(GameStateDAO.class.getName());
    private static final String SAVE_DIR = "saves";
    private static final String EXT= ".json";
    private static final String TMP_EXT = ".json.tmp";

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        // Rende il JSON leggibile (utile per debugging)
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);

        MAPPER.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                LOG.info("Directory di salvataggio creata: " + dir.getAbsolutePath());
            } else {
                LOG.severe("Impossibile creare la directory di salvataggio: " + dir.getAbsolutePath());
            }
        }
    }

    public static synchronized boolean save(GameSnapshot snapshot) {
        File tmpFile = fileFor(snapshot.getMatchId(), TMP_EXT);
        File destFile = fileFor(snapshot.getMatchId(), EXT);

        try {
            MAPPER.writeValue(tmpFile, snapshot);
            Files.move(tmpFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            LOG.fine("Snapshot salvato: " + destFile.getName());
            return true;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Errore nel salvataggio del match " + snapshot.getMatchId(), e);
            tmpFile.delete();
            return false;
        }
    }

    public static synchronized GameSnapshot load(int matchId) {
        File file = fileFor(matchId, EXT);
        if (!file.exists()) {
            return null;
        }
        try {
            GameSnapshot snapshot = MAPPER.readValue(file, GameSnapshot.class);
            LOG.info("Snapshot caricato per match " + matchId);
            return snapshot;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Impossibile leggere il salvataggio per match " + matchId, e);
            return null;
        }
    }

    public static synchronized List<GameSnapshot> loadAll() {
        List<GameSnapshot> result = new ArrayList<>();
        File dir = new File(SAVE_DIR);

        if (!dir.exists() || !dir.isDirectory()) {
            return result;
        }

        File[] files = dir.listFiles(
                (d, name) -> name.startsWith("match_") && name.endsWith(EXT));

        if (files == null) return result;

        for (File file : files) {
            try {
                GameSnapshot snapshot = MAPPER.readValue(file, GameSnapshot.class);
                result.add(snapshot);
                LOG.info("Snapshot trovato al boot: match " + snapshot.getMatchId()
                        + " (" + snapshot.getNumPlayers() + " giocatori)");
            } catch (IOException e) {
                LOG.log(Level.WARNING,
                        "File di salvataggio ignorato (corrotto?): " + file.getName(), e);
            }
        }
        return result;
    }

    public static synchronized void delete(int matchId) {
        File file = fileFor(matchId, EXT);
        if (file.exists() && !file.delete()) {
            LOG.warning("Impossibile eliminare il salvataggio per match " + matchId);
        } else {
            LOG.fine("Salvataggio eliminato per match " + matchId);
        }
        fileFor(matchId, TMP_EXT).delete();
    }

    private static File fileFor(int matchId, String extension) {
        return new File(SAVE_DIR + File.separator + "match_" + matchId + extension);
    }

}
