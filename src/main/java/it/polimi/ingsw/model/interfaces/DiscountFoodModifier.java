package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.player.Player;

public interface DiscountFoodModifier {
    void apply(Player p, DiscountFood effect);
}
