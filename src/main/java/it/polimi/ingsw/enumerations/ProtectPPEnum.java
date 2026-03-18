package it.polimi.ingsw.enumerations;


import it.polimi.ingsw.model.entities.card.effects.instant.ProtectPP;
import it.polimi.ingsw.model.interfaces.ProtectPPModifier;
import it.polimi.ingsw.model.player.Player;

public enum ProtectPPEnum implements ProtectPPModifier {
    PP_PROTECTION ((p, e, g) -> {});


    private final ProtectPPModifier modifier;
    ProtectPPEnum(ProtectPPModifier modifier) {
        this.modifier = modifier;
    }


    @Override
    public void apply(Player p, ProtectPP effect, GamePhaseEnum g) {
        modifier.apply(p, effect, g);
    }
}
