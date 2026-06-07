package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QTileData {
    @JsonProperty("id")
    private int id;

    @JsonProperty("caption")
    private String description;

    @JsonProperty("slot0x")
    private double slot0X;

    @JsonProperty("slot0y")
    private double slot0Y;

    @JsonProperty("slot1x")
    private double slot1X;

    @JsonProperty("slot1y")
    private double slot1Y;

    @JsonProperty("slot2x")
    private double slot2X;

    @JsonProperty("slot2y")
    private double slot2Y;

    @JsonProperty("slot3x")
    private double slot3X;

    @JsonProperty("slot3y")
    private double slot3Y;

    @JsonProperty("slot4x")
    private double slot4X;

    @JsonProperty("slot4y")
    private double slot4Y;

    public String getDescription() { return description; }
    public int getId() { return id; }

    public double getSlotX(int index){

        return switch (index) {
            case 0 -> slot0X;
            case 1 -> slot1X;
            case 2 -> slot2X;
            case 3 -> slot3X;
            case 4 -> slot4X;
            default -> 0.0;
        };
    }

    public double getSlotY(int index){

        return switch (index) {
            case 0 -> slot0Y;
            case 1 -> slot1Y;
            case 2 -> slot2Y;
            case 3 -> slot3Y;
            case 4 -> slot4Y;
            default -> 0.0;
        };
    }
}
