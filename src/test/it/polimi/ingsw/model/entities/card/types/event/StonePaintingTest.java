package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.character.Painter;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StonePaintingTest {

    private final StonePainting sp = new StonePainting(1, GamePhaseEnum.END_ROUND,
            new ArrayList<>(), new ArrayList<>(), 2, 2, 5, 6, CardTypeEnum.STONE_PAINTING);

    private Painter newPainter(int id) {
        return new Painter(id, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
    }

    @Test
    void execEvent_RewardsAndPenalises_WithPaintBonus() {
        Player p1 = new Player("p1", ColorPawnEnum.BLUE);
        Player p2 = new Player("p2", ColorPawnEnum.ORANGE);
        p2.addCard(newPainter(1));
        p2.addCard(newPainter(2));
        p2.addCard(newPainter(3));
        p2.activatePaintBonus();

        sp.execEvent(List.of(p1, p2), GamePhaseEnum.END_ROUND);

        assertEquals(-6, p1.getPP());
        assertEquals(0,  p1.getNFood());
        assertEquals(15, p2.getPP());
        assertEquals(3,  p2.getNFood());
    }

    @Test
    void execEvent_NoPaintBonus_NoFood() {
        Player p = new Player("p", ColorPawnEnum.BLUE);
        p.addCard(newPainter(1));
        p.addCard(newPainter(2));
        p.addCard(newPainter(3));

        sp.execEvent(List.of(p), GamePhaseEnum.END_ROUND);

        assertEquals(15, p.getPP());
        assertEquals(0,  p.getNFood());
    }

    @Test
    void execEvent_WrongPhase_DoesNothing() {
        Player p = new Player("p", ColorPawnEnum.BLUE);
        sp.execEvent(List.of(p), GamePhaseEnum.SETUP_PHASE);
        assertEquals(0, p.getPP());
        assertEquals(0, p.getNFood());
    }
}