package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.interfaces.LossIfBrokeModifier;
import it.polimi.ingsw.model.player.Player;

public enum LossIfBrokeEnum implements LossIfBrokeModifier{

    LOSS_IF_BROKE((p, e, g) -> {});

    private final LossIfBrokeModifier modifier;
    LossIfBrokeEnum(LossIfBrokeModifier modifier){
        this.modifier = modifier;
    }

    public void apply(Player p, LossIfBroke effect, GamePhaseEnum g) {
        modifier.apply(p, effect, g);
    }


}
