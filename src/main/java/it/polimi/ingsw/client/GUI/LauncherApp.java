package it.polimi.ingsw.client.GUI;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LauncherApp extends Application {
    public static Stage primaryStage;

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