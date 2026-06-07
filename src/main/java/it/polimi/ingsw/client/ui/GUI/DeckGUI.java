package it.polimi.ingsw.client.ui.GUI;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DeckGUI extends VBox {

    private static final double CARD_WIDTH  = 110;
    private static final double CARD_OFFSET = 10;
    private static final int    NUM_LAYERS  = 3;

    private final Label countLabel;
    private int age;

    public DeckGUI(int initialDeckSize, int initialAge) {
        super(6);
        this.age = initialAge;
        setAlignment(Pos.CENTER);

        StackPane stack = buildStack();

        countLabel = new Label(initialDeckSize + " carte");
        countLabel.setTextFill(Color.WHITE);
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        installTooltip(initialDeckSize);
        installHover();

        getChildren().add(stack);
    }

    private StackPane buildStack() {
        StackPane pane = new StackPane();
        pane.setPrefSize(
                CARD_WIDTH + NUM_LAYERS * CARD_OFFSET,
                CARD_WIDTH * 1.5 + NUM_LAYERS * CARD_OFFSET
        );

        Image backImg = loadBackImage();
        for (int i = NUM_LAYERS - 1; i >= 0; i--) {
            ImageView iv = new ImageView(backImg);
            iv.setFitWidth(CARD_WIDTH);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setTranslateX( i * CARD_OFFSET * 0.5);
            iv.setTranslateY(-i * CARD_OFFSET * 0.5);
            pane.getChildren().add(iv);
        }
        return pane;
    }

    private void installTooltip(int deckSize) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        javafx.util.Duration showDelay = javafx.util.Duration.millis(300);
        tooltip.setShowDelay(showDelay);
        tooltip.setHideDelay(javafx.util.Duration.millis(100));

        tooltip.setGraphic(buildTooltipContent(deckSize));
        tooltip.setOnShowing(e -> {
            tooltip.setGraphic(buildTooltipContent(
                    Integer.parseInt(countLabel.getText().replace(" carte", ""))
            ));
            tooltip.setAnchorY(tooltip.getAnchorY() + 10);
        });

        Tooltip.install(this, tooltip);
    }

    private javafx.scene.layout.VBox buildTooltipContent(int deckSize) {
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(6);
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

        javafx.scene.control.Label title = new javafx.scene.control.Label("Mazzo");
        title.setStyle(
                "-fx-text-fill: black;" +
                        "-fx-font-family: 'Vagabundo Medium';" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        javafx.scene.control.Label desc = new javafx.scene.control.Label(
                "Carte rimanenti nel mazzo: " + deckSize
        );
        desc.setStyle(
                "-fx-text-fill: #222222;" +
                        "-fx-font-family: 'Vagabundo Medium';" +
                        "-fx-font-size: 16px;"
        );
        desc.setWrapText(true);
        desc.setMaxWidth(220);

        content.getChildren().addAll(title, desc);
        return content;
    }

    private void installHover() {
        setOnMouseEntered(e -> {
            setScaleX(1.1);
            setScaleY(1.1);
            setViewOrder(-1.0);
        });
        setOnMouseExited(e -> {
            setScaleX(1.0);
            setScaleY(1.0);
            setViewOrder(0.0);
        });
    }

    public void update(int newDeckSize, int newAge) {
        this.age = newAge;
        countLabel.setText(newDeckSize + " carte");

        StackPane stack = (StackPane) getChildren().getFirst();
        Image newBack = loadBackImage();
        stack.getChildren().forEach(node -> {
            if (node instanceof ImageView iv) iv.setImage(newBack);
        });
    }

    private Image loadBackImage() {
        String path = String.format("/images/cards/Back_card_%d.png", age);
        try {
            var res = getClass().getResource(path);
            if (res != null) return new Image(res.toExternalForm(), false);
        } catch (Exception e) {
            System.err.println("[DeckGUI] Back image not found: " + path);
        }
        var fallback = getClass().getResource("/images/cards/Back_card_1.png");
        return fallback != null ? new Image(fallback.toExternalForm(), false) : null;
    }
}