package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.player.Player;

public interface LossIfBrokeModifier {
    public void apply(Player p, LossIfBroke effect);
}
