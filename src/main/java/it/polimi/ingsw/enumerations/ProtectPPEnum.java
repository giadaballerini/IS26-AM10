package it.polimi.ingsw.enumerations;


import it.polimi.ingsw.model.interfaces.ProtectPPModifier;
import it.polimi.ingsw.model.player.Player;

public enum ProtectPPEnum implements ProtectPPModifier {
    PP_PROTECTION (Player::activatePpProtection);


    private final ProtectPPModifier modifier;
    ProtectPPEnum(ProtectPPModifier modifier) {
        this.modifier = modifier;
    }


    @Override
    public void apply(Player p) {
        modifier.apply(p);
    }
    public boolean isOneTime(){return true;}
}
