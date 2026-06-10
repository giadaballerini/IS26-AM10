package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static registry that holds all board tile data loaded from the client-side JSON resource file.
 * The registry is populated once at class initialization and provides lookup methods
 * to retrieve tile attributes by tile ID.
 *
 * @see QTileData
 */
public class QTileRegistry {

    /** Map from tile ID to its corresponding {@link QTileData}, populated at class initialization. */
    private static final Map<Integer, QTileData> reg = new HashMap<>();

    static {
        loadTiles();
    }

    /**
     * Loads all tile data from the {@code /json/clientQTiles.json} resource file
     * and populates the registry. Called once during class initialization.
     *
     * @throws RuntimeException if the resource file cannot be found
     */
    private static void loadTiles() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = QTileRegistry.class.getResourceAsStream("/json/clientQTiles.json")) {

            if (is == null) {
                throw new RuntimeException("Risorsa clientQtiles.json non trovata");
            }
            List<QTileData> tiles = mapper.readValue(is, new TypeReference<List<QTileData>>() {});

            for (QTileData tile : tiles) {
                reg.put(tile.getId(), tile);
            }
            System.out.println("[JACKSON] QTiles caricate con successo!");

        } catch (Exception e) {
            System.out.println("[ERRORE CRITICO] Fallimento nel caricamento qtiles.json");
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link QTileData} associated with the given tile ID.
     *
     * @param id the unique identifier of the tile
     * @return the corresponding {@link QTileData}, or {@code null} if not found
     */
    public static QTileData getTile(int id) {
        return reg.get(id);
    }

    /**
     * Returns the set of all tile IDs currently loaded in the registry.
     *
     * @return a {@link Set} containing all registered tile IDs
     */
    public static Set<Integer> getIds() {
        return reg.keySet();
    }

    /**
     * Returns the display name for board tiles.
     *
     * @return the string {@code "Queue Tile "}
     */
    public static String getName() {
        return "Queue Tile ";
    }

    /**
     * Returns the display description of the tile with the given ID.
     *
     * @param id the unique identifier of the tile
     * @return the tile's description as defined in its {@link QTileData}
     */
    public static String getDescription(int id) {
        return getTile(id).getDescription();
    }
}