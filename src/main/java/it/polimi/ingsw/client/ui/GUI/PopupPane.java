package it.polimi.ingsw.client.ui.GUI;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Abstract base class for interactive GUI elements that display a styled tooltip
 * on hover, used throughout the game board to represent visual game objects
 * such as {@link CardGUI}, {@link TileGUI}, and {@link QTileGUI}.
 *
 * <p>Each {@code PopupPane} is a {@link StackPane} that reacts to mouse events
 * with a subtle scale animation and shows a custom floating tooltip containing
 * the element's name and description. Subclasses provide the concrete visual
 * content (card image, tile layout, etc.) by adding children to this pane.</p>
 *
 * <p>The tooltip content (name and description) is typically sourced from the
 * client-side registries ({@code CardRegistry}, {@code TileRegistry},
 * {@code QTileRegistry}) at construction time.</p>
 */
public abstract class PopupPane extends StackPane {

    /** The display name shown as the tooltip title (e.g. card name, tile name). */
    private final String displayName;

    /** The display description shown as the tooltip body (e.g. card effect, tile info). */
    private final String displayDescription;

    /**
     * Constructs a {@code PopupPane} with the given display name and description,
     * and initializes hover interactions and the tooltip.
     *
     * @param displayName        the title shown at the top of the tooltip,
     *                           typically the name of the game element
     * @param displayDescription the body text of the tooltip,
     *                           typically the effect or flavor description
     */
    public PopupPane(String displayName, String displayDescription) {
        this.displayName = displayName;
        this.displayDescription = displayDescription;
        setupTooltip();
        setupInteractions();
    }

    /**
     * Registers mouse enter/exit handlers on this pane to produce a
     * scale-up animation when hovered.
     *
     * <p>On enter, the pane scales to 110% and is brought to the front
     * via {@code viewOrder}. On exit, it returns to its original size and
     * depth.</p>
     */
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

    /**
     * Builds and installs the custom {@link Tooltip} on this pane.
     *
     * <p>The tooltip uses a {@link VBox} as its graphic content, bypassing
     * JavaFX's default tooltip styling (black rectangle) in favor of a
     * white rounded card with a drop shadow. It contains two {@link Label}s:
     * one for {@link #displayName} and one for {@link #displayDescription}.</p>
     *
     * <p>Timing is configured with a show delay of 300 ms and a hide delay
     * of 100 ms. The tooltip anchor is offset by 10 px downward on show to
     * avoid overlapping the cursor.</p>
     */
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

        // Removes the default tooltip background (black rectangle)
        tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        tooltip.setOnShowing(e -> {
            tooltip.setAnchorY(tooltip.getAnchorY() + 10);
        });

        // Show delay matches the UX feel of the rest of the board interactions
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));

        // Attaches the tooltip to this node automatically
        Tooltip.install(this, tooltip);
    }
}