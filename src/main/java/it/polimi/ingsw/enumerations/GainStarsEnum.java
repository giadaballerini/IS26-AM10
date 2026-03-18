package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.interfaces.GainStarsModifier;
import it.polimi.ingsw.model.player.Player;

public enum GainStarsEnum implements GainStarsModifier {
    GAIN_STARS((p, e, g) -> {});


    private final GainStarsModifier modifier;
    GainStarsEnum(GainStarsModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void apply(Player p, GainStars effect, GamePhaseEnum g) {
        modifier.apply(p, effect, g);
    }
}
