package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.CardData;
import it.polimi.ingsw.client.data.CardRegistry;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton responsible for loading and caching all card images used in the GUI.
 * Front images are cached by card ID, while back images are cached by their
 * resource key (derived from the card's type and age), so that cards sharing
 * the same back image reuse the same {@link Image} instance.
 *
 * @see CardData#getBackImagePath()
 */
public class CardImagesLoader {

    /**
     * Cache mapping each card ID to its front {@link Image}.
     * Uses a {@link ConcurrentHashMap} to support concurrent access during loading.
     */
    private final Map<Integer, Image> idToFront = new ConcurrentHashMap<>();

    /**
     * Cache mapping each back image resource key to its {@link Image}.
     * Multiple cards with the same type and age share the same back image instance.
     */
    private final Map<String, Image> stringToBack = new ConcurrentHashMap<>();

    private static CardImagesLoader instance;

    /**
     * Returns the singleton instance of this loader, creating it if necessary.
     *
     * @return the singleton {@link CardImagesLoader} instance
     */
    public static synchronized CardImagesLoader getInstance() {
        if (instance == null) {
            instance = new CardImagesLoader();
        }
        return instance;
    }

    /**
     * Derives the back image resource key for the given card,
     * based on its type and age.
     *
     * @param data the card data to derive the key from
     * @return the resource key identifying the appropriate back image
     */
    private String generateBackKey(CardData data) {
        return data.getBackImagePath();
    }

    /**
     * Loads an image from the given classpath resource path.
     *
     * @param path the classpath path of the image resource
     * @return the loaded {@link Image}, or {@code null} if the resource was not found
     */
    private Image loadImage(String path) {
        try {
            var resource = getClass().getResource(path);
            if (resource == null) {
                throw new Exception("[CardImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(), false);
        } catch (Exception e) {
            System.err.println("[CardImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    /**
     * Loads the front image for the card with the given ID and stores it in the cache.
     *
     * @param id the unique identifier of the card whose front image is to be loaded
     */
    private void loadFront(int id) {
        String path = String.format("/images/cards/Card_%03d.png", id);
        Image img = loadImage(path);
        if (img != null) {
            idToFront.put(id, img);
        }
    }

    /**
     * Loads the back image identified by the given resource key and stores it in the cache.
     *
     * @param key the resource key identifying the back image to load
     */
    private void loadBack(String key) {
        String path = String.format("/images/cards/%s.png", key);
        Image img = loadImage(path);
        if (img != null) {
            stringToBack.put(key, img);
        }
    }

    /**
     * Loads all card images (front and back) for every card registered in {@link CardRegistry}.
     * Each front image is loaded individually by card ID; back images are loaded only once
     * per unique resource key and shared across cards with the same type and age.
     * Should be called once at application startup before any card is displayed.
     */
    public void loadAll() {
        for (int id : CardRegistry.getIds()) {
            CardData data = CardRegistry.getCard(id);
            loadFront(id);
            String key = generateBackKey(data);
            if (!stringToBack.containsKey(key)) {
                loadBack(key);
            }
        }
        System.out.println("Tutte le carte sono state caricate.");
    }

    /**
     * Returns the cached front image for the card with the given ID.
     *
     * @param id the unique identifier of the card
     * @return the front {@link Image} of the card, or {@code null} if not loaded
     */
    public Image getFront(int id) {
        return idToFront.get(id);
    }

    /**
     * Returns the cached back image for the card with the given ID,
     * looked up by the card's back image resource key.
     *
     * @param id the unique identifier of the card
     * @return the back {@link Image} of the card, or {@code null} if the card is not found
     *         or its back image has not been loaded
     */
    public Image getBack(int id) {
        CardData data = CardRegistry.getCard(id);
        if (data == null) { return null; }
        return stringToBack.get(generateBackKey(data));
    }
}