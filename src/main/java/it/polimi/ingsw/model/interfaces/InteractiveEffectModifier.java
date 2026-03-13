package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.GainFoodEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

public interface InteractiveEffectModifier {
    Action apply(Player p, Card card, GainFoodEnum g);
}
