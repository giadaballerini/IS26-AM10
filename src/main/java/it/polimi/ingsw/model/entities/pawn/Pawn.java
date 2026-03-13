package it.polimi.ingsw.model.entities.pawn;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.model.player.Player;

public class Pawn {
    private Player p;
    private ColorPawnEnum color;

    public Pawn(Player p, ColorPawnEnum color) {
        this.p = p;
        this.color = color;
    }
    public Player getP() {
        return p;
    }
    public ColorPawnEnum getColor() {
        return color;
    }

}
