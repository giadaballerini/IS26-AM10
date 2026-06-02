package it.polimi.ingsw.client.GUI;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public abstract class PopupPane extends StackPane {

    private final String displayName;
    private final String displayDescription;

    public PopupPane(String displayName, String displayDescription) {
        this.displayName = displayName;
        this.displayDescription = displayDescription;
        setupTooltip();
        setupInteractions();
    }

    private void setupInteractions() {
        this.setOnMouseEntered(e -> {
            this.setScaleX(1.1);
            this.setScaleY(1.1);
            this.setViewOrder(-1.0);
        });

        this.setOnMouseExited(e -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
            this.setViewOrder(0.0);
        });
    }

    private void setupTooltip() {
        VBox content = new VBox(6);
        content.setMouseTransparent(true);
        content.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 7;" +
                        "-fx-padding: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.22), 8, 0, 0, 3);"
        );

        Label title = new Label(displayName);
        title.setStyle(
                "-fx-text-fill: black;" +
                        "-fx-font-family: 'Vagabundo Medium';" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        Label description = new Label(displayDescription);
        description.setStyle(
                "-fx-text-fill: #222222;" +
                        "-fx-font-family: 'Vagabundo Medium';" +
                        "-fx-font-size: 16px;"
        );
        description.setWrapText(true);
        description.setMaxWidth(220);

        content.getChildren().addAll(title, description);

        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(content);

        // Rimuove lo stile di default del tooltip (quel rettangolino nero)
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        tooltip.setOnShowing(e -> {
            tooltip.setAnchorY(tooltip.getAnchorY() + 10);
        });

        // Imposta i ritardi (500ms come nel tuo PauseTransition)
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));

        // Collega automaticamente il tooltip al tuo nodo
        Tooltip.install(this, tooltip);
    }
}
