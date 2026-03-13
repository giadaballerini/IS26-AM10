package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.GainFoodEnum;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.entities.card.Card;

public interface InstantEffectModifier {
    void apply(Player p, Card card, GainFoodEnum g);
}
