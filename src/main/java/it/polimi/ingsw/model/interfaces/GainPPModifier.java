package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.player.Player;

public interface GainPPModifier {
    void apply(Player p, GainPP effect, Card c);
}
