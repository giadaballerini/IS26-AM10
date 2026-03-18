package it.polimi.ingsw.model.action;

import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.player.Player;

public class Action {
    private Player owner;
    private DrawCardEnum type;
    public Action(Player owner, DrawCardEnum type) {
        this.owner = owner;
        this.type = type;
    }
}
