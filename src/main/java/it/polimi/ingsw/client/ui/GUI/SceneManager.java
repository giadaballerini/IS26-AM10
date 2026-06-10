package it.polimi.ingsw.client.ui.GUI;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages named {@link Scene} instances for the application's primary {@link Stage}.
 *
 * <p>Acts as a central registry for all JavaFX scenes used in the GUI
 * (e.g. splash screen, main menu, game board, leaderboard), allowing
 * other controllers such as {@link MainMenuController} and
 * {@link GameController} to switch between them by name without holding
 * direct references to the {@link Stage}.</p>
 *
 * <p>Scenes must be registered via {@link #register(String, Scene)} before
 * they can be activated with {@link #switchTo(String)}.</p>
 */
public class SceneManager {

    /** The application's primary window, whose scene is swapped on each transition. */
    private final Stage primaryStage;

    /** Maps scene names to their corresponding {@link Scene} instances. */
    private final Map<String, Scene> scenes = new HashMap<>();

    /**
     * Constructs a {@code SceneManager} bound to the given primary stage.
     *
     * @param stage the application's primary {@link Stage} whose active scene
     *              will be controlled by this manager
     */
    public SceneManager(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Registers a {@link Scene} under the given name.
     *
     * <p>If a scene is already registered under the same name, it is replaced.
     * Names should match the constants used by the GUI controllers when calling
     * {@link #switchTo(String)}.</p>
     *
     * @param name  the unique identifier for the scene (e.g. {@code "game"},
     *              {@code "menu"}, {@code "leaderboard"})
     * @param scene the {@link Scene} to associate with that name
     */
    public void register(String name, Scene scene) {
        scenes.put(name, scene);
    }

    /**
     * Switches the primary stage to the scene registered under the given name.
     *
     * <p>If no scene is registered under {@code name}, the call is a no-op
     * and the current scene remains unchanged.</p>
     *
     * @param name the name of the scene to display, as previously passed
     *             to {@link #register(String, Scene)}
     */
    public void switchTo(String name) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            primaryStage.setScene(scene);
        }
    }
}