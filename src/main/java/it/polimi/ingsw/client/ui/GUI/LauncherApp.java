package it.polimi.ingsw.client.ui.GUI;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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
 *       {@link ViewGUI}, and replaces the scene with {@code MainMenu.fxml}.</li>
 * </ol>
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
     * Initializes and shows the application window.
     *
     * <p>The method executes the following steps in order:</p>
     * <ol>
     *   <li>Stores the stage reference in {@link #primaryStage} and sets the
     *       application icon ({@code mesos_icon_256.png}).</li>
     *   <li>Attaches a scene-change listener that applies the custom base
     *       cursor and hover cursor to every new {@link Scene} via
     *       {@link CursorManager}.</li>
     *   <li>Pre-loads the {@code Mesos.ttf} font so it is available to all
     *       subsequent FXML stylesheets.</li>
     *   <li>Loads and displays the splash screen ({@code Splash.fxml}) at
     *       1920×1080, setting the window title to {@code "Mesos"}.</li>
     *   <li>Starts a daemon background thread that calls the four
     *       {@code *ImagesLoader} singletons to pre-cache all game imagery.</li>
     *   <li>On success, stops the splash animation, builds the
     *       {@link SceneManager} and {@link ViewGUI}, loads
     *       {@code MainMenu.fxml}, wires the {@link MainMenuController}, and
     *       switches the primary stage to the main-menu scene.</li>
     *   <li>On failure, stops the splash animation; any exception is printed
     *       to standard error.</li>
     * </ol>
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws Exception if the splash FXML cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/mesos_icon_256.png")));

        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                CursorManager.applyBaseCursor(newScene);
                CursorManager.applyHoverToScene(newScene);
            }
        });

        Font f = Font.loadFont(getClass().getResourceAsStream("/fonts/Mesos.ttf"), 12);

        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("/fxml/Splash.fxml"));
        Parent splashRoot = splashLoader.load();
        SplashController splashController = splashLoader.getController();

        stage.setScene(new Scene(splashRoot, 1920, 1080));
        stage.setTitle("Mesos");
        stage.show();

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
                Scene menuScene = new Scene(menu, 1920, 1080);
                sceneManager.register("menu", menuScene);
                viewGUI.setMenucontroller(menuCtrl);
                stage.setScene(menuScene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        loadTask.setOnFailed(e ->
                splashController.stopAnimation()
        );

        Thread t = new Thread(loadTask, "resource-loader");
        t.setDaemon(true);
        t.start();
    }
}