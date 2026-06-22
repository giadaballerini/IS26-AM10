package it.polimi.ingsw.client.ui.gui;

import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages named root {@link Parent} nodes for the application's primary {@link Stage}.
 *
 * <p>Acts as a central registry for all JavaFX views used in the GUI
 * (e.g. splash screen, main menu, game board, leaderboard), allowing
 * other controllers such as {@link MainMenuController} and
 * {@link GameController} to switch between them by name without holding
 * direct references to the {@link Stage}.</p>
 *
 * <p>Instead of swapping the entire {@link javafx.scene.Scene}, this manager
 * keeps a single permanent scene on the stage and swaps only the root
 * {@link Parent} node. This avoids fullscreen glitches and resize flickers
 * that occur when replacing the scene entirely.</p>
 *
 * <p>Roots must be registered via {@link #register(String, Parent)} before
 * they can be activated with {@link #switchTo(String)}.</p>
 */
public class SceneManager {

    /** The application's primary window, whose scene root is swapped on each transition. */
    private final Stage primaryStage;

    /** Maps view names to their corresponding root {@link Parent} nodes. */
    private final Map<String, Parent> roots = new HashMap<>();

    /**
     * Constructs a {@code SceneManager} bound to the given primary stage.
     *
     * @param stage the application's primary {@link Stage} whose active scene root
     *              will be controlled by this manager
     */
    public SceneManager(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Registers a root {@link Parent} node under the given name.
     *
     * <p>If a root is already registered under the same name, it is replaced.
     * Names should match the constants used by the GUI controllers when calling
     * {@link #switchTo(String)}.</p>
     *
     * @param name the unique identifier for the view (e.g. {@code "game"},
     *             {@code "menu"}, {@code "leaderboard"})
     * @param root the root {@link Parent} node to associate with that name
     */
    public void register(String name, Parent root) {
        roots.put(name, root);
    }

    /**
     * Switches the primary stage to the root registered under the given name
     * by replacing the current scene's root node.
     *
     * <p>If no root is registered under {@code name}, the call is a no-op
     * and the current scene remains unchanged.</p>
     *
     * @param name the name of the view to display, as previously passed
     *             to {@link #register(String, Parent)}
     */
    public void switchTo(String name) {
        Parent root = roots.get(name);
        if (root != null) {
            primaryStage.getScene().setRoot(root);
        }
    }
}