package it.polimi.ingsw.client.ui.GUI;

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


public class TileGUI extends PopupPane {
    //per il glow delle frecce
    private static final double FIXED_SLOT_W = 24.0;
    private static final double FIXED_SLOT_H = 32.0;
    private static final double SLOT_GAP     = 5.0;

    private final int tileId;
    private final ImageView view;
    private final AnchorPane pawnContainer;
    private final Rectangle highlightRectangle;
    private FadeTransition highlightAnimation;
    private boolean occupied = false;

    // null se la tile non ha frecce (es. Tile_0)
    private final TileData.HighlightBounds arrowBounds;

    // Contenitore esterno; contiene upBox + downBox affiancati.
    private final HBox arrowIndicatorBox;

    // Gruppo sinistro: slot per le upDraw (oro)
    private final HBox upBox;
    // Gruppo destro: slot per le downDraw (azzurro)
    private final HBox downBox;

    private final int tileMaxUp;
    private final int tileMaxDown;

    // Dimensioni fissate al primo setArrowHighlight(up>0 || down>0)
    private int upMaxCount = 0;
    private int downMaxCount = 0;

    // Animazioni separate per i due gruppi
    private final List<FadeTransition> upAnimations = new ArrayList<>();
    private final List<FadeTransition> downAnimations = new ArrayList<>();

    // Colori dei due tipi di azione
    private static final Color COLOR_UP = Color.GOLD;
    private static final Color COLOR_DOWN = Color.web("#7ec8e3"); // azzurro chiaro

