package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.LossIfBrokeEnum;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LossIfBrokeTest {

    @Mock
    LossIfBroke mockEff;

    @Test
    void apply() {
        Player p = new Player("Player", ColorPawnEnum.ORANGE);
        p.addFood(2);
        LossIfBroke test = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);
        test.apply(p);
        assertEquals(1, p.getNFood());
    }

    @Test
    void getPpCost() {
        LossIfBroke test = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);
        assertEquals(1, test.getPpCost() );
    }

    @Test
    void getFoodCost() {
        LossIfBroke test = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);
        assertEquals(2, test.getFoodCost() );
    }

    @Test
    void displayEffect() {
        LossIfBroke eff = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);

        mockEff.displayEffect();
        eff.displayEffect();

        verify(mockEff).displayEffect();     }

    @Test
    void isOneTime() {
        LossIfBroke test = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);
        assertFalse(test.isOneTime());

    }
}