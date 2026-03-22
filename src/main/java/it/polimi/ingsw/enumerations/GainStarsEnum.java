package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.interfaces.GainStarsModifier;
import it.polimi.ingsw.model.player.Player;

public enum GainStarsEnum implements GainStarsModifier {
    GAIN_STARS((p, e) -> {
        p.addStars(e.getStarsAmount());
    }){ public boolean isOneTime(){return true;}};


    private final GainStarsModifier modifier;
    GainStarsEnum(GainStarsModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void apply(Player p, GainStars effect) {
        modifier.apply(p, effect);
    }

    public boolean isOneTime(){return false;};
}
