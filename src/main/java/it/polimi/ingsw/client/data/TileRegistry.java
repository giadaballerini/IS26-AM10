package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static registry that holds all board tile data loaded from the client-side JSON resource file.
 * The registry is populated once at class initialization and preserves insertion order,
 * reflecting the order in which tiles are defined in the JSON file.
 *
 * @see TileData
 */
public class TileRegistry {

    /**
     * Map from tile ID to its corresponding {@link TileData}, populated at class initialization.
     * Uses a {@link LinkedHashMap} to preserve the insertion order of tiles as defined in the JSON file.
     */
    private static final Map<Integer, TileData> reg = new LinkedHashMap<>();

    /**
     * Logger used to report failures during the tile data loading in the static initializer block.
     */
    private static final Logger LOG = Logger.getLogger(TileRegistry.class.getName());

    static {
        loadTiles();
    }

    /**
     * Loads all tile data from the {@code /json/clientTiles.json} resource file
     * and populates the registry. Called once during class initialization.
     *
     * @throws RuntimeException if the resource file cannot be found
     */
    private static void loadTiles() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = TileRegistry.class.getResourceAsStream("/json/clientTiles.json")) {

            if (is == null) {
                throw new RuntimeException("Risorsa clientTiles.json non trovata");
            }
            List<TileData> tiles = mapper.readValue(is, new TypeReference<>() {
            });

            for (TileData tile : tiles) {
                reg.put(tile.getId(), tile);
            }


        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Fallimento nel caricamento tiles.json", e);
        }
    }

    /**
     * Returns the {@link TileData} associated with the given tile ID.
     *
     * @param id the unique identifier of the tile
     * @return the corresponding {@link TileData}, or {@code null} if not found
     */
    public static TileData getTile(int id) {
        return reg.get(id);
    }

    /**
     * Returns the set of all tile IDs currently loaded in the registry,
     * in the order they were defined in the JSON file.
     *
     * @return a {@link Set} containing all registered tile IDs in insertion order
     */
    public static Set<Integer> getIds() {
        return reg.keySet();
    }

    /**
     * Returns the display name for board tiles.
     *
     * @return the string {@code "Tessera Offerta "}
     */
    public static String getName() {
        return "Tessera Offerta ";
    }

    /**
     * Returns the display description of the tile with the given ID.
     *
     * @param id the unique identifier of the tile
     * @return the tile's description as defined in its {@link TileData}
     */
    public static String getDescription(int id) {
        return getTile(id).getDescription();
    }
    /**
     * Private constructor to prevent instantiation of this static utility class.
     */
    private TileRegistry() {
    }
}