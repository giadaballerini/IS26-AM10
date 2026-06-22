package it.polimi.ingsw.model;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GameInitializerTest {

    @ParameterizedTest
    @ValueSource(ints= {2, 3, 4, 5})
    void testInitDeck(int num) {
        GameInitializer gameInitializer = new GameInitializer();

        List<Card> deck = gameInitializer.initDeck(num);

        int deckSize = switch (num) {
            case 2 -> 63;
            case 3 -> 74;
            case 4 -> 85;
            case 5 -> 96;
            default -> 0;
        };

        assertEquals(deckSize,deck.size());

    }
    @ParameterizedTest
    @ValueSource(ints= {0,1})
    void testInitDeckInvalid(int num) {
        GameInitializer gameInitializer = new GameInitializer();
        List<Card> deck = gameInitializer.initDeck(num);
        assertEquals(0,deck.size());

    }

    @Test
    void initLowerListEmptyDeck() {
        GameInitializer gi = new GameInitializer();
        List<Card> result = gi.initLowerList(new ArrayList<>(), new ArrayList<>(), 3);
        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void testInitBoardInvalid(int num) {
        GameInitializer gi = new GameInitializer();
        List<Tile> board = gi.initBoard(num);
        assertEquals(0, board.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void initBuildingDeck(int num) {
        GameInitializer gameInitializer = new GameInitializer();

        int expectedSize = switch (num) {
            case 2 -> 6;
            case 3 -> 8;
            case 4 -> 9;
            case 5 -> 10;
            default -> 0;
        };

        List<Card> buildings = gameInitializer.initBuildingDeck(num);
        assertEquals(expectedSize, buildings.size());
    }
    @ParameterizedTest
    @ValueSource(ints = {0,1})
    void initBuildingDeckInvalid(int num) {
        GameInitializer gameInitializer = new GameInitializer();
        List<Card> buildings = gameInitializer.initBuildingDeck(num);
        assertEquals(0,buildings.size());
    }

    @ParameterizedTest
    @ValueSource(ints= {2, 3, 4, 5})
    void testInitQueue(int num) {
        GameInitializer gameInitializer = new GameInitializer();

        int minId = switch(num) {
            case 2 -> 0;
            case 3 -> 2;
            case 4 -> 5;
            case 5 -> 9;
            default -> -1;
        };

        int maxId =  switch(num) {
            case 2 -> 1;
            case 3 -> 4;
            case 4 -> 8;
            case 5 -> 13;
            default -> -1;
        };

        List<Tile> queue = gameInitializer.initQueue(num);
        assertEquals(num,queue.size());
        for(Tile t: queue){
            assertTrue(t.getId() >= minId && t.getId() <= maxId);
        }

    }
    @ParameterizedTest
    @ValueSource(ints= {0,1})
    void testInitQueueInvalid(int num) {
        GameInitializer gameInitializer = new GameInitializer();
        List<Tile> queue = gameInitializer.initQueue(num);
        assertEquals(0,queue.size());
    }

    @ParameterizedTest
    @ValueSource(ints= {2, 3, 4, 5})
    void testInitBoard(int num) {
        GameInitializer gameInitializer = new GameInitializer();
        List<Tile> board = gameInitializer.initBoard(num);

        for(Tile t: board){
            assertTrue(t.getMinPlayers() <= num);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void testInitUpperListValid(int numPlayers) {

        GameInitializer gi = new GameInitializer();
        List<Card> deck = gi.initDeck(numPlayers);
        List<Card> buildings = gi.initBuildingDeck(numPlayers);
        List<Card> upperList = new ArrayList<>();

        int initialDeckSize = deck.size();
        int initialBuildingsSize = buildings.size();

        int expectedCardsFromDeck = numPlayers + 4;
        int expectedBuildings = switch (numPlayers) {
            case 2 -> 1;
            case 3, 4, 5 -> 2;
            default -> 0;
        };

        gi.initUpperList(deck, buildings, upperList, numPlayers);

        assertEquals(expectedCardsFromDeck + expectedBuildings, upperList.size());

        assertEquals(initialDeckSize - expectedCardsFromDeck, deck.size());
        assertEquals(initialBuildingsSize - expectedBuildings, buildings.size());
    }
    @ParameterizedTest
    @ValueSource(ints = {0,1})
    void testInitUpperListInvalid(int numPlayers) {
        GameInitializer gi = new GameInitializer();
        List<Card> deck = gi.initDeck(numPlayers);
        List<Card> buildings = gi.initBuildingDeck(numPlayers);
        List<Card> upperList = new ArrayList<>();

        gi.initUpperList(deck, buildings, upperList, numPlayers);
        assertEquals(0,upperList.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void initLowerList(int numPlayers) {
        GameInitializer gi = new GameInitializer();
        List<Card> deck = gi.initDeck(numPlayers);
        List<Card> upperList = new ArrayList<>();

        int expectedLowerSize = numPlayers + 1;

        List<Card> lowerList = gi.initLowerList(deck, upperList, numPlayers);

        assertEquals(expectedLowerSize, lowerList.size());

        for (Card c : lowerList) {
            assertFalse(c.getType().isEvent(), "La lowerList non dovrebbe contenere eventi");
        }

        for (Card c : upperList) {
            assertTrue(c.getType().isEvent(), "Le carte aggiunte a upperList durante initLowerList devono essere eventi");
        }
    }

    @Test
    void testInitUpperList_deckExhaustedBeforeTarget() {
        GameInitializer gi = new GameInitializer();

        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Card c = mock(Card.class);
            when(c.getType()).thenReturn(CardTypeEnum.GATHERER);
            deck.add(c);
        }

        List<Card> buildings = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Card b = mock(Card.class);
            when(b.getType()).thenReturn(CardTypeEnum.BUILDING);
            buildings.add(b);
        }

        List<Card> upperList = new ArrayList<>();
        int numPlayers = 3;

        gi.initUpperList(deck, buildings, upperList, numPlayers);

        assertTrue(deck.isEmpty(), "Il deck deve essere esaurito");
        assertEquals(3 + 2, upperList.size(),
                "upperList deve contenere tutte le carte dal deck + gli edifici");
    }

    @Test
    void testInitLowerList_deckExhaustedDuringLoop() {
        GameInitializer gi = new GameInitializer();

        List<Card> deck = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            Card c = mock(Card.class);
            when(c.getType()).thenReturn(CardTypeEnum.GATHERER);
            deck.add(c);
        }

        List<Card> upperList = new ArrayList<>();
        int numPlayers = 3;

        List<Card> lowerList = gi.initLowerList(deck, upperList, numPlayers);

        assertTrue(deck.isEmpty(), "Il deck deve essere vuoto dopo l'esaurimento");
        assertEquals(2, lowerList.size(),
                "La lowerList deve contenere solo le carte disponibili nel deck");
        assertTrue(upperList.isEmpty());
    }
}