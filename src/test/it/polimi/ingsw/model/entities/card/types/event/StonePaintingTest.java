package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.character.Painter;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StonePaintingTest {

    @Test
    void execEvent_WithPaintBonus() {
        Player p1 = new Player("p1", ColorPawnEnum.BLUE);   // 0 painter, senza bonus
        Player p2 = new Player("p2", ColorPawnEnum.ORANGE); // 3 painter, con paintBonus

        Painter pa  = new Painter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        Painter pa1 = new Painter(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        Painter pa2 = new Painter(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        p2.addCard(pa);
        p2.addCard(pa1);
        p2.addCard(pa2);
        p2.activatePaintBonus(); // p2 ottiene cibo per ogni painter

        // StonePainting: nPainterSup=2, ppGain=5, ppLoss=6
        StonePainting sp = new StonePainting(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 5, 6, CardTypeEnum.STONE_PAINTING);

        sp.execEvent(List.of(p1, p2), GamePhaseEnum.END_ROUND);

        // p1: 0 painter <= nPainterSup(2) → -6 PP; nessun paintBonus
        assertEquals(-6, p1.getPP());
        assertEquals(0, p1.getNFood());

        // p2: 3 painter > nPainterSup(2) → +5*3=15 PP; paintBonus → +3 food
        assertEquals(15, p2.getPP());
        assertEquals(3, p2.getNFood());
    }

    @Test
    void execEvent_WithoutPaintBonus() {
        Player p = new Player("p", ColorPawnEnum.BLUE);
        Painter pa = new Painter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        Painter pa1 = new Painter(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        Painter pa2 = new Painter(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        p.addCard(pa);
        p.addCard(pa1);
        p.addCard(pa2);
        // paintBonus NON attivato

        StonePainting sp = new StonePainting(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 5, 6, CardTypeEnum.STONE_PAINTING);
        sp.execEvent(List.of(p), GamePhaseEnum.END_ROUND);

        assertEquals(15, p.getPP());
        assertEquals(0, p.getNFood()); // nessun cibo perché paintBonus non è attivo
    }

    @Test
    void execEvent_WrongPhase_DoesNothing() {
        Player p = new Player("p", ColorPawnEnum.BLUE);
        StonePainting sp = new StonePainting(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 5, 6, CardTypeEnum.STONE_PAINTING);
        sp.execEvent(List.of(p), GamePhaseEnum.SETUP_PHASE);
        assertEquals(0, p.getPP());
        assertEquals(0, p.getNFood());
    }
}
