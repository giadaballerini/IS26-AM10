package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.interfaces.LossIfBrokeModifier;
import it.polimi.ingsw.model.player.Player;

public enum LossIfBrokeEnum implements LossIfBrokeModifier{

    LOSS_IF_BROKE((p, e) -> {
        if(p.getNFood() == 0){
            p.addPP(-2);
        } else p.addFood(-1);
    });

    private final LossIfBrokeModifier modifier;
    LossIfBrokeEnum(LossIfBrokeModifier modifier){
        this.modifier = modifier;
    }

    public void apply(Player p, LossIfBroke effect) {
        modifier.apply(p, effect);
    }
    public boolean isOneTime(){return false;}

}
