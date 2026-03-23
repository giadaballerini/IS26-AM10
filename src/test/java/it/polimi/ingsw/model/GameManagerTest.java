package it.polimi.ingsw.model;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.OccupiedTileException;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.pawn.Pawn;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.GameEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        List<GameEventListener> listeners = new ArrayList<GameEventListener>();
        List<Player> players = new ArrayList<Player>();
        gameManager = new GameManager(listeners, players, 2);
    }


    @Test
    @DisplayName("Test initGame")
    void testShouldInitGame() {
        gameManager.initGame();
        //assertNotNull(gameManager.);
        //serve getter
    }

    @Test
    @DisplayName("Test changeAge")
    void testShouldChangeAge() {
        int currAge = 1;
        List<Card> upperList = new ArrayList<Card>();
        assertNotNull(upperList);
        List<Card> lowerList = new ArrayList<>();
        assertNotNull(lowerList);
        List<CardEffectInteractive> instantEffects = new ArrayList<>();
        assertNotNull(instantEffects);
        List<CardEffectInstant> autoEffects = new ArrayList<>();
        assertNotNull(autoEffects);
        Building building = new Building(3, GamePhaseEnum.DRAW_PHASE, instantEffects, autoEffects, 2, 0, 0, CardTypeEnum.BUILDING);
        assertNotNull(building);
        upperList.add(building);
        lowerList.add(building);
        if (currAge < 3) {
            currAge++;
            assertEquals(2, currAge);
        }



        System.out.println("Current age: " + currAge);

        Iterator<Card> it = upperList.iterator();
        while (it.hasNext()) {
            Card c = it.next();
            if (c.getType().equals(CardTypeEnum.BUILDING)) {
                lowerList.add(c);
                 it.remove();
                assertEquals(2, lowerList.size());

            }
        }
        for(Card d : lowerList){
            assertEquals(CardTypeEnum.BUILDING, d.getType());
        }
        List<Building> buildings = new ArrayList<>();
        buildings.add(new Building(1, GamePhaseEnum.DRAW_PHASE, instantEffects, autoEffects, 2, 0, 0, CardTypeEnum.BUILDING));
        int finalCurrAge = currAge;
        upperList.addAll(buildings.stream()
                .filter(b -> b.getAge() == finalCurrAge)
                .toList());

        assertEquals(1, upperList.size());

    }

    @Test
    @DisplayName("Test nextPhase")
    void testShouldNextPhase() {
        gameManager.nextPhase();
        //serve getter

    }

    @Test
    @DisplayName("Test refillBoard")
    void testShouldRefillBoard() {
        
    }


    @Test
    @DisplayName("Test move")
    void testShouldMove() throws OccupiedTileException {
        Tile tile = new Tile('A', 2, null, null);
        assertNotNull(tile);
        Pawn pawn = new Pawn(null, null);
        tile.setPawn(pawn);
        assertNotNull(pawn);
        ArrayList<Tile> queue = new ArrayList<>();
        assertNotNull(queue);
        queue.add(tile);
        assertThrows(OccupiedTileException.class, () -> gameManager.move(tile));
        int i = 0;

        }


    @Test
    void testShouldFinalScoreCount() {
    }

    @Test
    void testShouldGameWinners() {
    }

    @Test
    void testShouldGetCurrentPlayer() {
    }

    @Test
    void testShouldAddListener() {
    }

    @Test
    void testShouldGetToDoActions() {
    }

    @Test
    void testShouldPlayEvent() {
    }

    @Test
    void testShouldCheckBoardTileEffects() {
    }

    @Test
    void testShouldCheckQueueTileEffects() {
    }

    @Test
    void testShouldDrawCard() {
    }
}