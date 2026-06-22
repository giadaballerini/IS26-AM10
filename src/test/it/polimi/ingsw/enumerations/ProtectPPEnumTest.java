package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProtectPPEnumTest {

    @Mock private Player player;

    @Test
    void testShouldActivatePpProtection() {
        Player real = new Player("Player1", ColorPawnEnum.ORANGE);

        ProtectPPEnum.PP_PROTECTION.apply(real);

        real.applyRitualLoss(10);
        assertEquals(0, real.getPP());
    }

    @Test
    void testWithoutProtection_applyRitualLoss_deductsPP() {
        Player real = new Player("Player1", ColorPawnEnum.ORANGE);

        real.applyRitualLoss(10);

        assertEquals(-10, real.getPP(), "senza protezione la perdita PP deve essere effettiva");
    }

    @Test
    void testShouldCallActivatePpProtection_OnMock() {
        ProtectPPEnum.PP_PROTECTION.apply(player);
        verify(player).activatePpProtection();
    }

    @Test
    void testShouldIsOneTime() {
        assertTrue(ProtectPPEnum.PP_PROTECTION.isOneTime());
    }
}