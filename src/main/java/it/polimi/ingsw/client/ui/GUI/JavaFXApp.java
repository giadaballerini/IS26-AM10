package it.polimi.ingsw.client.ui.GUI;

import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXApp extends Application {
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MESOS");
        primaryStage.setHeight(800);
        primaryStage.setWidth(1000);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        loginScreen.show();
        primaryStage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            if (isFullScreen) {
                primaryStage.setFullScreenExitHint("");
            }
        });
    }
}
