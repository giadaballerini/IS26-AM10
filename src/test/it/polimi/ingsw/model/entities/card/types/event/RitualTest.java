package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RitualTest {

    private final Ritual ritual = new Ritual(1, GamePhaseEnum.PLAY_EVENT,
            new ArrayList<>(), new ArrayList<>(), 1, 3, 4, CardTypeEnum.RITUAL);

    @Test
    void execEvent_RewardsMaxAndPenalisesMin() {
        Player p1 = new Player("p1", ColorPawnEnum.BLUE);
        Player p2 = new Player("p2", ColorPawnEnum.ORANGE);
        Player p3 = new Player("p3", ColorPawnEnum.WHITE);
        Player p4 = new Player("p4", ColorPawnEnum.PURPLE);
        Player p5 = new Player("p5", ColorPawnEnum.YELLOW);
        Player p6 = new Player("p6", ColorPawnEnum.ORANGE);

        p1.addStars(1); p2.addStars(3); p3.addStars(1);
        p4.addStars(3); p5.addStars(1); p6.addStars(2);

        p5.activatePpProtection();
        p3.activateDoubleShaman();
        p4.activateDoubleShaman();

        List<Player> players = List.of(p1, p2, p3, p4, p5, p6);

        ritual.execEvent(players, GamePhaseEnum.PLAY_EVENT);
        assertEquals(-3, p1.getPP());
        assertEquals(4,  p2.getPP());
        assertEquals(-3, p3.getPP());
        assertEquals(8,  p4.getPP());
        assertEquals(0,  p5.getPP());
        assertEquals(0,  p6.getPP());

        ritual.execEvent(players, GamePhaseEnum.END_ROUND);
        assertEquals(-6,  p1.getPP());
        assertEquals(8,   p2.getPP());
        assertEquals(-6,  p3.getPP());
        assertEquals(16,  p4.getPP());
        assertEquals(0,   p5.getPP());
        assertEquals(0,   p6.getPP());
    }

    @Test
    void execEvent_WrongPhase_DoesNothing() {
        Player p1 = new Player("p1", ColorPawnEnum.BLUE);
        Player p2 = new Player("p2", ColorPawnEnum.ORANGE);
        p1.addStars(1);
        p2.addStars(3);

        ritual.execEvent(List.of(p1, p2), GamePhaseEnum.SETUP_PHASE);

        assertEquals(0, p1.getPP());
        assertEquals(0, p2.getPP());
    }
}