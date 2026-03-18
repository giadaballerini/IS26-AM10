package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.interfaces.GainPPModifier;
import it.polimi.ingsw.model.player.Player;

public enum GainPPEnum implements GainPPModifier {
    PP_FOR_CAT((p, e, g) -> {}),
    PP_FOR_SET((p, e, g) -> {}),
    PP_FLAT((p, e, g) -> {}),
    DOUBLE_PP_SHAMAN((p, e, g) -> {}),
    DOUBLE_BUILDER((p, e, g) -> {}),
    PP_CRAFTER((p, e, g) -> {});

    private final GainPPModifier modifier;
    GainPPEnum(GainPPModifier modifier) {
        this.modifier = modifier;
    }


    @Override
    public void apply(Player p, GainPP effect, GamePhaseEnum g) {
        modifier.apply(p, effect, g);
    }
}
