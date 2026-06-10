package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.QTileRegistry;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton image loader for Queue Tile front-face images.
 *
 * <p>Loads and caches {@link Image} instances for all Queue Tiles registered
 * in {@link QTileRegistry}, sourcing them from the classpath under
 * {@code /images/qtiles/}. The cache is backed by a {@link ConcurrentHashMap}
 * to support safe access from multiple threads during initialization.</p>
 *
 * <p>Must be initialized once at application startup via {@link #loadQTiles()}
 * before any {@link QTileGUI} attempts to retrieve images through
 * {@link #getFront(int)}.</p>
 *
 * @see CardImagesLoader
 * @see TileImagesLoader
 */
public class QTileImagesLoader {

    /** The single instance of this loader. */
    private static QTileImagesLoader instance;

    /**
     * Cache mapping each Queue Tile ID to its loaded front-face {@link Image}.
     * Uses {@link ConcurrentHashMap} to tolerate concurrent reads after loading.
     */
    private final Map<Integer, Image> idToFront = new ConcurrentHashMap<>();

    /** Private constructor — use {@link #getInstance()} to obtain the singleton. */
    private QTileImagesLoader() {}

    /**
     * Returns the singleton instance of {@code QTileImagesLoader},
     * creating it on the first call.
     *
     * @return the shared {@code QTileImagesLoader} instance
     */
    public static synchronized QTileImagesLoader getInstance() {
        if (instance == null) {
            instance = new QTileImagesLoader();
        }
        return instance;
    }

    /**
     * Loads the front-face images for all Queue Tiles known to {@link QTileRegistry}.
     *
     * <p>Should be called once during the GUI initialization phase, before the
     * game board is rendered. Tiles whose image resources cannot be found are
     * skipped silently (an error is logged to {@code stderr}).</p>
     */
    public void loadQTiles() {
        for (Integer id : QTileRegistry.getIds()) {
            loadFront(id);
        }
        System.out.println("Tutte le Tile sono state create!");
    }

    /**
     * Loads the front-face image for a single Queue Tile and stores it in the cache.
     *
     * <p>The image is expected at the classpath path
     * {@code /images/qtiles/QTiles_<id>.jpg}. If the resource is not found
     * or loading fails, the entry is not added to the cache and an error
     * is printed to {@code stderr}.</p>
     *
     * @param id the Queue Tile ID whose image should be loaded
     */
    private void loadFront(int id) {
        String path = String.format("/images/qtiles/QTiles_%d.jpg", id);
        Image img = loadImage(path);
        if (img != null) {
            idToFront.put(id, img);
        }
    }

    /**
     * Resolves and loads a JavaFX {@link Image} from the given classpath path.
     *
     * <p>Loading is performed synchronously ({@code backgroundLoading = false})
     * to ensure the image is ready before it is handed to a {@link QTileGUI}.
     * Returns {@code null} if the resource cannot be located.</p>
     *
     * @param path the absolute classpath path to the image resource
     *             (e.g. {@code /images/qtiles/QTiles_3.jpg})
     * @return the loaded {@link Image}, or {@code null} if the resource was not found
     */
    private Image loadImage(String path) {
        try {
            var resource = getClass().getResource(path);
            if (resource == null) {
                throw new Exception("[QTileImagesLoader] Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(), false);
        } catch (Exception e) {
            System.err.println("[QTileImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the cached front-face image for the given Queue Tile ID.
     *
     * <p>Returns {@code null} if {@link #loadQTiles()} has not been called yet
     * or if loading failed for the requested ID.</p>
     *
     * @param id the Queue Tile ID
     * @return the front-face {@link Image} for the tile, or {@code null} if unavailable
     */
    public Image getFront(int id) {
        return idToFront.get(id);
    }
}