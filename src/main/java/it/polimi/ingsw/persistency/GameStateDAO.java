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

/**
 * Data Access Object for persisting and retrieving {@link GameSnapshot} instances.
 *
 * <p>Snapshots are stored as pretty-printed JSON files under the {@value #SAVE_DIR}
 * directory, one file per match, named {@code match_<matchId>.json}.
 * All public methods are {@code synchronized} to be safe for concurrent access
 * from multiple threads.</p>
 *
 * <p>Writes use a write-to-temp-then-atomic-move strategy to prevent corrupt
 * save files in the event of a crash mid-write.</p>
 */
public class GameStateDAO {

    private static final Logger LOG = Logger.getLogger(GameStateDAO.class.getName());

    /** Directory where save files are stored. */
    private static final String SAVE_DIR = "saves";

    /** File extension for committed save files. */
    private static final String EXT = ".json";

    /** File extension for temporary files used during atomic writes. */
    private static final String TMP_EXT = ".json.tmp";

    /**
     * Shared Jackson mapper configured with pretty-print output and
     * lenient deserialization (unknown properties are ignored).
     */
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
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

    /**
     * Persists a {@link GameSnapshot} to disk using an atomic write.
     *
     * <p>The snapshot is first written to a temporary file ({@code .json.tmp}),
     * then atomically moved to the final destination ({@code .json}),
     * replacing any previously saved state for the same match.
     * The temporary file is deleted if an error occurs.</p>
     *
     * @param snapshot the game snapshot to save; must not be {@code null}
     * @return {@code true} if the snapshot was saved successfully;
     *         {@code false} if an I/O error occurred
     */
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

    /**
     * Loads the saved {@link GameSnapshot} for the given match ID.
     *
     * @param matchId the unique identifier of the match to load
     * @return the deserialized snapshot, or {@code null} if no save file exists
     *         or the file cannot be read
     */
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

    /**
     * Loads all saved {@link GameSnapshot} instances found in the save directory.
     *
     * <p>Intended for use at server startup to resume interrupted matches.
     * Files that cannot be deserialized (e.g. corrupted) are logged and skipped;
     * they do not prevent other snapshots from being loaded.</p>
     *
     * @return a list of all successfully loaded snapshots; never {@code null},
     *         may be empty if no valid save files are found
     */
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

    /**
     * Deletes the save file for the given match ID, if it exists.
     *
     * <p>Any leftover temporary file for the same match is also removed.
     * Failures to delete are logged but do not throw an exception.</p>
     *
     * @param matchId the unique identifier of the match whose save file should be deleted
     */
    public static synchronized void delete(int matchId) {
        File file = fileFor(matchId, EXT);
        if (file.exists() && !file.delete()) {
            LOG.warning("Impossibile eliminare il salvataggio per match " + matchId);
        } else {
            LOG.fine("Salvataggio eliminato per match " + matchId);
        }
        fileFor(matchId, TMP_EXT).delete();
    }

    /**
     * Builds the {@link File} handle for a given match ID and file extension.
     *
     * @param matchId   the unique identifier of the match
     * @param extension the file extension to use (e.g. {@value #EXT} or {@value #TMP_EXT})
     * @return the corresponding {@link File} within the save directory
     */
    private static File fileFor(int matchId, String extension) {
        return new File(SAVE_DIR + File.separator + "match_" + matchId + extension);
    }
}