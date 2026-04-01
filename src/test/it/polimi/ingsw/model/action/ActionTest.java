package it.polimi.ingsw.model.action;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest {

    Player player = new Player("Test", ColorPawnEnum.BLUE);
    Action action = new Action(player, DrawCardEnum.DOWN_DRAW);


    @Test
    void testShouldGetType() {
        assertEquals(DrawCardEnum.DOWN_DRAW, action.getType());
    }

    @Test
    void testShouldGetOwner() {
        assertEquals(player, action.getOwner());
    }
}