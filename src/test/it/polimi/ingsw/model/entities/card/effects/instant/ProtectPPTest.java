package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.ProtectPPEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProtectPPTest {

    @Mock
    ProtectPP mockEff;

    @Test
    void apply() {
        Player real = new Player("Player1", ColorPawnEnum.ORANGE);
        ProtectPP eff = new ProtectPP(ProtectPPEnum.PP_PROTECTION);
        eff.apply(real);
        assertTrue(real.getHasProtection());

    }

    @Test
    void displayEffect() {

        ProtectPP eff = new ProtectPP(ProtectPPEnum.PP_PROTECTION);

        mockEff.displayEffect();
        eff.displayEffect();

        verify(mockEff).displayEffect();     }

    @Test
    void isOneTime() {
        ProtectPP eff = new ProtectPP(ProtectPPEnum.PP_PROTECTION);
        assertTrue(eff.isOneTime());
    }
}
