package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * JavaFX component that renders a player's totem (pawn) image in the GUI.
 *
 * <p>Each player in the game is identified by a {@link ColorPawnEnum} color
 * (ORANGE, YELLOW, BLUE, PURPLE, or WHITE). {@code TotemGUI} wraps the
 * corresponding pawn image — retrieved from {@link PawnImagesLoader} — into
 * a fixed-size 60×60 {@link javafx.scene.layout.StackPane}, ready to be
 * embedded in any scene that needs to display a player's presence on the
 * board or in the UI.</p>
 */
public class TotemGUI extends StackPane {

    /**
     * Constructs a {@code TotemGUI} for the given player color.
     *
     * <p>The totem image is sourced from {@link PawnImagesLoader#getTotemImage(ColorPawnEnum)},
     * which must have been populated beforehand by calling
     * {@link PawnImagesLoader#loadPawns()}. The image is rendered at a fixed
     * size of 60×60 pixels with the aspect ratio preserved; hit-testing is
     * enabled on the full bounding box so the node can receive mouse events
     * even in transparent areas.</p>
     *
     * @param color the {@link ColorPawnEnum} identifying the player whose
     *              totem this component represents
     */
    public TotemGUI(ColorPawnEnum color) {
        ImageView totem = new ImageView();
        totem.setImage(PawnImagesLoader.getInstance().getTotemImage(color));
        totem.setPreserveRatio(true);
        totem.setPickOnBounds(true);
        totem.setFitHeight(60);
        totem.setFitWidth(60);

        getChildren().add(totem);
    }
}