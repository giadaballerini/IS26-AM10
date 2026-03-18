package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.player.Player;

public interface GainPPModifier {
    public void apply(Player p, GainPP effect, GamePhaseEnum phase);
}
