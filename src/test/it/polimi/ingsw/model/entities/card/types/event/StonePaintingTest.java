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
    void execEvent() {
        Player p1 = new Player("p1", ColorPawnEnum.BLUE);
        Player p2 = new Player("p2", ColorPawnEnum.ORANGE);
        List<Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        Painter pa = new Painter(1,GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        Painter pa1 = new Painter(1,GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);
        Painter pa2 = new Painter(1,GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.PAINTER);

        p2.addCard(pa);
        p2.addCard(pa1);
        p2.addCard(pa2);

        StonePainting sp = new StonePainting(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 5, 6, CardTypeEnum.STONE_PAINTING);

        sp.execEvent(players, GamePhaseEnum.END_ROUND);

        assertEquals(-6, p1.getPP());
        assertEquals(15, p2.getPP());
        sp.execEvent(players, GamePhaseEnum.SETUP_PHASE);

        assertEquals(-6, p1.getPP());
        assertEquals(15, p2.getPP());

    }
}