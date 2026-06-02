package it.polimi.ingsw.client.GUI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private Label loadingLabel;

    private Timeline dotsAnim;

    @FXML
    public void initialize() {
        String[] dots = {".", "..", "..."};
        int[] i = {0};
        dotsAnim = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    i[0] = (i[0] + 1) % dots.length;
                    loadingLabel.setText("Caricamento risorse" + dots[i[0]]);
                })
        );
        dotsAnim.setCycleCount(Timeline.INDEFINITE);
        dotsAnim.play();
    }

    public void stopAnimation() {
        if (dotsAnim != null) dotsAnim.stop();
    }
}