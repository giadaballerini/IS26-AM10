package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the static display data associated with a board tile, as loaded from
 * the client-side JSON resource file. Each tile holds the bounds of its interactive
 * GUI areas (card slot and optional arrow area) and the maximum number of draw
 * indicators to display in the GUI.
 *
 * @see TileRegistry
 */
public class TileData {

    /** The unique identifier of this tile. */
    @JsonProperty("id")
    private int id;

    /** The display description of this tile. */
    @JsonProperty("caption")
    private String description;

    /**
     * Bounds of the rectangle highlighting the card slot area.
     * Always present for every tile.
     */
    @JsonProperty("pawnSlot")
    private HighlightBounds pawnSlot;

    /**
     * Bounds of the rectangle highlighting the arrow area.
     * {@code null} for tiles that have no draw actions.
     */
    @JsonProperty("arrowArea")
    private HighlightBounds arrowArea;

    /** Maximum number of up-draw indicators to display in the GUI for this tile. */
    @JsonProperty("maxUpDraws")
    private int maxUpDraws;

    /** Maximum number of down-draw indicators to display in the GUI for this tile. */
    @JsonProperty("maxDownDraws")
    private int maxDownDraws;

    /**
     * Defines the pixel bounds of a highlighted rectangular area overlaid on a tile image.
     * Used by the GUI to position the card slot highlight and the arrow area indicator.
     * All coordinates are in pixels, relative to a tile rendered at 110 px width.
     */
    public static class HighlightBounds {

        /** Top margin of the rectangle from the top of the rendered tile image (px). */
        @JsonProperty("topMargin")
        public double topMargin;

        /** Left margin of the rectangle from the left edge of the rendered tile image (px). */
        @JsonProperty("leftMargin")
        public double leftMargin;

        /** Width of the highlighted rectangle (px). */
        @JsonProperty("width")
        public double width;

        /** Height of the highlighted rectangle (px). */
        @JsonProperty("height")
        public double height;

        /**
         * Creates a new empty {@code HighlightBounds} instance.
         * Required by Jackson for JSON deserialization.
         */
        public HighlightBounds() {
        }
    }

    /**
     * Returns the textual description of this tile.
     *  @return the display description of this tile */
    public String getDescription() { return description; }

    /**
     * Returns the unique identifier of this tile.
     *  @return the unique identifier of this tile */
    public int getId() { return id; }

    /**
     * Returns the {@link HighlightBounds} of the card slot area.
    * @return the {@link HighlightBounds} of the card slot area
    */
    public HighlightBounds getPawnSlot() { return pawnSlot; }

    /**
     * Returns the {@link HighlightBounds} of the arrow area,
     * @return the {@link HighlightBounds} of the arrow area,
     *         or {@code null} if this tile has no draw actions
     */
    public HighlightBounds getArrowArea() { return arrowArea; }

    /**
     * Returns {@code true} if this tile has an arrow area, {@code false} otherwise.
     * @return {@code true} if this tile has an arrow area, {@code false} otherwise
     */
    public boolean hasArrows() { return arrowArea != null; }

    /**
     * Returns the maximum number of up-draw indicators to display in the GUI for this tile.
     * @return the maximum number of up-draw indicators to display in the GUI for this tile */
    public int getMaxUpDraws() { return maxUpDraws; }

    /**
     * Returns the maximum number of down-draw indicators to display in the GUI for this tile.
     * @return the maximum number of down-draw indicators to display in the GUI for this tile */
    public int getMaxDownDraws() { return maxDownDraws; }
    /**
     * Creates a new empty {@code TileData} instance.
     * Required by Jackson for JSON deserialization.
     */
    public TileData() {
    }
}