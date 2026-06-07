package it.polimi.ingsw.client.ui.GUI;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class VillagePanel extends StackPane {

    private final HBox myVillage;
    private final VBox drawer;
    private final VBox content;
    private final Button toggleBtn;

    private boolean expanded = true;

    private static final double DRAWER_PADDING = 10.0;
    private static final double BG_OPACITY     = 0.88;

    public VillagePanel() {
        myVillage = new HBox(10);
        myVillage.setAlignment(Pos.CENTER);

        toggleBtn = new Button("▼  il mio villaggio");
        toggleBtn.getStyleClass().add("village-toggle-btn");
        toggleBtn.setStyle(
                "-fx-background-color: rgba(20,20,20,0.82);" +
                        "-fx-text-fill: #e8d5a3;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8 8 0 0;" +
                        "-fx-padding: 6 20 6 20;"
        );
        toggleBtn.setOnAction(e -> toggleDrawer());

        content = new VBox(myVillage);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(DRAWER_PADDING, DRAWER_PADDING, 6, DRAWER_PADDING));

        drawer = new VBox(0, toggleBtn, content);
        drawer.setAlignment(Pos.BOTTOM_CENTER);
        drawer.setStyle(
                "-fx-background-color: rgba(18,15,12," + BG_OPACITY + ");" +
                        "-fx-background-radius: 12 12 0 0;"
        );
        drawer.setMaxWidth(Double.MAX_VALUE);
        drawer.setPrefWidth(USE_COMPUTED_SIZE);
        drawer.setMaxHeight(Region.USE_PREF_SIZE);

        this.getChildren().add(drawer);
        StackPane.setAlignment(drawer, Pos.BOTTOM_CENTER);

        this.setPickOnBounds(false);
        drawer.setPickOnBounds(false);
        content.setPickOnBounds(false);
        this.setMouseTransparent(false);
    }

    public HBox getMyVillage() {
        return myVillage;
    }

    private void toggleDrawer() {
        expanded = !expanded;

        if (!expanded) {
            // Congela l'altezza corrente prima di chiudere così aggiungere figli non sposta il drawer verso l'alto
            drawer.setPrefHeight(drawer.getHeight());
        } else {
            // Riapre: lascia che il layout calcoli l'altezza liberamente
            drawer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        }

        double hideAmount = drawer.getHeight() - toggleBtn.getHeight();
        double target = expanded ? 0 : hideAmount;

        toggleBtn.setText(expanded ? "▼  il mio villaggio" : "▲  il mio villaggio");

        TranslateTransition tt = new TranslateTransition(Duration.millis(250), drawer);
        tt.setToY(target);
        tt.play();
    }
}