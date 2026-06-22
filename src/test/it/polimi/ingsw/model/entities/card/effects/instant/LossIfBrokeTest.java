package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.LossIfBrokeEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LossIfBrokeTest {

    @Test
    void apply() {
        Player p = new Player("Player", ColorPawnEnum.ORANGE);
        p.addFood(2);
        LossIfBroke test = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);
        test.apply(p);
        assertEquals(0, p.getNFood());
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
    void isOneTime() {
        LossIfBroke test = new LossIfBroke(1, 2, LossIfBrokeEnum.LOSS_IF_BROKE);
        assertFalse(test.isOneTime());

    }
}