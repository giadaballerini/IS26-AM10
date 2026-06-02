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

import static org.junit.jupiter.api.Assertions.*;

class HuntTest {

    public class PlayerTest extends Player {
        public PlayerTest(String name, ColorPawnEnum color) {
            super(name, color);
        }
        public Village getVillage() { return myVillage; }
    }

    @Test
    void execEvent_WithHuntBonus() {
        Hunt hunt = new Hunt(4, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), new ArrayList<>(), 2, 1, 1, CardTypeEnum.HUNT);

        PlayerTest p = new PlayerTest("Test", ColorPawnEnum.BLUE);
        PlayerTest p2 = new PlayerTest("Test2", ColorPawnEnum.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(p);
        players.add(p2);

        Hunter hunter1 = new Hunter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, true, CardTypeEnum.HUNTER);
        Hunter hunter2 = new Hunter(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, false, CardTypeEnum.HUNTER);
        Hunter hunter3 = new Hunter(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, true, CardTypeEnum.HUNTER);

        // p: 2 hunter, con huntBonus attivato
        p.addCard(hunter1);
        p.addCard(hunter2);
        p.activateHuntBonus();

        // p2: 3 hunter, senza huntBonus
        p2.addCard(hunter1);
        p2.addCard(hunter3);
        p2.addCard(hunter2);

        hunt.execEvent(players, GamePhaseEnum.PLAY_EVENT);

        // p: 2 hunter * 1 foodGain + bonus 2*1 = 4 food; stesso per PP
        assertEquals(4, p.getNFood());
        assertEquals(4, p.getPP());

        // p2: 3 hunter * 1 foodGain = 3 food, nessun bonus
        assertEquals(3, p2.getNFood());
        assertEquals(3, p2.getPP());
    }

    @Test
    void execEvent_WrongPhase_DoesNothing() {
        Hunt hunt = new Hunt(4, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), new ArrayList<>(), 2, 1, 1, CardTypeEnum.HUNT);
        PlayerTest p = new PlayerTest("Test", ColorPawnEnum.BLUE);
        Hunter hunter = new Hunter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, true, CardTypeEnum.HUNTER);
        p.addCard(hunter);
        p.activateHuntBonus();

        hunt.execEvent(List.of(p), GamePhaseEnum.SETUP_PHASE);

        assertEquals(0, p.getNFood());
        assertEquals(0, p.getPP());
    }
}