    public TileGUI(int tileId) {

        super(TileRegistry.getName(), TileRegistry.getDescription(tileId));

        this.tileId = tileId;
        TileData data = TileRegistry.getTile(tileId);

        view = new ImageView();
        view.setImage(TileImagesLoader.getInstance().getFront(this.tileId));
        view.setFitWidth(110);
        double ratio = TileImagesLoader.getInstance().getFront(this.tileId).getHeight() / TileImagesLoader.getInstance().getFront(this.tileId).getWidth();
        double computedHeight = 110 * ratio;
        view.setFitHeight(computedHeight);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        this.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");

        pawnContainer = new AnchorPane();
        pawnContainer.setPickOnBounds(false);
        pawnContainer.setPrefSize(110, computedHeight);

        highlightRectangle = new Rectangle(49, 28);
        highlightRectangle.setArcWidth(6);
        highlightRectangle.setArcHeight(6);
        highlightRectangle.setFill(Color.TRANSPARENT);
        highlightRectangle.setStroke(Color.GOLD);
        highlightRectangle.setStrokeWidth(2.5);
        highlightRectangle.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.GOLD, 10, 0.5, 0, 0));
        highlightRectangle.setVisible(false);
        highlightRectangle.setMouseTransparent(true);
        StackPane.setAlignment(highlightRectangle, Pos.TOP_CENTER);
        StackPane.setMargin(highlightRectangle, new Insets(38, 0, 0, 0));

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

            // Separatore visivo tra i due gruppi
            Rectangle sep = new Rectangle(1, 10);
            sep.setFill(Color.gray(0.5, 0.5));

            arrowIndicatorBox = new HBox(4, downBox, sep, upBox);
            arrowIndicatorBox.setAlignment(Pos.CENTER);
            arrowIndicatorBox.setMouseTransparent(true);
            arrowIndicatorBox.setVisible(false);

            StackPane.setAlignment(arrowIndicatorBox, Pos.TOP_LEFT);
            StackPane.setMargin(arrowIndicatorBox, new Insets(arrowBounds.topMargin, 0, 0, arrowBounds.leftMargin));
        } else {
            arrowBounds = null;
            arrowIndicatorBox = null;
            upBox = null;
            downBox = null;
            this.tileMaxUp   = 0;
            this.tileMaxDown = 0;
        }

        getChildren().addAll(view, pawnContainer, highlightRectangle);
        if (arrowIndicatorBox != null) getChildren().add(arrowIndicatorBox);


        this.setMinSize(110, computedHeight);
        this.setPrefSize(110, computedHeight);
        this.setMaxSize(110, computedHeight);

        this.getStyleClass().add("tile");
    }

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
     * Mostra gli indicatori freccia distinti per upDraw (oro, sinistra) e
     * downDraw (azzurro, destra). Ogni gruppo ha slot fissi: i rettangoli
     * usati diventano invisibili ma occupano ancora spazio, così la posizione
     * degli slot rimasti non cambia.
     * Passare (0, 0) per spegnere tutto.
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
            upAnimations.forEach(FadeTransition::stop);  upAnimations.clear();
            downAnimations.forEach(FadeTransition::stop); downAnimations.clear();

            upMaxCount   = Math.max(upMaxCount,   upDraws);
            downMaxCount = Math.max(downMaxCount, downDraws);

            double upW   = upMaxCount   * FIXED_SLOT_W + Math.max(0, upMaxCount   - 1) * SLOT_GAP;
            double downW = downMaxCount * FIXED_SLOT_W + Math.max(0, downMaxCount - 1) * SLOT_GAP;

            if (upBox != null) {
                upBox.getChildren().clear();
                upBox.setMinWidth(upW);
                upBox.setPrefWidth(upW);
                upBox.setMaxWidth(upW);
                buildGroup(upBox, upMaxCount, COLOR_UP, upAnimations);
            }

            if (downBox != null) {
                downBox.getChildren().clear();
                downBox.setMinWidth(downW);
                downBox.setPrefWidth(downW);
                downBox.setMaxWidth(downW);
                buildGroup(downBox, downMaxCount, COLOR_DOWN, downAnimations);
            }
        }

        applyGroup(upBox,   upAnimations,   upMaxCount,   upDraws);
        applyGroup(downBox, downAnimations, downMaxCount, downDraws);
        arrowIndicatorBox.setVisible(true);
    }

    /** Costruisce gli slot fissi di un gruppo (chiamato una sola volta per gruppo). */
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
     * Mostra i primi {@code active} slot del gruppo e nasconde i restanti.
     * setVisible(false) mantiene lo slot nel layout (occupa spazio) ma lo
     * rende invisibile, così gli slot attivi non si spostano.
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
     * Resetta completamente gli slot freccia.
     * Chiamare a inizio turno (tramite resetArrowSize in GameController)
     * prima di invocare setArrowHighlight con i nuovi valori.
     */
    public void resetArrowSize() {
        upAnimations.forEach(FadeTransition::stop);
        downAnimations.forEach(FadeTransition::stop);
        upAnimations.clear();
        downAnimations.clear();

        upMaxCount   = tileMaxUp;
        downMaxCount = tileMaxDown;

        double upW = tileMaxUp * FIXED_SLOT_W + Math.max(0, tileMaxUp - 1) * SLOT_GAP;
        double downW = tileMaxDown * FIXED_SLOT_W + Math.max(0, tileMaxDown - 1) * SLOT_GAP;
        if (upBox != null) {
            upBox.getChildren().clear();
            upBox.setMinWidth(upW); upBox.setPrefWidth(upW); upBox.setMaxWidth(upW);
            buildGroup(upBox, tileMaxUp, COLOR_UP, upAnimations); // ricostruisce gli slot vuoti
        }
        if (downBox != null) {
            downBox.getChildren().clear();
            downBox.setMinWidth(downW); downBox.setPrefWidth(downW); downBox.setMaxWidth(downW);
            buildGroup(downBox, tileMaxDown, COLOR_DOWN, downAnimations);
        }
        if (arrowIndicatorBox != null) arrowIndicatorBox.setVisible(false);
    }


    public void addPawn(TotemGUI pawn) {
        if (pawn.getParent() != null) {
            ((javafx.scene.layout.Pane) pawn.getParent()).getChildren().remove(pawn);
        }
        pawnContainer.getChildren().add(pawn);
        AnchorPane.setTopAnchor(pawn, 2.0);
        AnchorPane.setLeftAnchor(pawn, 36.0);
        occupied = true;
    }

    public void removePawn(TotemGUI pawn) {
        pawnContainer.getChildren().remove(pawn);
        occupied = false;
    }

    public boolean isOccupied() { return occupied; }

    public void clearPawn() {
        pawnContainer.getChildren().clear();
        occupied = false;
    }
}