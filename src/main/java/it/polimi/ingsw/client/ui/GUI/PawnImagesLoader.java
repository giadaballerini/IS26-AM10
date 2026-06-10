package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton responsible for loading and caching the pawn images used in the GUI.
 * Each pawn image corresponds to a {@link ColorPawnEnum} value and is loaded
 * from the classpath at startup. Images are shared across all components
 * that need to display player pawns on the board.
 */
public class PawnImagesLoader {

    private static PawnImagesLoader instance;

    /**
     * Cache mapping each pawn color to its corresponding {@link Image}.
     * Uses a {@link ConcurrentHashMap} to support concurrent access during loading.
     */
    private final Map<ColorPawnEnum, Image> colorToTotem = new ConcurrentHashMap<>();

    /**
     * Returns the singleton instance of this loader, creating it if necessary.
     *
     * @return the singleton {@link PawnImagesLoader} instance
     */
    public static synchronized PawnImagesLoader getInstance() {
        if (instance == null) {
            instance = new PawnImagesLoader();
        }
        return instance;
    }

    /**
     * Loads and caches the pawn image for every {@link ColorPawnEnum} value.
     * Should be called once at application startup before any pawn is displayed.
     */
    public void loadPawns() {
        for (ColorPawnEnum color : ColorPawnEnum.values()) {
            Image img = loadPawn(color);
            if (img != null) {
                colorToTotem.put(color, img);
            }
        }
        System.out.println("Tutti i pawn  sono stati creati!");
    }

    /**
     * Loads the pawn image for the given color from the classpath.
     *
     * @param color the pawn color to load the image for
     * @return the loaded {@link Image}, or {@code null} if the resource was not found
     */
    private Image loadPawn(ColorPawnEnum color) {
        String name = switch (color) {
            case BLUE   -> "blue";
            case YELLOW -> "yellow";
            case WHITE  -> "white";
            case ORANGE -> "orange";
            default     -> "purple";
        };
        String path = String.format("/images/pawns/pawn_%s.png", name);
        return loadImage(path);
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
                throw new Exception("[PawnImagesLoader]Resource not found: " + path);
            }
            return new Image(resource.toExternalForm(), false);
        } catch (Exception e) {
            System.err.println("[PawnImagesLoader] Fallito: " + path + " → " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the cached pawn image for the given color.
     *
     * @param color the pawn color whose image is requested
     * @return the corresponding pawn {@link Image}, or {@code null} if not loaded
     */
    public Image getTotemImage(ColorPawnEnum color) {
        return colorToTotem.get(color);
    }
}