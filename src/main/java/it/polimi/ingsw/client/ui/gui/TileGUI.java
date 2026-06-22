package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.client.data.TileData;
import it.polimi.ingsw.client.data.TileRegistry;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI component representing a board tile that players can place their pawn on.
 *
 * <p>Each {@code TileGUI} extends {@link PopupPane} to inherit hover scaling
 * and the styled tooltip sourced from {@link TileRegistry}. On top of the tile
 * image it layers:</p>
 * <ul>
 *   <li>A {@link TotemGUI} pawn placed via {@link #addPawn(TotemGUI)} when a
 *       player occupies the tile.</li>
 *   <li>A gold pulsing {@link Rectangle} ({@code highlightRectangle}) shown
 *       over the card slot when the tile is selectable by the current player,
 *       toggled by {@link #setHighlight(boolean)}.</li>
 *   <li>An arrow indicator composed of two {@link HBox} groups
 *       ({@code upBox} for gold up-draws, {@code downBox} for light-blue
 *       down-draws), shown when the tile's draw actions are available,
 *       managed by {@link #setArrowHighlight(int, int)}.</li>
 * </ul>
 *
 * <p>Tiles without an arrow area (e.g. {@code Tile_0}) have {@code arrowBounds},
 * {@code arrowIndicatorBox}, {@code upBox} and {@code downBox} set to
 * {@code null}; arrow-related methods are no-ops for those tiles.</p>
 *
 * @see TileData
 * @see TileRegistry
 * @see QTileGUI
 */
public class TileGUI extends PopupPane {

    /** Fixed pixel width of a single arrow indicator slot. */
    private static final double FIXED_SLOT_W = 24.0;

    /** Fixed pixel height of a single arrow indicator slot. */
    private static final double FIXED_SLOT_H = 32.0;

    /** Pixel gap between adjacent slots within the same group. */
    private static final double SLOT_GAP = 5.0;

    /** Stroke color for up-draw arrow slots (gold). */
    private static final Color COLOR_UP = Color.GOLD;

    /** Stroke color for down-draw arrow slots (light blue). */
    private static final Color COLOR_DOWN = Color.web("#7ec8e3");

    /**
     * Transparent overlay pane used to anchor a {@link TotemGUI} pawn
     * over the tile image at a fixed position.
     */
    private final AnchorPane pawnContainer;

    /**
     * Gold rounded rectangle overlaid on the card slot area.
     * Pulses via {@link #highlightAnimation} when {@link #setHighlight(boolean)}
     * is called with {@code true}.
     */
    private final Rectangle highlightRectangle;

    /**
     * Looping {@link FadeTransition} applied to {@link #highlightRectangle}.
     * Created lazily on the first call to {@link #setHighlight(boolean) setHighlight(true)}.
     */
    private FadeTransition highlightAnimation;

    /** Whether a {@link TotemGUI} pawn is currently placed on this tile. */
    private boolean occupied = false;

    /**
     * Outer container holding {@link #downBox}, a separator, and {@link #upBox}
     * side by side, positioned over the tile's arrow area.
     * {@code null} for tiles without arrow support.
     */
    private final HBox arrowIndicatorBox;

    /**
     * Left subgroup of {@link #arrowIndicatorBox} holding gold slots for
     * up-draw actions. {@code null} for tiles without arrow support.
     */
    private final HBox upBox;

    /**
     * Right subgroup of {@link #arrowIndicatorBox} holding light-blue slots
     * for down-draw actions. {@code null} for tiles without arrow support.
     */
    private final HBox downBox;

    /**
     * Maximum number of up-draw actions this tile can ever display,
     * as defined by {@link TileData#getMaxUpDraws()}.
     * Used by {@link #resetArrowSize()} to restore the baseline slot count.
     */
    private final int tileMaxUp;

    /**
     * Maximum number of down-draw actions this tile can ever display,
     * as defined by {@link TileData#getMaxDownDraws()}.
     * Used by {@link #resetArrowSize()} to restore the baseline slot count.
     */
    private final int tileMaxDown;

    /**
     * Current number of pre-built slots in {@link #upBox}.
     * May grow beyond {@link #tileMaxUp} if {@link #setArrowHighlight(int, int)}
     * is called with a higher value before {@link #resetArrowSize()} is invoked.
     */
    private int upMaxCount = 0;

    /**
     * Current number of pre-built slots in {@link #downBox}.
     * May grow beyond {@link #tileMaxDown} if {@link #setArrowHighlight(int, int)}
     * is called with a higher value before {@link #resetArrowSize()} is invoked.
     */
    private int downMaxCount = 0;

    /** Active {@link FadeTransition} animations for each slot in {@link #upBox}. */
    private final List<FadeTransition> upAnimations = new ArrayList<>();

    /** Active {@link FadeTransition} animations for each slot in {@link #downBox}. */
    private final List<FadeTransition> downAnimations = new ArrayList<>();

    /**
     * Constructs a {@code TileGUI} for the tile with the given ID.
     *
     * <p>Loads the tile image via {@link TileImagesLoader}, reads slot and
     * arrow metadata from {@link TileRegistry}, and conditionally builds the
     * arrow indicator subcomponents. All pawn and highlight layers are
     * initialized but kept hidden until explicitly activated.</p>
     *
     * @param tileId the unique identifier of the tile, used to retrieve its
     *               image, description, and layout data from the registry
     */
    public TileGUI(int tileId) {
        super(TileRegistry.getName(), TileRegistry.getDescription(tileId));

        TileData data = TileRegistry.getTile(tileId);

        ImageView view = new ImageView();
        view.setImage(TileImagesLoader.getInstance().getFront(tileId));
        view.setFitWidth(110);
        double ratio = TileImagesLoader.getInstance().getFront(tileId).getHeight()
                / TileImagesLoader.getInstance().getFront(tileId).getWidth();
        double computedHeight = 110 * ratio;
        view.setFitHeight(computedHeight);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        this.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");

        pawnContainer = new AnchorPane();
        pawnContainer.setPickOnBounds(false);
        pawnContainer.setPrefSize(110, computedHeight);

        highlightRectangle = new Rectangle(data.getPawnSlot().width, data.getPawnSlot().height);
        highlightRectangle.setArcWidth(6);
        highlightRectangle.setArcHeight(6);
        highlightRectangle.setFill(Color.TRANSPARENT);
        highlightRectangle.setStroke(Color.GOLD);
        highlightRectangle.setStrokeWidth(2.5);
        highlightRectangle.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.GOLD, 10, 0.5, 0, 0));
        highlightRectangle.setVisible(false);
        highlightRectangle.setMouseTransparent(true);
        StackPane.setAlignment(highlightRectangle, Pos.TOP_CENTER);
        StackPane.setMargin(highlightRectangle, new Insets(data.getPawnSlot().topMargin, 0, 0, data.getPawnSlot().leftMargin));

        TileData.HighlightBounds arrowBounds;
        if (data.hasArrows()) {
            arrowBounds = data.getArrowArea();

            int maxUp   = data.getMaxUpDraws();
            int maxDown = data.getMaxDownDraws();
            this.tileMaxUp   = maxUp;
            this.tileMaxDown = maxDown;

            upBox = new HBox(4);
            upBox.setAlignment(Pos.CENTER_RIGHT);
            upBox.setMouseTransparent(true);
            double upW = maxUp * FIXED_SLOT_W + Math.max(0, maxUp - 1) * SLOT_GAP;
            upBox.setMinWidth(upW); upBox.setPrefWidth(upW); upBox.setMaxWidth(upW);

            downBox = new HBox(4);
            downBox.setAlignment(Pos.CENTER_LEFT);
            downBox.setMouseTransparent(true);
            double downW = maxDown * FIXED_SLOT_W + Math.max(0, maxDown - 1) * SLOT_GAP;
            downBox.setMinWidth(downW); downBox.setPrefWidth(downW); downBox.setMaxWidth(downW);

            Rectangle sep = new Rectangle(1, 10);
            sep.setFill(Color.gray(0.5, 0.5));

            arrowIndicatorBox = new HBox(4, downBox, sep, upBox);
            arrowIndicatorBox.setAlignment(Pos.CENTER);
            arrowIndicatorBox.setMouseTransparent(true);
            arrowIndicatorBox.setVisible(false);

            StackPane.setAlignment(arrowIndicatorBox, Pos.TOP_LEFT);
            StackPane.setMargin(arrowIndicatorBox,
                    new Insets(arrowBounds.topMargin, 0, 0, arrowBounds.leftMargin));
        } else {
            arrowBounds = null;
            arrowIndicatorBox = null;
            upBox             = null;
            downBox           = null;
            this.tileMaxUp    = 0;
            this.tileMaxDown  = 0;
        }

        getChildren().addAll(view, pawnContainer, highlightRectangle);
        if (arrowIndicatorBox != null) getChildren().add(arrowIndicatorBox);

        this.setMinSize(110, computedHeight);
        this.setPrefSize(110, computedHeight);
        this.setMaxSize(110, computedHeight);
        this.getStyleClass().add("tile");
    }

    /**
     * Activates or deactivates the gold pulsing highlight on the tile's card slot.
     *
     * <p>Used by {@link GameController} to signal to the current player that
     * this tile is a valid move target. When turned on, a {@link FadeTransition}
     * is created lazily and played indefinitely. When turned off, the animation
     * is stopped and the rectangle hidden.</p>
     *
     * @param on {@code true} to show and animate the highlight;
     *           {@code false} to stop the animation and hide it
     */
    public void setHighlight(boolean on) {
        if (on) {
            highlightRectangle.setVisible(true);
            if (highlightAnimation == null) {
                highlightAnimation = new FadeTransition(Duration.millis(800), highlightRectangle);
                highlightAnimation.setFromValue(0.3);
                highlightAnimation.setToValue(1.0);
                highlightAnimation.setCycleCount(FadeTransition.INDEFINITE);
                highlightAnimation.setAutoReverse(true);
            }
            highlightAnimation.play();
        } else {
            if (highlightAnimation != null) highlightAnimation.stop();
            highlightRectangle.setVisible(false);
        }
    }

    /**
     * Updates the arrow indicator to show the number of available up-draw
     * and down-draw actions on this tile for the current player's turn.
     *
     * <p>Up-draw slots are rendered in gold ({@link #COLOR_UP}) on the right
     * subgroup; down-draw slots in light blue ({@link #COLOR_DOWN}) on the
     * left subgroup. Each active slot pulses via a staggered
     * {@link FadeTransition}; inactive slots remain invisible but still occupy
     * their fixed width so that active slot positions do not shift.</p>
     *
     * <p>If both counts exceed the currently allocated slot count, all slots
     * are rebuilt to accommodate the new maximum before being applied.
     * Passing {@code (0, 0)} hides the indicator entirely.</p>
     *
     * <p>This method is a no-op for tiles that have no arrow area.</p>
     *
     * @param upDraws   the number of up-draw action slots to highlight (gold);
     *                  must be &ge; 0
     * @param downDraws the number of down-draw action slots to highlight (light blue);
     *                  must be &ge; 0
     */
    public void setArrowHighlight(int upDraws, int downDraws) {
        if (arrowIndicatorBox == null) return;

        if (upDraws <= 0 && downDraws <= 0) {
            upAnimations.forEach(FadeTransition::stop);
            downAnimations.forEach(FadeTransition::stop);
            arrowIndicatorBox.setVisible(false);
            return;
        }

        if (upDraws > upMaxCount || downDraws > downMaxCount) {
            upAnimations.forEach(FadeTransition::stop);   upAnimations.clear();
            downAnimations.forEach(FadeTransition::stop); downAnimations.clear();

            upMaxCount   = Math.max(upMaxCount,   upDraws);
            downMaxCount = Math.max(downMaxCount, downDraws);

            double upW   = upMaxCount   * FIXED_SLOT_W + Math.max(0, upMaxCount   - 1) * SLOT_GAP;
            double downW = downMaxCount * FIXED_SLOT_W + Math.max(0, downMaxCount - 1) * SLOT_GAP;

            if (upBox != null) {
                upBox.getChildren().clear();
                upBox.setMinWidth(upW); upBox.setPrefWidth(upW); upBox.setMaxWidth(upW);
                buildGroup(upBox, upMaxCount, COLOR_UP, upAnimations);
            }
            if (downBox != null) {
                downBox.getChildren().clear();
                downBox.setMinWidth(downW); downBox.setPrefWidth(downW); downBox.setMaxWidth(downW);
                buildGroup(downBox, downMaxCount, COLOR_DOWN, downAnimations);
            }
        }

        applyGroup(upBox,   upAnimations,   upMaxCount,   upDraws);
        applyGroup(downBox, downAnimations, downMaxCount, downDraws);
        arrowIndicatorBox.setVisible(true);
    }

    /**
     * Populates an arrow indicator group with {@code max} fixed-size animated slots.
     *
     * <p>Each slot is a rounded {@link Rectangle} stroked in {@code color} with a
     * matching {@link DropShadow} glow. A {@link FadeTransition} is created per slot
     * with a 120 ms staggered delay so that adjacent slots pulse at slightly different
     * phases. Animations are stored in {@code anims} for later control via
     * {@link #applyGroup}.</p>
     *
     * <p>This method should be called only once per group per rebuild; call
     * {@link #resetArrowSize()} or clear the box before rebuilding.</p>
     *
     * @param box   the {@link HBox} to populate with slot rectangles
     * @param max   the total number of slots to create
     * @param color the stroke and glow color for the slots
     * @param anims the list to append each slot's {@link FadeTransition} to
     */
    private void buildGroup(HBox box, int max, Color color, List<FadeTransition> anims) {
        for (int i = 0; i < max; i++) {
            Rectangle r = new Rectangle(FIXED_SLOT_W, FIXED_SLOT_H);
            r.setArcWidth(4);
            r.setArcHeight(4);
            r.setFill(Color.TRANSPARENT);
            r.setStroke(color);
            r.setStrokeWidth(2);
            DropShadow glow = new DropShadow(BlurType.GAUSSIAN, color, 10.0, 0.3, 0, 0);
            r.setEffect(glow);

            FadeTransition ft = new FadeTransition(Duration.millis(800), r);
            ft.setFromValue(0.3);
            ft.setToValue(1.0);
            ft.setCycleCount(FadeTransition.INDEFINITE);
            ft.setAutoReverse(true);
            ft.setDelay(Duration.millis(i * 120));

            anims.add(ft);
            box.getChildren().add(r);
        }
    }

    /**
     * Shows and animates the first {@code active} slots in a group, hiding the rest.
     *
     * <p>Hidden slots use {@code setVisible(false)} rather than being removed, so
     * they still occupy their fixed width in the layout and active slots do not
     * shift position as the count changes.</p>
     *
     * @param box    the {@link HBox} containing the pre-built slot rectangles
     * @param anims  the corresponding list of {@link FadeTransition} animations
     * @param max    the total number of slots in the group
     * @param active the number of slots to make visible and animate (from index 0)
     */
    private void applyGroup(HBox box, List<FadeTransition> anims, int max, int active) {
        for (int i = 0; i < max; i++) {
            Rectangle r = (Rectangle) box.getChildren().get(i);
            boolean on = i < active;
            r.setVisible(on);
            if (on) anims.get(i).play();
            else    anims.get(i).stop();
        }
    }

    /**
     * Fully resets the arrow indicator slots back to the tile's baseline capacity.
     *
     * <p>Stops and discards all running animations, then rebuilds {@link #upBox}
     * and {@link #downBox} with exactly {@link #tileMaxUp} and {@link #tileMaxDown}
     * slots respectively, matching the maximums defined in {@link TileData}.
     * The indicator is hidden after the reset.</p>
     *
     * <p>Should be called by {@link GameController} at the start of each turn
     * before invoking {@link #setArrowHighlight(int, int)} with the new values,
     * to ensure that any over-allocated slots from a previous turn are discarded.</p>
     */
    public void resetArrowSize() {
        upAnimations.forEach(FadeTransition::stop);
        downAnimations.forEach(FadeTransition::stop);
        upAnimations.clear();
        downAnimations.clear();

        upMaxCount   = tileMaxUp;
        downMaxCount = tileMaxDown;

        double upW   = tileMaxUp   * FIXED_SLOT_W + Math.max(0, tileMaxUp   - 1) * SLOT_GAP;
        double downW = tileMaxDown * FIXED_SLOT_W + Math.max(0, tileMaxDown - 1) * SLOT_GAP;

        if (upBox != null) {
            upBox.getChildren().clear();
            upBox.setMinWidth(upW); upBox.setPrefWidth(upW); upBox.setMaxWidth(upW);
            buildGroup(upBox, tileMaxUp, COLOR_UP, upAnimations);
        }
        if (downBox != null) {
            downBox.getChildren().clear();
            downBox.setMinWidth(downW); downBox.setPrefWidth(downW); downBox.setMaxWidth(downW);
            buildGroup(downBox, tileMaxDown, COLOR_DOWN, downAnimations);
        }
        if (arrowIndicatorBox != null) arrowIndicatorBox.setVisible(false);
    }

    /**
     * Places a {@link TotemGUI} pawn on this tile at the fixed pawn anchor position.
     *
     * <p>If the pawn is currently attached to another parent container, it is
     * detached first to avoid duplicate-parent exceptions. After placement
     * the tile is marked as {@link #occupied}.</p>
     *
     * @param pawn the {@link TotemGUI} representing the player's totem to place
     */
    public void addPawn(TotemGUI pawn) {
        if (pawn.getParent() != null) {
            ((javafx.scene.layout.Pane) pawn.getParent()).getChildren().remove(pawn);
        }
        pawnContainer.getChildren().add(pawn);
        AnchorPane.setTopAnchor(pawn, 2.0);
        AnchorPane.setLeftAnchor(pawn, 36.0);
        occupied = true;
    }

    /**
     * Removes a specific {@link TotemGUI} pawn from this tile and marks it
     * as unoccupied.
     *
     * @param pawn the {@link TotemGUI} to remove
     */
    public void removePawn(TotemGUI pawn) {
        pawnContainer.getChildren().remove(pawn);
        occupied = false;
    }

    /**
     * Returns whether this tile currently holds a pawn.
     *
     * @return {@code true} if a {@link TotemGUI} has been placed via
     *         {@link #addPawn(TotemGUI)} and not yet removed; {@code false} otherwise
     */
    public boolean isOccupied() { return occupied; }

    /**
     * Removes all pawns from this tile and marks it as unoccupied.
     *
     * <p>Typically called during a full board reset or at the end of a round
     * when all players' pawns are returned.</p>
     */
    public void clearPawn() {
        pawnContainer.getChildren().clear();
        occupied = false;
    }
}