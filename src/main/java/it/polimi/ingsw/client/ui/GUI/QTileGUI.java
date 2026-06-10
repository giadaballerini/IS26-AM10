package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.data.QTileData;
import it.polimi.ingsw.client.data.QTileRegistry;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.HashMap;
import java.util.Map;

/**
 * GUI component representing a Queue Tile on the game board.
 *
 * <p>A Queue Tile is a special board tile that holds a fixed number of player
 * pawns ({@link TotemGUI}) in predefined slots. The number of available slots
 * and their pixel coordinates are defined by the tile's {@link QTileData},
 * loaded from the client-side {@link QTileRegistry}.</p>
 *
 * <p>Extends {@link PopupPane} to inherit hover scaling and the styled tooltip
 * that shows the tile's name and description. On mouse enter, the overlay
 * {@link AnchorPane} fades to highlight the tile; on exit it returns opaque.</p>
 */
public class QTileGUI extends PopupPane {

    /** Static data for this tile (slot coordinates, id, description). */
    private final QTileData qTileData;

    /** Image view rendering the front face of the tile. */
    private final ImageView view;

    /** Transparent overlay pane used to position {@link TotemGUI} pawns over the tile image. */
    private final AnchorPane anchorPane;

    /**
     * Tracks the occupancy of each pawn slot by slot index.
     * {@code true} means the slot is occupied by a pawn, {@code false} means it is free.
     */
    private final Map<Integer, Boolean> posToOccupied = new HashMap<>();

    /**
     * Constructs a {@code QTileGUI} for the Queue Tile with the given ID.
     *
     * <p>All slots are initialized as free. The tile image is loaded via
     * {@link QTileImagesLoader} and the tooltip content is sourced from
     * {@link QTileRegistry}.</p>
     *
     * @param qTileId the unique identifier of the Queue Tile, used to retrieve
     *                its image, slot layout, and description from the registry
     */
    public QTileGUI(int qTileId) {
        super(QTileRegistry.getName(), QTileRegistry.getDescription(qTileId));

        this.qTileData = QTileRegistry.getTile(qTileId);
        for (int i = 0; i < qTileId; i++) {
            posToOccupied.put(i, false);
        }
        view = new ImageView();
        view.setImage(QTileImagesLoader.getInstance().getFront(qTileId));
        view.setFitWidth(110);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        anchorPane = new AnchorPane();

        getChildren().addAll(view, anchorPane);
        this.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");
        this.getStyleClass().add("tile");
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            this.anchorPane.setOpacity(0.4);
        });
        this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            this.anchorPane.setOpacity(1);
        });
    }

    /**
     * Places a player pawn onto the first available slot of this tile.
     *
     * <p>Scans {@link #posToOccupied} in order and positions the given
     * {@link TotemGUI} at the slot's pixel coordinates as defined by
     * {@link QTileData#getSlotX(int)} and {@link QTileData#getSlotY(int)}.
     * If all slots are occupied, the pawn is silently ignored.</p>
     *
     * @param pawn the {@link TotemGUI} representing the player's totem to place
     */
    public void addPawn(TotemGUI pawn) {
        int firstFreeSlot = -1;
        for (int j = 0; j < qTileData.getId(); j++) {
            if (!posToOccupied.get(j)) {
                firstFreeSlot = j;
                break;
            }
        }
        if (firstFreeSlot != -1) {
            anchorPane.getChildren().add(pawn);
            anchorPane.setTopAnchor(pawn, qTileData.getSlotY(firstFreeSlot));
            anchorPane.setLeftAnchor(pawn, qTileData.getSlotX(firstFreeSlot));
            posToOccupied.put(firstFreeSlot, true);
        }
    }

    /**
     * Removes a specific player pawn from this tile and frees its slot.
     *
     * <p>The first occupied entry in {@link #posToOccupied} is marked free,
     * reflecting the removal of the pawn from the overlay pane. Slot indices
     * are released in insertion order (lowest index first).</p>
     *
     * @param pawn the {@link TotemGUI} to remove from the tile
     */
    public void removePawn(TotemGUI pawn) {
        anchorPane.getChildren().remove(pawn);
        for (int j = 0; j < qTileData.getId(); j++) {
            if (posToOccupied.get(j)) {
                posToOccupied.put(j, false);
                break;
            }
        }
    }

    /**
     * Removes all pawns from this tile and resets every slot to unoccupied.
     *
     * <p>Typically called when the game state is reset or the tile is cleared
     * at the end of a round.</p>
     */
    public void clearPawns() {
        anchorPane.getChildren().clear();
        for (int i = 0; i < qTileData.getId(); i++) {
            posToOccupied.put(i, false);
        }
    }
}