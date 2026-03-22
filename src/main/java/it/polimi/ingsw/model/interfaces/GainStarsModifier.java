package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.player.Player;

public interface GainStarsModifier {
    public void apply(Player p, GainStars effect);
}
