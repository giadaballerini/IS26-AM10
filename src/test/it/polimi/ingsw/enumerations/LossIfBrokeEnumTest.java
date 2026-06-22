package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LossIfBrokeEnumTest {

    @Mock
    Player player;

    @Mock
    LossIfBroke effect;

    @Test
    void testApply_LossIfBroke_noFood_appliesPpPenalty() {
        when(player.getNFood()).thenReturn(0);
        when(effect.getPpCost()).thenReturn(2);

        LossIfBrokeEnum.LOSS_IF_BROKE.apply(player, effect);

        verify(player).addPP(-2);
        verify(player, never()).addFood(anyInt());
    }

    @Test
    void testApply_LossIfBroke_hasFood_appliesFoodPenalty() {
        when(player.getNFood()).thenReturn(67);
        when(effect.getFoodCost()).thenReturn(1);

        LossIfBrokeEnum.LOSS_IF_BROKE.apply(player, effect);

        verify(player).addFood(-1);
        verify(player, never()).addPP(anyInt());
    }

    @Test
    void testIsOneTime_returnsFalse() {
        assertFalse(LossIfBrokeEnum.LOSS_IF_BROKE.isOneTime());
    }
}