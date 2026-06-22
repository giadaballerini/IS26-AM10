package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the static display data associated with a board tile, as loaded from
 * the client-side JSON resource file. Each tile has up to five slots where player
 * pawns can be placed; this class stores the pixel coordinates of each slot,
 * used by the GUI to position pawns correctly on the tile image.
 *
 * @see QTileRegistry
 */
public class QTileData {

    /** The unique identifier of this tile, also used as the number of available slots. */
    @JsonProperty("id")
    private int id;

    /** The display description of this tile. */
    @JsonProperty("caption")
    private String description;

    /** X coordinate of slot 0. */
    @JsonProperty("slot0x")
    private double slot0X;

    /** Y coordinate of slot 0. */
    @JsonProperty("slot0y")
    private double slot0Y;

    /** X coordinate of slot 1. */
    @JsonProperty("slot1x")
    private double slot1X;

    /** Y coordinate of slot 1. */
    @JsonProperty("slot1y")
    private double slot1Y;

    /** X coordinate of slot 2. */
    @JsonProperty("slot2x")
    private double slot2X;

    /** Y coordinate of slot 2. */
    @JsonProperty("slot2y")
    private double slot2Y;

    /** X coordinate of slot 3. */
    @JsonProperty("slot3x")
    private double slot3X;

    /** Y coordinate of slot 3. */
    @JsonProperty("slot3y")
    private double slot3Y;

    /** X coordinate of slot 4. */
    @JsonProperty("slot4x")
    private double slot4X;

    /** Y coordinate of slot 4. */
    @JsonProperty("slot4y")
    private double slot4Y;

    /**
     * THe textual description of this tile.
     * @return the display description of this tile */
    public String getDescription() { return description; }

    /**
     * The unique identifier of this tile.
     * @return the unique identifier of this tile
     */
    public int getId() { return id; }

    /**
     * Returns the X coordinate of the pawn slot at the given index,
     * used by the GUI to position a pawn on the tile image.
     *
     * @param index the slot index (0–4)
     * @return the X coordinate of the slot, or {@code 0.0} if the index is out of range
     */
    public double getSlotX(int index) {
        return switch (index) {
            case 0 -> slot0X;
            case 1 -> slot1X;
            case 2 -> slot2X;
            case 3 -> slot3X;
            case 4 -> slot4X;
            default -> 0.0;
        };
    }

    /**
     * Returns the Y coordinate of the pawn slot at the given index,
     * used by the GUI to position a pawn on the tile image.
     *
     * @param index the slot index (0–4)
     * @return the Y coordinate of the slot, or {@code 0.0} if the index is out of range
     */
    public double getSlotY(int index) {
        return switch (index) {
            case 0 -> slot0Y;
            case 1 -> slot1Y;
            case 2 -> slot2Y;
            case 3 -> slot3Y;
            case 4 -> slot4Y;
            default -> 0.0;
        };
    }
    /**
     * Creates a new empty {@code QTileData} instance.
     * Required by Jackson for JSON deserialization.
     */
    public QTileData() {
    }
}