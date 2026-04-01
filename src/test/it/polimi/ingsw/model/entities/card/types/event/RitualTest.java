package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RitualTest {

    @Test
    void execEvent() {
        Player p1 = new Player("p1", ColorPawnEnum.BLUE);
        Player p2 = new Player("p2", ColorPawnEnum.ORANGE);
        Player p3 = new Player("p3", ColorPawnEnum.WHITE);
        Player p4 = new Player("p4", ColorPawnEnum.PURPLE);
        Player p5 = new Player("p5", ColorPawnEnum.YELLOW);
        Player p6 = new Player("p6", ColorPawnEnum.ORANGE);
       List<Player> players = new ArrayList<>();
       players.add(p1);
       players.add(p2);
       players.add(p3);
       players.add(p4);
       players.add(p5);
       players.add(p6);
       p6.addStars(2);
       p1.addStars(1);
       p3.addStars(1);
       p5.addStars(1);
       p5.addProtection();
       p3.addDouble();
       p2.addStars(3);
       p4.addStars(3);
       p4.addDouble();
       Ritual ritual = new Ritual(1, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), new ArrayList<>(), 1, 3, 4, CardTypeEnum.RITUAL);
       ritual.execEvent(players, GamePhaseEnum.PLAY_EVENT);
       assertEquals(-3, p1.getPP());
       assertEquals(4, p2.getPP());
       assertEquals(-6, p3.getPP());
       assertEquals(8, p4.getPP());
       assertEquals(0, p5.getPP());
       assertEquals(0, p6.getPP());

       ritual.execEvent(players, GamePhaseEnum.END_ROUND);
       assertEquals(-6, p1.getPP());
       assertEquals(8, p2.getPP());
       assertEquals(-12, p3.getPP());
       assertEquals(16, p4.getPP());
       assertEquals(0, p5.getPP());
       assertEquals(0, p6.getPP());

       ritual.execEvent(players, GamePhaseEnum.SETUP_PHASE);
        assertEquals(-6, p1.getPP());
        assertEquals(8, p2.getPP());
        assertEquals(-12, p3.getPP());
        assertEquals(16, p4.getPP());
        assertEquals(0, p5.getPP());
        assertEquals(0, p6.getPP());


    }
}