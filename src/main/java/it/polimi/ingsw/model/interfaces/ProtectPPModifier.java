package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.ProtectPP;
import it.polimi.ingsw.model.player.Player;

public interface ProtectPPModifier {
    public void apply(Player p);
}
