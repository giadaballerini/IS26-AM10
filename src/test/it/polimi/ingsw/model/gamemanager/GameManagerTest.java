package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.exceptions.OccupiedTileException;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.entities.card.types.character.Painter;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.entities.card.types.event.Feast;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.observer.ModelObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GameManagerTest {
    private static class TestableGameNotifier extends GameNotifier {
        public TestableGameNotifier(List<ModelObserver> listeners) {
            super(listeners);
        }
        public List<ModelObserver> getListeners() { return listeners; }
    }
    private static class TestableGameManager extends GameManager {
        public TestableGameManager(List<ModelObserver> l, List<Player> p, int n) {
            super(l, p, n, () -> {});
            this.currPhaseState = new SetupPhaseState();
        }

        // stato
        public List<Tile> getBoard()            { return state.getBoard(); }
        public int getCurrAge()                 { return state.getCurrAge(); }
        public List<Player> getPlayers()        { return state.getPlayers(); }
        public List<Card> getUpperList()        { return state.getUpperList(); }
        public List<Card> getLowerList()        { return state.getLowerList(); }
        public List<Tile> getQueue()            { return state.getQueue(); }
        public List<Card> getBuildings()        { return state.getBuildings(); }
        public List<Card> getDeck()             { return state.getDeck(); }
        public GamePhaseState getCurrPhase()    { return this.currPhaseState; }
        public Player getCurrentPlayer()        { return state.getCurrPlayer(); }

        // toDoActions — esposta direttamente per i test
        public List<Action> getToDoActions()    { return state.getToDoActions(); }
        public void addToDoAction(Action a)     { state.getToDoActions().add(a); }
        public void clearToDoActions()          { state.getToDoActions().clear(); }

        // listeners — esposto dal notifier tramite TestableGameNotifier
        public List<ModelObserver> getListeners() { return ((TestableGameNotifier) notifier).getListeners(); }

        public void setTurn(int i)              { state.setCurrTurn(i); }

        public void consumeAction() {
            List<Action> actions = state.getToDoActions();
            if (!actions.isEmpty()) actions.removeFirst();
        }
    }

    private TestableGameManager tgm;

    @BeforeEach
    void setUp() {
        List<ModelObserver> listeners = new ArrayList<>();
        List<Player> players = new ArrayList<>();
        players.add(new Player("Player1", ColorPawnEnum.BLUE));
        players.add(new Player("Player2", ColorPawnEnum.ORANGE));
        tgm = new TestableGameManager(listeners, players, 2);
    }

    // ── initGame ─────────────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("Test initGame")
    void testShouldInitGameForValidValues(int value) {
        List<Player> players = new ArrayList<>();
        List<ModelObserver> listeners = new ArrayList<>();
        for (int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        tgm = new TestableGameManager(listeners, players, value);
        tgm.initGame();

        assertNotNull(tgm.getBoard());
        assertFalse(tgm.getBoard().isEmpty());

        int expectedUpper = value + 4;
        int sub = switch (value) {
            case 2    -> 1;
            case 3, 4 -> 2;
            case 5    -> 3;
            default   -> 0;
        };
        assertEquals(expectedUpper, tgm.getUpperList().size() - sub);

        assertFalse(tgm.getLowerList().isEmpty());
        assertEquals(value + 1, tgm.getLowerList().size());

        assertEquals(1, tgm.getCurrAge());
        assertNotNull(tgm.getCurrentPlayer());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Test notInitGame")
    void testShouldNotInitGameForInvalidValues(int value) {
        List<Player> players = new ArrayList<>();
        List<ModelObserver> listeners = new ArrayList<>();
        for (int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        tgm = new TestableGameManager(listeners, players, value);
        tgm.initGame();

        assertEquals(0, tgm.getDeck().size());
        assertEquals(0, tgm.getBuildings().size());
        assertTrue(tgm.getBoard().isEmpty());
        assertTrue(tgm.getQueue().isEmpty());
        assertTrue(tgm.getUpperList().isEmpty());
        assertTrue(tgm.getLowerList().isEmpty());
    }

    // ── changeAge ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Test changeAge 1 to 2")
    void testShouldChangeAge1to2() {
        tgm.initGame();

        Building build1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.SETUP_PHASE, 1, 4, 2, new ArrayList<>(), new ArrayList<>());
        tgm.getUpperList().add(build1);

        Building build2 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.SETUP_PHASE, 2, 4, 2, new ArrayList<>(), new ArrayList<>());
        tgm.getBuildings().add(build2);

        tgm.changeAge();

        assertEquals(2, tgm.getCurrAge());
        assertFalse(tgm.getUpperList().contains(build1));
        assertTrue(tgm.getLowerList().contains(build1));
        assertTrue(tgm.getUpperList().contains(build2));
        assertFalse(tgm.getLowerList().contains(build2));
    }

    @Test
    @DisplayName("Test changeAge 2 to 3")
    void testShouldChangeAge2to3() {
        tgm.initGame();
        tgm.changeAge();

        Building build3 = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());
        tgm.getLowerList().add(build3);

        tgm.changeAge();

        assertEquals(3, tgm.getCurrAge());
        assertFalse(tgm.getLowerList().contains(build3));
    }

    @Test
    @DisplayName("Test changeAge 3 stays 3")
    void testShouldChangeAge3to3() {
        tgm.initGame();
        tgm.changeAge();

        Building build3 = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());
        tgm.getLowerList().add(build3);
        tgm.changeAge();

        assertEquals(3, tgm.getCurrAge());
        assertFalse(tgm.getLowerList().contains(build3));

        tgm.getLowerList().add(build3);
        tgm.changeAge();

        assertEquals(3, tgm.getCurrAge());
        assertFalse(tgm.getLowerList().contains(build3));
    }

    // ── nextPhase ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Test nextPhase setup stays setup")
    void testShouldNextPhaseSetup() {
        tgm.initGame();
        tgm.nextPhase();
        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Test nextPhase setup to draw")
    void testShouldNextPhaseSetupToDraw() {
        tgm.initGame();
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        Tile t = new Tile(2, 1, new ArrayList<>(), List.of(eff), false, "");
        tgm.getBoard().addFirst(t);

        tgm.move(t);
        tgm.move(tgm.getBoard().get(1));

        assertTrue(tgm.isQueueEmpty());
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Test nextPhase draw to endTurn")
    void testShouldNextPhaseDrawToEnd() {
        tgm.initGame();
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        Tile t1 = new Tile(2, 1, new ArrayList<>(), List.of(eff), false, "giorgio");
        Tile t2 = new Tile(3, 1, new ArrayList<>(), new ArrayList<>(), false, "giorgio");
        tgm.getBoard().addFirst(t2);
        tgm.getBoard().addFirst(t1);

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.consumeAction();
        assertTrue(tgm.getToDoActions().isEmpty());

        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
    }

    @Test
    @DisplayName("Test nextPhase draw stays draw")
    void testShouldNextPhaseDrawToDraw() {
        tgm.initGame();
        tgm.addToDoAction(new Action(tgm.getPlayers().get(1), DrawCardEnum.DOWN_DRAW));

        tgm.move(tgm.getBoard().getFirst());
        tgm.nextPlayer();
        tgm.move(tgm.getBoard().get(1));
        tgm.nextPhase();

        GamePhaseState stateBefore = tgm.getCurrPhase();
        tgm.nextPhase();

        assertSame(stateBefore, tgm.getCurrPhase());
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Test nextPhase EndTurn to EndRound")
    void testShouldNextPhaseEndTurnToEndRound() {
        tgm.initGame();
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        Tile t1 = new Tile(3, 2, new ArrayList<>(), List.of(eff), false, "giorgio");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "giorgio");
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.clearToDoActions();
        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
    }

    @Test
    @DisplayName("Test nextPhase EndRound to SetUp")
    void testShouldNextPhaseEndRoundToSetUp() {
        tgm.initGame();
        Tile t1 = new Tile(1, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "giorgio");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "giorgio");
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));

        tgm.clearToDoActions();
        tgm.nextPhase();

        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Test nextPhase EndRound to PlayEvent at turn 10")
    void testShouldNextPhaseEndRoundToPlayEvent() {
        tgm.initGame();
        Tile t1 = new Tile(2, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "giorgio");
        Tile t2 = new Tile(3, 2, new ArrayList<>(), new ArrayList<>(), false, "giorgio");
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));

        tgm.clearToDoActions();
        tgm.setTurn(10);
        tgm.nextPhase();

        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
        assertEquals(GamePhaseEnum.END_GAME, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Test nextPhase PlayEvent to EndGame")
    void testShouldNextPhasePlayEventToEndGame() {
        tgm.initGame();
        Tile t1 = new Tile(2, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "giorgio");
        Tile t2 = new Tile(3, 2, new ArrayList<>(), new ArrayList<>(), false, "giorgio");
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));

        tgm.clearToDoActions();
        tgm.setTurn(10);
        tgm.nextPhase();

        assertEquals(GamePhaseEnum.END_GAME, tgm.getCurrPhase().getPhase());
    }

    // ── refillBoard ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Test refillBoard removes events and characters from lower")
    void testShouldRefillBoardRemovesInLower() {
        tgm.initGame();
        Builder b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Event e1 = new Feast(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 2, CardTypeEnum.FEAST);
        tgm.getLowerList().add(b1);
        tgm.getLowerList().add(e1);
        tgm.refillBoard();

        assertFalse(tgm.getLowerList().contains(b1));
        assertFalse(tgm.getLowerList().contains(e1));
    }

    @Test
    @DisplayName("Test refillBoard moves characters from upper to lower")
    void testShouldRefillBoardUpperToLower() {
        tgm.initGame();
        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);
        tgm.refillBoard();

        assertFalse(tgm.getUpperList().contains(b1));
        assertTrue(tgm.getLowerList().contains(b1));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("Test refillBoard triggers changeAge")
    void testShouldRefillBoardChangeAge(int value) {
        List<Player> players = new ArrayList<>();
        List<ModelObserver> listeners = new ArrayList<>();
        for (int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        tgm = new TestableGameManager(listeners, players, value);
        tgm.initGame();

        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getDeck().addFirst(b1);

        tgm.refillBoard();

        int numValid = tgm.getUpperList().stream()
                .filter(card -> card.getType() != CardTypeEnum.BUILDING)
                .toList().size();

        assertEquals(2, tgm.getCurrAge());
        assertTrue(tgm.getUpperList().contains(b1));
        assertFalse(tgm.getLowerList().contains(b1));
        assertEquals(value + 4, numValid);
    }

    // ── move ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Test move — tile occupata lancia eccezione")
    void testShouldNotMove() {
        tgm.initGame();
        Tile targetTile = tgm.getBoard().getFirst();
        Player testP = new Player("TestPlayer", ColorPawnEnum.WHITE);
        try {
            targetTile.setPlayer(testP);
        } catch (OccupiedTileException e) {
            fail("La tile doveva essere libera durante il setup del test");
        }
        assertThrows(OccupiedTileException.class, () -> tgm.move(targetTile));
        assertEquals(testP, targetTile.getPlayer());
    }

    @Test
    @DisplayName("Test move — tile libera")
    void testShouldMove() {
        tgm.initGame();
        Tile targetTile = tgm.getBoard().getFirst();
        Player currPlayer = tgm.getCurrentPlayer();

        tgm.move(targetTile);

        assertTrue(targetTile.isOccupied());
        assertEquals(currPlayer, targetTile.getPlayer());
    }

    // ── finalScoreCount ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Test finalScoreCount")
    void testShouldFinalScoreCount() {
        Player p1 = new Player("abc", ColorPawnEnum.BLUE);
        Player p2 = new Player("def", ColorPawnEnum.ORANGE);
        GameManager gm = new GameManager(new ArrayList<>(), List.of(p1, p2), 2, () -> {});

        CardEffectInstant e1 = new GainPP(CardTypeEnum.BUILDER, 3, GainPPEnum.PP_FOR_CAT);
        Building build1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.SETUP_PHASE, 2, 4, 2, List.of(e1), new ArrayList<>());
        Building build2 = new Building(CardTypeEnum.BUILDING, 12, null, 2, 5, 2, new ArrayList<>(), new ArrayList<>());
        Builder b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Builder b2 = new Builder(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Builder b3 = new Builder(5, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.BUILDER);
        Crafter c1 = new Crafter(123, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Crafter c2 = new Crafter(456, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BOWL, CardTypeEnum.CRAFTER);
        Crafter c3 = new Crafter(789, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Painter pa1 = new Painter(45, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter pa2 = new Painter(54, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter pa3 = new Painter(543, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter pa4 = new Painter(542, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);

        p1.addCard(b1); p1.addCard(b2); p2.addCard(b3);
        p1.addCard(c1); p1.addCard(c2); p1.addCard(c3); p2.addCard(c3);
        p1.addCard(pa1); p1.addCard(pa2); p2.addCard(pa3); p2.addCard(pa4);
        p1.addBuilding(build1); p2.addBuilding(build2);

        gm.finalScoreCount();

        assertEquals(26, p1.getPP());
        assertEquals(16, p2.getPP());
    }

    // ── drawCard ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Draw Card from lower — successo")
    void testShouldDrawCardLowerFine() {
        tgm.initGame();
        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        assertDoesNotThrow(() -> tgm.drawCard(b1));

        assertFalse(tgm.getLowerList().contains(b1));
        assertEquals(1, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("Draw Card from upper — successo")
    void testShouldDrawCardUpperFine() {
        tgm.initGame();
        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);

        assertDoesNotThrow(() -> tgm.drawCard(b1));

        assertFalse(tgm.getUpperList().contains(b1));
        assertEquals(1, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("Draw Card upper con azione down — errore")
    void testShouldDrawCardUpperNotFine() {
        tgm.initGame();
        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        InvalidDrawException ex = assertThrows(InvalidDrawException.class, () -> tgm.drawCard(b1));
        assertEquals("Fila non valida", ex.getMessage());
        assertTrue(tgm.getLowerList().contains(b1));
        assertEquals(0, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("Draw Card lower con azione up — errore")
    void testShouldDrawCardLowerNotFine() {
        tgm.initGame();
        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);

        InvalidDrawException ex = assertThrows(InvalidDrawException.class, () -> tgm.drawCard(b1));
        assertEquals("Fila non valida", ex.getMessage());
        assertTrue(tgm.getUpperList().contains(b1));
        assertEquals(0, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    // ── initFood ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("initFood — giocatore extra non riceve cibo")
    void testShouldInitFood() {
        int value = 6;
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < value; i++)
            players.add(new Player("Player" + i, i < 5 ? ColorPawnEnum.values()[i] : ColorPawnEnum.BLUE));
        GameManager gm = new GameManager(new ArrayList<>(), players, value, () -> {});
        gm.initGame();
        assertEquals(0, players.get(value - 1).getNFood());
    }

    // ── getToDoActions ────────────────────────────────────────────────────────

    @Test
    void testShouldGetToDoActions() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        GameManager gm = new GameManager(new ArrayList<>(), players, 5, () -> {});
        assertEquals(0, gm.getToDoActions().size());
    }

    @Test
    @DisplayName("checkEffects con lista non vuota")
    void checkEffectsNotEmpty() {
        tgm.initGame();
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        Building build1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.SETUP_PHASE, 2, 4, 2, new ArrayList<>(), List.of(eff));
        tgm.getCurrentPlayer().addBuilding(build1);
        tgm.checkEffects();

        assertEquals(1, tgm.getToDoActions().size());
        assertSame(DrawCardEnum.UP_DRAW, tgm.getToDoActions().getFirst().getType());
    }

    // ── addListener ───────────────────────────────────────────────────────────

    @Test
    void testShouldAddListener() {
        ModelObserver l = mock(ModelObserver.class);
        assertEquals(0, tgm.getListeners().size());
        tgm.getListeners().add(l);
        assertEquals(1, tgm.getListeners().size());
    }

    // ── placeholder ───────────────────────────────────────────────────────────

    @Test void testShouldPlayEvent() {}
    @Test void testShouldCheckBoardTileEffects() {}
    @Test void testShouldCheckQueueTileEffects() {}
}