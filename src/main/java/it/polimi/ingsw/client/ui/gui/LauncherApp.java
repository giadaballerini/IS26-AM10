package it.polimi.ingsw.client.ui.gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

/**
 * JavaFX entry point for the Mesos GUI client.
 *
 * <p>{@code LauncherApp} extends {@link Application} and is responsible for
 * bootstrapping the entire graphical front-end. Its {@link #start(Stage)}
 * method performs three sequential phases:</p>
 * <ol>
 *   <li><strong>Splash screen</strong> — loads and displays
 *       {@code Splash.fxml} immediately so the user sees feedback while
 *       heavy resources are loading.</li>
 *   <li><strong>Background resource loading</strong> — runs all
 *       {@code *ImagesLoader} singletons ({@link CardImagesLoader},
 *       {@link TileImagesLoader}, {@link QTileImagesLoader},
 *       {@link PawnImagesLoader}) on a daemon thread named
 *       {@code "resource-loader"}, keeping the FX Application Thread free.</li>
 *   <li><strong>Main menu transition</strong> — once loading succeeds,
 *       stops the splash animation, constructs the {@link SceneManager} and
 *       {@link ViewGUI}, and replaces the scene root with {@code MainMenu.fxml}.</li>
 * </ol>
 *
 * <p>A single {@link Scene} is created at startup and kept for the entire
 * application lifetime. View transitions swap only the root {@link Parent}
 * node via {@link SceneManager#switchTo(String)}, avoiding fullscreen
 * glitches and resize flickers.</p>
 *
 * <p>The static field {@link #primaryStage} is stored at startup so that
 * other GUI components (e.g. {@link GameController}) can access the root
 * {@link Stage} without passing it through the call stack.</p>
 */
public class LauncherApp extends Application {

    /**
     * The application's primary {@link Stage}, stored at startup for global
     * access by other GUI components.
     *
     * <p><strong>Note:</strong> this field is written once on the FX
     * Application Thread inside {@link #start(Stage)} and should only be read
     * after that method has returned.</p>
     */
    public static Stage primaryStage;

    /**
     * Stops the application by terminating the JVM.
     * <p>
     * This method is called by the JavaFX runtime when the application
     * is shutting down (e.g., after the primary stage is closed).
     */
    @Override
    public void stop() {
        System.exit(0);
    }

    /**
     * Initializes and shows the application window.
     *
     * <p>Creates a single permanent {@link Scene} with the splash screen as
     * its initial root. All subsequent view transitions swap only the root
     * node via {@link SceneManager}, so the scene — and therefore the
     * fullscreen state — is never replaced.</p>
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws Exception if the splash FXML cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/mesos_icon_256.png")));

        Font.loadFont(getClass().getResourceAsStream("/fonts/Mesos.ttf"), 12);

        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("/fxml/Splash.fxml"));
        Parent splashRoot = splashLoader.load();
        SplashController splashController = splashLoader.getController();

        Scene permanentScene = new Scene(splashRoot);
        permanentScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                CursorManager.applyBaseCursor(newScene);
                CursorManager.applyHoverToScene(newScene);
            }
        });

        stage.setScene(permanentScene);
        stage.setTitle("Mesos");
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.fullScreenProperty().addListener((obs, wasFullScreen, isNowFullScreen) -> {
            if (!isNowFullScreen) {
                stage.setFullScreen(true);
            }
        });
        stage.show();

        CursorManager.applyBaseCursor(permanentScene);
        CursorManager.applyHoverToScene(permanentScene);

        Task<Void> loadTask = new Task<>() {
            /**
             * Executes the resource-loading phase on the background thread.
             *
             * <p>Calls each image-loader singleton in sequence:
             * {@link CardImagesLoader}, {@link TileImagesLoader},
             * {@link QTileImagesLoader}, and {@link PawnImagesLoader}.
             * All loaders populate their internal caches so that subsequent
             * scene construction on the FX thread incurs no additional I/O.</p>
             *
             * @return {@code null} (result unused)
             */
            @Override
            protected Void call() {
                CardImagesLoader.getInstance().loadAll();
                TileImagesLoader.getInstance().loadTiles();
                QTileImagesLoader.getInstance().loadQTiles();
                PawnImagesLoader.getInstance().loadPawns();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            splashController.stopAnimation();
            try {
                SceneManager sceneManager = new SceneManager(stage);
                ViewGUI viewGUI = new ViewGUI();

                FXMLLoader menuLoader = new FXMLLoader(
                        getClass().getResource("/fxml/MainMenu.fxml"));
                Parent menu = menuLoader.load();
                MainMenuController menuCtrl = menuLoader.getController();
                menuCtrl.setSceneManager(sceneManager);
                menuCtrl.setViewGUI(viewGUI);

                sceneManager.register("menu", menu);
                viewGUI.setMenuController(menuCtrl);

                sceneManager.switchTo("menu");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        loadTask.setOnFailed(e -> splashController.stopAnimation());

        Thread t = new Thread(loadTask, "resource-loader");
        t.setDaemon(true);
        t.start();
    }
    /**
     * Creates a new {@code LauncherApp} instance.
     * Called by the JavaFX runtime via reflection before {@link #start(javafx.stage.Stage)}.
     */
    public LauncherApp() {
    }
}