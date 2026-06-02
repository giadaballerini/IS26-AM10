package it.polimi.ingsw.client.GUI;

import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXApp extends Application {
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MESOS");
        primaryStage.setHeight(800);
        primaryStage.setWidth(1000);
        primaryStage.setResizable(false);
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        loginScreen.show();
    }
}
