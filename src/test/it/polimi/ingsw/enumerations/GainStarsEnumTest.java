package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GainStarsEnumTest {

    @Mock
    GainStars effect;

    @Mock
    Player player;


    @Test
    void testShouldAddStars() {
        when(effect.getStarsAmount()).thenReturn(6767);

        GainStarsEnum.GAIN_STARS.apply(player, effect);

        verify(player).addStars(6767);
    }

    @Test
    void TestShouldIsOneTime() {
        when(effect.getStarsAmount()).thenReturn(67);

        GainStarsEnum.GAIN_STARS.apply(player, effect);

        verify(player).addStars(67);
        assertTrue(GainStarsEnum.GAIN_STARS.isOneTime());
    }
}