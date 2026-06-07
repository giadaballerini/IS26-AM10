package it.polimi.ingsw.client.ui.GUI;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StatsPanel extends StackPane {

    private final VBox opponents;
    private final HBox drawer;
    private final Label toggleLabel;

    private boolean expanded = false;
    private boolean positionInitialized = false;

    private static final double BG_OPACITY    = 0.88;
    private static final double PANEL_PADDING = 12.0;

    public StatsPanel() {
        opponents = new VBox(6);
        opponents.setAlignment(Pos.TOP_LEFT);
        opponents.setPadding(new Insets(PANEL_PADDING));

        toggleLabel = new Label("▲ Statistiche avversari");
        toggleLabel.setRotate(-90);
        toggleLabel.setStyle(
                "-fx-background-color: rgba(20,20,20,0.82);" +
                        "-fx-text-fill: #e8d5a3;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8 8 0 0;" +
                        "-fx-padding: 6 14 6 14;"
        );

        Group tabGroup = new Group(toggleLabel);
        tabGroup.setOnMouseClicked(e -> toggleDrawer());
        CursorManager.makeNodesHoverable(tabGroup);

        drawer = new HBox(0, tabGroup, opponents);
        drawer.setAlignment(Pos.TOP_LEFT);
        drawer.setStyle(
                "-fx-background-color: rgba(18,15,12," + BG_OPACITY + ");" +
                        "-fx-background-radius: 8 0 0 8;"
        );

        drawer.widthProperty().addListener((obs, oldW, newW) -> {
            if (!positionInitialized && newW.doubleValue() > 0) {
                positionInitialized = true;
                drawer.setTranslateX(opponents.getWidth());
            }
        });

        this.getChildren().add(drawer);
        StackPane.setAlignment(drawer, Pos.TOP_RIGHT);

        this.setMaxWidth(Region.USE_PREF_SIZE);
        this.setMaxHeight(Region.USE_PREF_SIZE);
        this.setPickOnBounds(false);
    }

    public VBox getOpponents() {
        return opponents;
    }

    private void toggleDrawer() {
        expanded = !expanded;
        toggleLabel.setText(expanded ? "▼  Statistiche avversari" : "▲  Statistiche avversari");

        double target = expanded ? 0 : opponents.getWidth();

        TranslateTransition tt = new TranslateTransition(Duration.millis(250), drawer);
        tt.setToX(target);
        tt.play();
    }
}