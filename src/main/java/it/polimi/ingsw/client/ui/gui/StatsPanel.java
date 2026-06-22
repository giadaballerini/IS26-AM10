package it.polimi.ingsw.client.ui.gui;

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

/**
 * Collapsible side panel displaying the opponent players' statistics
 * during a game session.
 *
 * <p>Rendered as a slide-in drawer anchored to the top-right corner of the
 * game board (managed by {@link GameController}). The panel consists of:</p>
 * <ul>
 *   <li>A vertical tab label ({@code toggleLabel}) rotated 90° that acts as
 *       the click target to expand or collapse the drawer.</li>
 *   <li>A {@link VBox} ({@code opponents}) populated at runtime by
 *       {@link GameController} with one entry per opponent, typically
 *       showing food, stars, prestige points and hand size.</li>
 * </ul>
 *
 * <p>The drawer is collapsed synchronously at construction time (see
 * {@link #collapseImmediately()}), translated off-screen to the right by the
 * width of {@code opponents} so only the tab remains visible. Clicking the
 * tab triggers a 250 ms {@link TranslateTransition} that slides the full
 * panel into view or hides it again.</p>
 *
 * <p>The panel uses {@code pickOnBounds = false} so that the transparent
 * area outside the drawer does not intercept mouse events on the board
 * beneath it.</p>
 *
 * @see VillagePanel
 */
public class StatsPanel extends StackPane {

    /**
     * Container populated at runtime with one stats row per opponent.
     * Accessible externally via {@link #getOpponents()} so that
     * {@link GameController} can add and update its children.
     */
    private final VBox opponents;

    /**
     * Horizontal container holding the {@code toggleLabel} tab and the
     * {@code opponents} pane side by side. Slide animation is applied
     * to this node via {@code translateX}.
     */
    private final HBox drawer;

    /**
     * Rotated label acting as the clickable toggle tab on the right edge
     * of the screen. Its text reflects the current expanded/collapsed state.
     */
    private final Label toggleLabel;

    /**
     * Whether the drawer is currently slid into view ({@code true})
     * or hidden off-screen ({@code false}).
     */
    private boolean expanded = false;

    /** Background opacity shared by the drawer container. */
    private static final double BG_OPACITY    = 0.88;

    /** Internal padding applied to the {@code opponents} {@link VBox}. */
    private static final double PANEL_PADDING = 12.0;

    /**
     * Constructs a {@code StatsPanel} with an empty opponents list and
     * a collapsed drawer.
     *
     * <p>The drawer's initial off-screen translation is deferred until
     * the first layout pass via a width listener, since the node's
     * dimensions are not available at construction time.</p>
     */
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

        this.getChildren().add(drawer);
        StackPane.setAlignment(drawer, Pos.TOP_RIGHT);

        this.setMaxWidth(Region.USE_PREF_SIZE);
        this.setMaxHeight(Region.USE_PREF_SIZE);
        this.setPickOnBounds(false);

        collapseImmediately();
    }

    /**
     * Forces a synchronous CSS + layout pass on {@link #drawer} and
     * immediately applies the collapsed translation, so the panel never
     * renders in its expanded position even for a single frame.
     *
     * <p>This replaces a reactive {@code widthProperty} listener approach:
     * waiting for a layout-triggered property change is timing-dependent on
     * when the panel is attached to the scene and shown, which could let an
     * expanded frame slip through before the listener fires. {@code drawer}
     * does not need to be attached to a {@code Scene} for this: it already
     * has {@code tabGroup} and {@link #opponents} as children, which is
     * enough for JavaFX to compute its preferred size in isolation.</p>
     */
    private void collapseImmediately() {
        drawer.applyCss();
        drawer.layout();
        drawer.setTranslateX(opponents.getWidth());
    }

    /**
     * Re-applies the collapsed position using the drawer's current content
     * width.
     *
     * <p>{@link #collapseImmediately()} runs at construction time, when
     * {@link #opponents} is still empty, so the resulting offset is based on
     * a near-zero width. {@link GameController} populates {@code opponents}
     * with one row per opponent shortly after construction; without a
     * re-snap the drawer is wider than the stale offset accounts for, so the
     * panel appears expanded at the start of a match even though
     * {@link #expanded} is {@code false}. Calling this once after the
     * initial population corrects the offset. No-op if the drawer is
     * currently expanded.</p>
     *
     * <p>Must be called on the FX Application Thread.</p>
     */
    public void snapCollapsed() {
        if (!expanded) {
            collapseImmediately();
        }
    }

    /**
     * Returns the {@link VBox} that holds the opponent stats rows.
     *
     * <p>Called by {@link GameController} to add, update, or remove
     * per-opponent stat entries as the game state changes.</p>
     *
     * @return the opponents container {@link VBox}
     */
    public VBox getOpponents() {
        return opponents;
    }

    /**
     * Toggles the drawer between its expanded and collapsed states,
     * animating the transition with a 250 ms {@link TranslateTransition}.
     *
     * <p>When expanding, the drawer slides left until fully visible
     * ({@code translateX = 0}). When collapsing, it slides right until
     * only the tab label remains on screen ({@code translateX = opponents.getWidth()}).
     * The toggle label arrow is updated to reflect the new state.</p>
     */
    private void toggleDrawer() {
        expanded = !expanded;
        toggleLabel.setText(expanded ? "▼  Statistiche avversari" : "▲  Statistiche avversari");

        double target = expanded ? 0 : opponents.getWidth();

        TranslateTransition tt = new TranslateTransition(Duration.millis(250), drawer);
        tt.setToX(target);
        tt.play();
    }
}