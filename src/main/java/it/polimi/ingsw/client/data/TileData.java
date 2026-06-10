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
    @JsonProperty("cardSlot")
    private HighlightBounds cardSlot;

    /**
     * Bounds of the rectangle highlighting the arrow area.
     * {@code null} for tiles that have no draw actions (e.g. Tile 0).
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
     * All coordinates are in pixels, relative to a tile rendered at 110px width.
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
    }

    /** @return the display description of this tile */
    public String getDescription() { return description; }

    /** @return the unique identifier of this tile */
    public int getId() { return id; }

    /** @return the {@link HighlightBounds} of the card slot area */
    public HighlightBounds getCardSlot() { return cardSlot; }

    /**
     * @return the {@link HighlightBounds} of the arrow area,
     *         or {@code null} if this tile has no draw actions
     */
    public HighlightBounds getArrowArea() { return arrowArea; }

    /**
     * @return {@code true} if this tile has an arrow area (i.e. supports draw actions),
     *         {@code false} otherwise
     */
    public boolean hasArrows() { return arrowArea != null; }

    /** @return the maximum number of up-draw indicators to display in the GUI for this tile */
    public int getMaxUpDraws() { return maxUpDraws; }

    /** @return the maximum number of down-draw indicators to display in the GUI for this tile */
    public int getMaxDownDraws() { return maxDownDraws; }
}