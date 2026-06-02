package it.polimi.ingsw.client.GUI;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private final Stage primaryStage;
    private final Map<String, Scene> scenes = new HashMap<>();
    public SceneManager(Stage stage){
        this.primaryStage = stage;
    }
    public void register(String name, Scene scene){
        scenes.put(name, scene);
    }
    public void switchTo(String name){
        Scene scene = scenes.get(name);
        if(scene != null){
            primaryStage.setScene(scene);
        }
    }
}
