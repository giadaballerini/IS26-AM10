package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.character.Hunter;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntTest {

    static class TestPlayer extends Player {
        public TestPlayer(String name, ColorPawnEnum color) { super(name, color); }
        public Village getVillage() { return myVillage; }
    }

    private final Hunt hunt = new Hunt(4, GamePhaseEnum.PLAY_EVENT,
            new ArrayList<>(), new ArrayList<>(), 2, 1, 1, CardTypeEnum.HUNT);

    private Hunter newHunter(int id) {
        return new Hunter(id, GamePhaseEnum.DRAW_PHASE,
                new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.HUNTER);
    }

    @Test
    void execEvent_CorrectPhase_GivesFoodAndPP() {
        TestPlayer p1 = new TestPlayer("P1", ColorPawnEnum.BLUE);
        p1.addCard(newHunter(1));
        p1.addCard(newHunter(2));
        p1.activateHuntBonus();

        TestPlayer p2 = new TestPlayer("P2", ColorPawnEnum.ORANGE);
        p2.addCard(newHunter(3));
        p2.addCard(newHunter(4));
        p2.addCard(newHunter(5));

        hunt.execEvent(List.of(p1, p2), GamePhaseEnum.PLAY_EVENT);

        assertEquals(4, p1.getNFood());
        assertEquals(4, p1.getPP());
        assertEquals(3, p2.getNFood());
        assertEquals(3, p2.getPP());
    }

    @Test
    void execEvent_WrongPhase_DoesNothing() {
        TestPlayer p = new TestPlayer("P", ColorPawnEnum.BLUE);
        p.addCard(newHunter(1));
        p.activateHuntBonus();

        hunt.execEvent(List.of(p), GamePhaseEnum.SETUP_PHASE);

        assertEquals(0, p.getNFood());
        assertEquals(0, p.getPP());
    }

    @Test
    void execEvent_NoHunters_ZeroGain() {
        TestPlayer p = new TestPlayer("P", ColorPawnEnum.BLUE);
        p.activateHuntBonus();

        hunt.execEvent(List.of(p), GamePhaseEnum.PLAY_EVENT);

        assertEquals(0, p.getNFood());
        assertEquals(0, p.getPP());
    }
}