package it.polimi.ingsw.model.action;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionTest {

    private Player player;
    private Action action;

    @BeforeEach
    void setUp() {
        player = new Player("Test", ColorPawnEnum.BLUE);
        action = new Action(player, DrawCardEnum.DOWN_DRAW);
    }

    @Test
    void testShouldGetType() {
        assertEquals(DrawCardEnum.DOWN_DRAW, action.getType());
    }

    @Test
    void testShouldGetOwner() {
        assertEquals(player, action.getOwner());
    }
}