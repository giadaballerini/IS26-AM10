package it.polimi.ingsw.network.dto;

import java.io.Serializable;

public class TileDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean occupied;
    private int id;
    private int minPlayers;
    private String player;

    private int upDraws;
    private int downDraws;
    private int foodAmount;

    public TileDTO(boolean occupied, int id, int minPlayers, String player, int upDraws, int downDraws, int foodAmount) {
        this.occupied = occupied;
        this.id = id;
        this.minPlayers = minPlayers;
        this.player = player;
        this.upDraws = upDraws;
        this.downDraws = downDraws;
        this.foodAmount = foodAmount;
    }

    public TileDTO(TileDTO tileDTO, String player, boolean occupied){
        this.occupied = occupied;
        this.id = tileDTO.getId();
        this.minPlayers = tileDTO.getMinPlayers();
        this.player = player;
        this.upDraws = tileDTO.getUpDraws();
        this.downDraws = tileDTO.getDownDraws();
        this.foodAmount = tileDTO.getFoodAmount();
    }

    public boolean isOccupied() {
        return occupied;
    }

    public int getId(){
        return id;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public String getPlayer() {
        return player;
    }

    public int getUpDraws() {
        return upDraws;
    }

    public int getDownDraws() {
        return downDraws;
    }

    public int getFoodAmount() {
        return foodAmount;
    }
}