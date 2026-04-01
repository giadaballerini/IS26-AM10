package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.ProtectPP;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProtectPPEnumTest {

    @Mock
    private Player player;

    @Mock
    private Card card;

    @Mock
    private ProtectPPEnum effect;

    @Test
    void testShouldAddProtection() {
        Player real = new Player("Player1", ColorPawnEnum.ORANGE);

        ProtectPPEnum.PP_PROTECTION.apply(real);

        assertTrue(real.getHasProtection());
    }

    @Test
    void ITestShouldIsOneTime() {
        ProtectPPEnum.PP_PROTECTION.apply(player);

        assertTrue(ProtectPPEnum.PP_PROTECTION.isOneTime());
    }
}