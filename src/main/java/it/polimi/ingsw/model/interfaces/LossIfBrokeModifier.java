package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.player.Player;

public interface LossIfBrokeModifier {
    void apply(Player p, LossIfBroke effect);
}
