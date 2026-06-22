package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardEffectInstantTest {

    @Mock
    Player player;

    @Mock
    Card card;


    @Test
    void testAbstractClass(){

        CardEffectInstant effectAbs = mock(CardEffectInstant.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));

        assertDoesNotThrow(() -> effectAbs.apply(player));
        assertDoesNotThrow(() -> effectAbs.apply(player, card));

        assertEquals(0, effectAbs.getPpAmount());

        assertFalse(effectAbs.isOneTime());
    }

}
