package it.polimi.ingsw.model.interfaces;


import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;

public interface InteractiveEffectModifier {
    Action apply(Player p, DrawCardEnum drawCard);
}
