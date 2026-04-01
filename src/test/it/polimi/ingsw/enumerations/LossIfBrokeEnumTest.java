package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LossIfBrokeEnumTest {
    
    @Mock
    Player player;
    
    @Mock
    LossIfBroke effect;

    @Test
    void TestShouldApply_LossIfBroke_Poor() {
        when(player.getNFood()).thenReturn(0);
        
        LossIfBrokeEnum.LOSS_IF_BROKE.apply(player, effect);

        verify(player).addPP(-2);
    }
    
    @Test
    void TestShouldApply_LossIfBroke_Rich(){
        when(player.getNFood()).thenReturn(67);

        LossIfBrokeEnum.LOSS_IF_BROKE.apply(player, effect);

        verify(player).addFood(-1);
    }

    @Test
    void TestShouldIsOneTime() {
        when(player.getNFood()).thenReturn(67);

        LossIfBrokeEnum.LOSS_IF_BROKE.apply(player, effect);

        verify(player).addFood(-1);
        assertFalse(LossIfBrokeEnum.LOSS_IF_BROKE.isOneTime());
    }
}