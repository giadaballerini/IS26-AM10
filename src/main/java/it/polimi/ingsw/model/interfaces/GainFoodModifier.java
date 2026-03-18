package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.player.Player;

public interface GainFoodModifier {

    void apply(Player p, GainFood effect, GamePhaseEnum g);
}
