package it.polimi.ingsw.client.GUI;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginScreen {
    private final Stage stage;
    private Scene scene;

    public LoginScreen(Stage stage) {
        this.stage = stage;
        initializeUI();
    }
    private void initializeUI() {
        StackPane root = new StackPane();
        ImageView backgroundImage = createBackGroundImage();
        root.getChildren().add(backgroundImage);
        VBox loginForm = new VBox();
        root.getChildren().add(loginForm);
        scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
    }

    private ImageView createBackGroundImage() {
        try{
            Image img = new Image("file:src/main/resources/images/background.pdf");
            ImageView view = new ImageView(img);
            view.setFitWidth(1000);
            view.setFitHeight(800);
            view.setPreserveRatio(false);
            return view;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public void show() {
        stage.show();
    }


}
