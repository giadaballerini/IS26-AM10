package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TileData {

    @JsonProperty("id")
    private int id;

    @JsonProperty("caption")
    private String description;

    /** Bounds del rettangolo che evidenzia il card slot (sempre presente). */
    @JsonProperty("cardSlot")
    private HighlightBounds cardSlot;

    /**
     * Bounds del rettangolo che evidenzia l'area frecce.
     * null per le tile senza frecce (es. Tile_0).
     */
    @JsonProperty("arrowArea")
    private HighlightBounds arrowArea;

    @JsonProperty("maxUpDraws")
    private int maxUpDraws;

    @JsonProperty("maxDownDraws")
    private int maxDownDraws;



    public static class HighlightBounds {
        /** Margine dall'alto nella tile renderizzata (px, tile larga 110px). */
        @JsonProperty("topMargin")
        public double topMargin;

        @JsonProperty("leftMargin")
        public double leftMargin;

        /** Larghezza del rettangolo (px). */
        @JsonProperty("width")
        public double width;

        @JsonProperty("height")
        public double height;
    }


    public String getDescription(){ return description; }
    public int    getId(){ return id; }
    public HighlightBounds getCardSlot(){ return cardSlot; }
    public HighlightBounds getArrowArea(){ return arrowArea; }
    public boolean hasArrows(){ return arrowArea != null; }
    public int getMaxUpDraws()   { return maxUpDraws; }
    public int getMaxDownDraws() { return maxDownDraws; }
}