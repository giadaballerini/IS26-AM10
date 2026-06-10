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

/**
 * A floating, slide-able drawer panel that displays the local player's village
 * (their hand of played cards) anchored to the bottom of the game scene.
 *
 * <p>{@code VillagePanel} is created programmatically by
 * {@link GameController} and added as an overlay on top of
 * the scene's root {@link StackPane}. Once initialized, {@link GameController}
 * discards its original FXML {@code myVillage} reference and replaces it with
 * the {@link HBox} exposed by {@link #getMyVillage()}, so that card columns
 * (grouped by {@link it.polimi.ingsw.enumerations.CardTypeEnum}) can be
 * appended to it as the player acquires new cards during the game.</p>
 *
 * <p>The panel consists of two vertically stacked regions inside a dark
 * rounded-top drawer:</p>
 * <ul>
 *   <li>A toggle {@link Button} at the top that collapses or expands the
 *       drawer with a 250 ms {@link TranslateTransition}.</li>
 *   <li>A content area containing {@link #myVillage}, the {@link HBox} into
 *       which {@link GameController} inserts one {@code VBox} column per card
 *       type.</li>
 * </ul>
 *
 * <p>Hit-testing is deliberately kept non-transparent on the drawer itself
 * but disabled on the {@link StackPane} bounds so that board elements beneath
 * the transparent region of the overlay remain clickable.</p>
 */
public class VillagePanel extends StackPane {

    /**
     * The inner container populated by {@link GameController} with one
     * {@code VBox} column per card type held in the player's village.
     */
    private final HBox myVillage;

    /** The outer sliding container that wraps the toggle button and the content area. */
    private final VBox drawer;

    /** The content area that holds {@link #myVillage} inside the drawer. */
    private final VBox content;

    /** The button that collapses or expands the drawer when clicked. */
    private final Button toggleBtn;

    /**
     * Tracks whether the drawer is currently expanded ({@code true}) or
     * collapsed ({@code false}).
     */
    private boolean expanded = true;

    /** Padding applied to the content area on all sides except the bottom. */
    private static final double DRAWER_PADDING = 10.0;

    /** Background opacity of the drawer, balancing legibility with board visibility. */
    private static final double BG_OPACITY = 0.88;

    /**
     * Constructs a {@code VillagePanel} and fully initializes its layout.
     *
     * <p>The drawer is aligned to {@link Pos#BOTTOM_CENTER} within the parent
     * {@link StackPane}, and its preferred width is left unconstrained so it
     * spans the full width of the scene root. The panel starts in the expanded
     * state.</p>
     */
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

    /**
     * Returns the inner {@link HBox} that holds the player's village card columns.
     *
     * <p>{@link GameController} calls this once during scene setup to obtain a
     * direct reference to the container, then appends one {@code VBox} column
     * per {@link it.polimi.ingsw.enumerations.CardTypeEnum} as the player draws
     * cards during the game.</p>
     *
     * @return the {@link HBox} to which card-type columns should be added
     */
    public HBox getMyVillage() {
        return myVillage;
    }

    /**
     * Toggles the drawer between its expanded and collapsed states using a
     * 250 ms {@link TranslateTransition}.
     *
     * <p>When collapsing, the drawer's preferred height is frozen to its current
     * rendered height before the translation begins. This prevents the layout
     * engine from recalculating the height mid-animation (which would shift the
     * drawer upward if new card children were added while it was moving).
     * When re-expanding, the preferred height constraint is released so the
     * layout can resize freely to accommodate its content.</p>
     *
     * <p>The toggle button label switches between {@code "▼"} (expanded) and
     * {@code "▲"} (collapsed) to provide a clear visual affordance.</p>
     */
    private void toggleDrawer() {
        expanded = !expanded;

        if (!expanded) {
            // Freeze the current height before collapsing so that adding children
            // does not push the drawer upward during the animation.
            drawer.setPrefHeight(drawer.getHeight());
        } else {
            // Re-opening: release the height constraint so the layout resizes freely.
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