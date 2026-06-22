package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.ProtectPPEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtectPPTest {

    @Test
    void apply() {
        Player real = new Player("Player1", ColorPawnEnum.ORANGE);
        ProtectPP eff = new ProtectPP(ProtectPPEnum.PP_PROTECTION);
        eff.apply(real);
    }

    @Test
    void isOneTime() {
        ProtectPP eff = new ProtectPP(ProtectPPEnum.PP_PROTECTION);
        assertTrue(eff.isOneTime());
    }
}
