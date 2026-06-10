package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.TileData;
import it.polimi.ingsw.client.data.TileRegistry;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton responsible for loading and caching the front-face images of all
 * game tiles used in the GUI.
 *
 * <p>On startup, {@link #loadTiles()} iterates over every tile ID registered
 * in {@link TileRegistry} and resolves the corresponding PNG resource from
 * {@code /images/tiles/Tile_<id>.png}. Successfully loaded images are stored
 * in a thread-safe map and retrieved at render time via {@link #getFront(int)}.</p>
 *
 * <p>Unlike cards and Q-tiles, each {@link it.polimi.ingsw.client.data.TileData}
 * represents a board position tile (with an optional arrow area and card slot),
 * so only a single face image is needed per tile.</p>
 */
public class TileImagesLoader {
    private static TileImagesLoader instance;

    /** Thread-safe cache mapping each tile ID to its loaded front-face {@link Image}. */
    private final Map<Integer, Image> idToFront = new ConcurrentHashMap<>();

    /**
     * Returns the singleton instance of {@code TileImagesLoader},
     * creating it if it does not yet exist.
     *
     * @return the shared {@code TileImagesLoader} instance
     */
    public static synchronized TileImagesLoader getInstance() {
        if (instance == null) {
            instance = new TileImagesLoader();
        }
        return instance;
    }

    /**
     * Loads the front-face image for every tile registered in {@link TileRegistry}.
     *
     * <p>This method should be called once during GUI initialisation, before any
     * scene that renders board tiles is displayed. Missing or unreadable resources
     * are silently skipped (an error is logged to {@code stderr}), so the game can
     * still run with partially loaded assets.</p>
     */
    public void loadTiles(){
        for(Integer id : TileRegistry.getIds()){

            TileData data = TileRegistry.getTile(id);

            loadFront(id);
        }
        System.out.println("Tutte le Tile sono state create!");
    }

    /**
     * Resolves and caches the front-face image for the tile with the given ID.
     *
     * <p>The image is expected at the classpath resource path
     * {@code /images/tiles/Tile_<id>.png}. If the resource cannot be found or
     * loaded, the entry is simply omitted from the cache.</p>
     *
     * @param id the unique tile ID as defined in {@link TileRegistry}
     */
    private void loadFront(int id){
        String path = String.format("/images/tiles/Tile_%d.png", id);

        Image img = loadImage(path);
        if(img != null){
            idToFront.put(id, img);
        }
    }

    /**
     * Loads a JavaFX {@link Image} from the given classpath resource path.
     *
     * <p>The image is loaded synchronously ({@code backgroundLoading = false})
     * to ensure it is fully available before being stored in the cache.
     * If the resource is not found or any exception occurs during loading,
     * the error is printed to {@code stderr} and {@code null} is returned.</p>
     *
     * @param path the absolute classpath resource path (e.g.
     *             {@code /images/tiles/Tile_3.png})
     * @return the loaded {@link Image}, or {@code null} if loading failed
     */
    private Image loadImage(String path){
        try{
            var resource = getClass().getResource(path);
            if(resource == null){
                throw new Exception("[TileImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(),false);
        } catch (Exception e){
            System.err.println("[TileImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the cached front-face image for the tile with the given ID.
     *
     * @param id the unique tile ID as defined in {@link TileRegistry}
     * @return the front-face {@link Image}, or {@code null} if the image was
     *         not loaded (e.g. missing resource)
     */
    public Image getFront(int id){
        return idToFront.get(id);
    }
}