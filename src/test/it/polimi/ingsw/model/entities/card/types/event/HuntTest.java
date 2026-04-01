package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.enumerations.ProtectPPEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.ProtectPP;
import it.polimi.ingsw.model.entities.card.types.building.Building;
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

        public Village getVillage(){
            return myVillage;
        }

    }


    @Test
    void execEvent() {


        Hunt hunt = new Hunt(4, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), new ArrayList<>(), 2, 1, 1, CardTypeEnum.HUNT);
        PlayerTest p = new PlayerTest("Test", ColorPawnEnum.BLUE);
        PlayerTest p2 = new PlayerTest("Test2", ColorPawnEnum.BLUE);
        List<Player> players = new ArrayList<>();
        players.add(p);
        players.add(p2);
        Hunter hunter = new Hunter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, true, CardTypeEnum.HUNTER);
        Hunter hunter1 = new Hunter(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, true, CardTypeEnum.HUNTER);
        Hunter hunter2 = new Hunter(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, false, CardTypeEnum.HUNTER);

        List<CardEffectInstant> proc = new ArrayList<>();
        proc.add(new ProtectPP(ProtectPPEnum.PP_PROTECTION));

        p.addCard(hunter);
        p.addCard(hunter2);
        p2.addCard(hunter);
        p2.addCard(hunter1);
        p2.addCard(hunter2);

        p.setHuntFlag(true);

        hunt.execEvent(players ,GamePhaseEnum.PLAY_EVENT);

        assertTrue(p.hasHuntFlag());
        assertFalse(p2.getHasProtection());
        assertEquals(4, p.getNFood());
        assertEquals(4, p.getPP());

        assertEquals(3, p2.getNFood());
        assertEquals(3, p2.getPP());

        hunt.execEvent(players ,GamePhaseEnum.SETUP_PHASE);

        assertTrue(p.hasHuntFlag());
        assertFalse(p2.getHasProtection());
        assertEquals(4, p.getNFood());
        assertEquals(4, p.getPP());

        assertEquals(3, p2.getNFood());
        assertEquals(3, p2.getPP());

    }
}