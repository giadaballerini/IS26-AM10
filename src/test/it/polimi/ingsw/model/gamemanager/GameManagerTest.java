package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.ProtectPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Hunter;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameManagerTest {


    private static class TestableGameManager extends GameManager {

        public TestableGameManager(List<ModelObserver> l, List<Player> p, int n) {
            super(l, p, n, () -> {});
            setCurrPhaseState(new SetupPhaseState());
        }


        public GamePhaseEnum getCurrPhaseEnum() {
            return super.getCurrPhase();
        }


        public List<Tile>   getBoard()         { return stateField("board"); }
        public List<Tile>   getQueue()         { return stateField("queue"); }
        public int          getCurrAge()       { return stateField("currAge"); }
        public List<Player> getPlayers()       { return stateField("players"); }
        public List<Card>   getUpperList()     { return stateField("upperList"); }
        public List<Card>   getLowerList()     { return stateField("lowerList"); }
        public List<Card>   getDeck()          { return stateField("deck"); }
        public Player       getCurrentPlayer() { return stateField("currPlayer"); }

        public void addToDoAction(Action a)    { this.<List<Action>>stateField("toDoActions").add(a); }
        public void clearToDoActions()         { this.<List<Action>>stateField("toDoActions").clear(); }
        public void consumeAction()            { List<Action> l = stateField("toDoActions"); if (!l.isEmpty()) l.removeFirst(); }

        public void setTurn(int i) {
            setStateField("currTurn", i);
        }


        public void addListener(ModelObserver obs) {
            this.<List<ModelObserver>>notifierField("listeners").add(obs);
        }
        public List<ModelObserver> getListeners() {
            return notifierField("listeners");
        }
        public boolean getSkippableDraw(){return stateField("skippableDraw");}


        @SuppressWarnings("unchecked")
        private <T> T stateField(String name) {
            try {
                var stateF = GameManager.class.getDeclaredField("state");
                stateF.setAccessible(true);
                Object state = stateF.get(this);
                var f = state.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return (T) f.get(state);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        private void setStateField(String name, Object value) {
            try {
                var stateF = GameManager.class.getDeclaredField("state");
                stateF.setAccessible(true);
                Object state = stateF.get(this);
                var f = state.getClass().getDeclaredField(name);
                f.setAccessible(true);
                f.set(state, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T notifierField(String name) {
            try {
                var notifierF = GameManager.class.getDeclaredField("notifier");
                notifierF.setAccessible(true);
                Object notifier = notifierF.get(this);
                var f = notifier.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return (T) f.get(notifier);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private TestableGameManager tgm;
    private CardEffectInteractive effect = mock(DrawCard.class);

    @Mock private ModelObserver obsP1 = mock(ModelObserver.class);
    @Mock private ModelObserver obsP2 = mock(ModelObserver.class);
    @Mock private ModelObserver obsP3 = mock(ModelObserver.class);

    @BeforeEach
    void setUp() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Player1", ColorPawnEnum.BLUE));
        players.add(new Player("Player2", ColorPawnEnum.ORANGE));
        players.add(new Player("Player3", ColorPawnEnum.PURPLE));
        when(obsP1.getNickname()).thenReturn("Player1");
        when(obsP2.getNickname()).thenReturn("Player2");
        when(obsP3.getNickname()).thenReturn("Player3");
        tgm = new TestableGameManager(new ArrayList<>(), players, 3);
    }

    @Test
    @DisplayName("nextPhase — setup rimane setup finché la queue non è vuota")
    void testShouldNextPhaseSetup() {
        tgm.initGame();
        tgm.nextPhase();
        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhaseEnum());
    }

    @Test
    @DisplayName("nextPhase — setup → draw quando la queue è vuota")
    void testShouldNextPhaseSetupToDraw() {
        tgm.initGame();

        Tile t = new Tile(2, 1, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");

        tgm.getBoard().addFirst(t);

        tgm.move(t);
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));

        assertTrue(tgm.isQueueEmpty());
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());
    }

    @Test
    @DisplayName("nextPhase — draw → endTurn quando toDoActions è vuota")
    void testShouldNextPhaseDrawToEndTurn() {
        tgm.initGame();
        Tile t1 = new Tile(2, 1, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(3, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t2);
        tgm.getBoard().addFirst(t1);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());
        tgm.consumeAction();

        assertTrue(tgm.getToDoActions().isEmpty());
        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
    }

    @Test
    @DisplayName("nextPhase — draw rimane draw se ci sono ancora azioni pendenti")
    void testShouldNextPhaseDrawStaysDraw() {
        tgm.initGame();
        tgm.addToDoAction(new Action(tgm.getPlayers().get(1), DrawCardEnum.DOWN_DRAW));
        tgm.move(tgm.getBoard().getFirst());
        tgm.nextPlayer();
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());
        assertFalse(tgm.getToDoActions().isEmpty());
        GamePhaseEnum phaseBefore = tgm.getCurrPhaseEnum();
        tgm.nextPhase();
        assertEquals(phaseBefore, tgm.getCurrPhaseEnum());
    }

    @Test
    @DisplayName("nextPhase — endTurn → endRound quando tutti i giocatori sono tornati")
    void testShouldNextPhaseEndTurnToEndRound() {
        tgm.initGame();
        Tile t1 = new Tile(3, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());
        tgm.clearToDoActions();
        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
    }

    @Test
    @DisplayName("nextPhase — endRound → setup al turno normale")
    void testShouldNextPhaseEndRoundToSetup() {
        tgm.initGame();
        Tile t1 = new Tile(1, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        tgm.clearToDoActions();
        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhaseEnum());
    }

    @Test
    @DisplayName("nextPhase — endRound → endGame al turno 10")
    void testShouldNextPhaseEndRoundToEndGameAtTurn10() {
        tgm.initGame();
        Tile t1 = new Tile(2, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(3, 2, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        tgm.clearToDoActions();
        tgm.setTurn(10);
        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
        assertEquals(GamePhaseEnum.END_GAME, tgm.getCurrPhaseEnum());
    }

    @Test
    @DisplayName("onMoveRequested — tile occupata lancia OccupiedTileException")
    void testShouldThrowOnOccupiedTile() {
        tgm.initGame();
        Tile targetTile = tgm.getBoard().getFirst();
        Player occupier = new Player("Occupier", ColorPawnEnum.WHITE);
        targetTile.setPlayer(occupier);
        String currNick = tgm.getCurrentPlayer().getNickname();
        assertThrows(OccupiedTileException.class, () -> tgm.onMoveRequested(currNick, 0));
        assertEquals(occupier, targetTile.getPlayer());
    }

    @Test
    @DisplayName("move — tile libera: il giocatore corrente occupa la tile")
    void testShouldMoveToFreeTile() {
        tgm.initGame();
        Tile targetTile = tgm.getBoard().getFirst();
        Player currPlayer = tgm.getCurrentPlayer();
        tgm.move(targetTile);
        assertTrue(targetTile.isOccupied());
        assertEquals(currPlayer, targetTile.getPlayer());
    }

    @Test
    @DisplayName("drawCard — pesca da lower con azione DOWN: successo")
    void testShouldDrawCardFromLower() {
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        assertDoesNotThrow(() -> tgm.onDrawCardRequested(tgm.getCurrentPlayer().getNickname(), b1.getId()));
        assertFalse(tgm.getLowerList().contains(b1));
        assertEquals(1, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("drawCard — pesca da upper con azione UP: successo")
    void testShouldDrawCardFromUpper() {
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);

        assertDoesNotThrow(() -> tgm.onDrawCardRequested(tgm.getCurrentPlayer().getNickname(), b1.getId()));
        assertFalse(tgm.getUpperList().contains(b1));
        assertEquals(1, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("onDrawCardRequested — carta in lower con azione UP: lancia InvalidDrawException")
    void testShouldFailDrawFromLowerWithUpAction() {
        tgm.initGame();

        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        tgm.move(tgm.getBoard().get(3));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        InvalidDrawException ex = assertThrows(InvalidDrawException.class,
                () -> tgm.onDrawCardRequested(tgm.getCurrentPlayer().getNickname(), b1.getId()));
        assertEquals("Non hai pescate disponibili dalla fila inferiore", ex.getMessage());
        assertTrue(tgm.getLowerList().contains(b1));
        assertEquals(0, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("drawCard — carta in upper con azione DOWN: lancia InvalidDrawException")
    void testShouldFailDrawFromUpperWithDownAction() {
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);

        InvalidDrawException ex = assertThrows(InvalidDrawException.class,
                () -> tgm.onDrawCardRequested(tgm.getCurrentPlayer().getNickname(), b1.getId()));
        assertEquals("Non hai pescate disponibili dalla fila superiore", ex.getMessage());
        assertTrue(tgm.getUpperList().contains(b1));
        assertEquals(0, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }
    @Test
    @DisplayName("getToDoActions — lista vuota all'avvio")
    void testToDoActionsEmptyOnStart() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        GameManager gm = new GameManager(new ArrayList<>(), players, 5, () -> {});
        assertEquals(0, gm.getToDoActions().size());
    }

    @Test
    @DisplayName("addListener — il listener viene registrato nel notifier")
    void testShouldAddListener() {
        ModelObserver listener = mock(ModelObserver.class);
        tgm.addListener(listener);
        assertEquals(1, tgm.getListeners().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("initGame — configurazione valida inizializza board e liste")
    void testShouldInitGameForValidValues(int value) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        tgm = new TestableGameManager(new ArrayList<>(), players, value);
        tgm.initGame();

        assertNotNull(tgm.getBoard());
        assertFalse(tgm.getBoard().isEmpty());

        int sub = switch (value) {
            case 2       -> 1;
            case 3, 4, 5 -> 2;
            default      -> 0;
        };
        assertEquals(value + 4, tgm.getUpperList().size() - sub);
        assertFalse(tgm.getLowerList().isEmpty());
        assertEquals(value + 1, tgm.getLowerList().size());
        assertEquals(1, tgm.getCurrAge());
        assertNotNull(tgm.getCurrentPlayer());
    }

    @Test
    @DisplayName("checkEffects — aggiunge azione se building ha effetto interattivo attivo")
    void testCheckEffectsAddsActionWhenBuildingHasEffect() {
        tgm.initGame();
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        Building build1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.SETUP_PHASE, 2, 4, 2, new ArrayList<>(), List.of(eff));
        tgm.getCurrentPlayer().addBuilding(build1);
        tgm.checkEffects();
        assertTrue(tgm.getCurrentPlayer().hasSkippableDraws());
        assertSame(DrawCardEnum.UP_DRAW,
                tgm.getCurrentPlayer().resolveSkippableDraws().getFirst().getType());
    }

    @Test
    @DisplayName("notifyGameEnding — ranking distinto: ogni observer riceve la propria posizione")
    void notifyGameEnding_distinctRanks_eachObserverReceivesCorrectRankingPos() {
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);
        Player p3 = tgm.getPlayers().get(2);

        p1.addPP(30);
        p2.addPP(20);
        p3.addPP(10);

        Map<String, Integer> globalPositions = Map.of("Player1", 1, "Player2", 2, "Player3", 3);

        TestableGameManager gm = new TestableGameManager(List.of(obsP1, obsP2, obsP3), List.of(p1, p2, p3), 3);
        gm.notifyGameEnding(globalPositions);

        verify(obsP1).onGameEnding(any(), eq(1), eq(1));
        verify(obsP2).onGameEnding(any(), eq(2), eq(2));
        verify(obsP3).onGameEnding(any(), eq(3), eq(3));
    }

    @Test
    @DisplayName("notifyGameEnding — pareggio: rank condiviso e ultimo riceve rank 3 (non 2)")
    void notifyGameEnding_tiedPlayers_shareRankAndLastPlayerGetsSkippedRank() {
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);
        Player p3 = tgm.getPlayers().get(2);

        p1.addPP(20);
        p2.addPP(20);
        p3.addPP(10);

        Map<String, Integer> globalPositions = Map.of("Player1", 1, "Player2", 1, "Player3", 2);

        TestableGameManager gm = new TestableGameManager(List.of(obsP1, obsP2, obsP3), List.of(p1, p2, p3), 3);
        gm.notifyGameEnding(globalPositions);

        verify(obsP1).onGameEnding(any(), eq(1), anyInt());
        verify(obsP2).onGameEnding(any(), eq(1), anyInt());
        verify(obsP3).onGameEnding(any(), eq(3), anyInt());
    }

    @Test
    @DisplayName("notifyGameEnding — statsList contiene tutti i giocatori con PP e food corretti")
    void notifyGameEnding_statsListReflectsCurrentModelState() {
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);

        p1.addPP(15);
        p1.addFood(3);
        p2.addPP(10);

        Map<String, Integer> globalPositions = Map.of("Alice", 1, "Bob", 2);

        GameManager gm = new TestableGameManager(List.of(obsP1, obsP2), List.of(p1, p2), 2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlayerStatsDTO>> captor = ArgumentCaptor.forClass(List.class);

        gm.notifyGameEnding(globalPositions);

        verify(obsP1).onGameEnding(captor.capture(), anyInt(), anyInt());

        List<PlayerStatsDTO> stats = captor.getValue();
        assertEquals(2, stats.size());

        PlayerStatsDTO P1Stats = stats.stream()
                .filter(s -> "Player1".equals(s.getNickname()))
                .findFirst()
                .orElseThrow();

        assertEquals(15, P1Stats.getPPs());
        assertEquals(3,  P1Stats.getnFood());
    }

    @Test
    @DisplayName("notifyGameEnding — nickname assente da globalPositions → globalRankingPos = -1")
    void notifyGameEnding_missingGlobalPosition_passesMinus1() {
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);
        p1.addPP(10);
        p2.addPP(5);

        Map<String, Integer> globalPositions = Map.of("Alice", 1);

        GameManager gm = new TestableGameManager(List.of(obsP1, obsP2), List.of(p1, p2), 2);
        gm.notifyGameEnding(globalPositions);

        verify(obsP2).onGameEnding(any(), anyInt(), eq(-1));
    }

    @Test
    @DisplayName("notifyGameEnding — ogni observer viene notificato esattamente una volta")
    void notifyGameEnding_allObserversNotifiedExactlyOnce() {
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);
        Player p3 = tgm.getPlayers().get(2);
        p1.addPP(10);
        p2.addPP(5);
        p3.addPP(1);

        Map<String, Integer> globalPositions = Map.of("Alice", 1, "Bob", 2, "Carl", 3);

        GameManager gm = new TestableGameManager(List.of(obsP1, obsP2, obsP3), List.of(p1, p2, p3), 3);
        gm.notifyGameEnding(globalPositions);

        verify(obsP1, times(1)).onGameEnding(any(), anyInt(), anyInt());
        verify(obsP2, times(1)).onGameEnding(any(), anyInt(), anyInt());
        verify(obsP3, times(1)).onGameEnding(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("onMoveRequested — fase diversa da SETUP lancia InvalidPhaseException")
    void testShouldThrowInvalidPhaseExceptionOnMoveRequested() {
        tgm.initGame();
        Tile t1 = new Tile(1, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        String currNick = tgm.getCurrentPlayer().getNickname();
        assertThrows(InvalidPhaseException.class, () -> tgm.onMoveRequested(currNick, 0));
    }

    @Test
    @DisplayName("onMoveRequested — nickname diverso dal giocatore corrente lancia InvalidPlayerException")
    void testShouldThrowInvalidPlayerExceptionOnMoveRequested() {
        tgm.initGame();
        String wrongNick = tgm.getPlayers().stream()
                .map(Player::getNickname)
                .filter(n -> !n.equals(tgm.getCurrentPlayer().getNickname()))
                .findFirst()
                .orElseThrow();

        assertThrows(InvalidPlayerException.class, () -> tgm.onMoveRequested(wrongNick, 0));
    }

    @Test
    @DisplayName("onMoveRequested — tilePos inesistente lancia InvalidMoveException")
    void testShouldThrowInvalidMoveExceptionOnMoveRequested() {
        tgm.initGame();
        String currNick = tgm.getCurrentPlayer().getNickname();
        int invalidPos = tgm.getBoard().size() + 5;

        assertThrows(InvalidMoveException.class, () -> tgm.onMoveRequested(currNick, invalidPos));
    }

    @Test
    @DisplayName("onMoveRequested — richiesta valida applica il movimento senza eccezioni")
    void testShouldMoveSuccessfullyViaOnMoveRequested() {
        tgm.initGame();
        Tile targetTile = tgm.getBoard().getFirst();
        Player currPlayer = tgm.getCurrentPlayer();
        String currNick = currPlayer.getNickname();

        assertDoesNotThrow(() -> tgm.onMoveRequested(currNick, 0));

        assertTrue(targetTile.isOccupied());
        assertEquals(currPlayer, targetTile.getPlayer());
    }

    @Test
    @DisplayName("onDrawCardRequested — nickname diverso dal giocatore corrente lancia InvalidPlayerException")
    void testShouldThrowInvalidPlayerExceptionOnDrawCardRequested() {
        tgm.initGame();
        String wrongNick = tgm.getPlayers().stream()
                .map(Player::getNickname)
                .filter(n -> !n.equals(tgm.getCurrentPlayer().getNickname()))
                .findFirst()
                .orElseThrow();

        assertThrows(InvalidPlayerException.class, () -> tgm.onDrawCardRequested(wrongNick, 1));
    }

    @Test
    @DisplayName("onDrawCardRequested — fase diversa da DRAW/OPTIONAL_DRAW lancia InvalidPhaseException")
    void testShouldThrowInvalidPhaseExceptionOnDrawCardRequested() {
        tgm.initGame();
        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhaseEnum());
        String currNick = tgm.getCurrentPlayer().getNickname();

        assertThrows(InvalidPhaseException.class, () -> tgm.onDrawCardRequested(currNick, 1));
    }

    @Test
    @DisplayName("onDrawCardRequested — cardID inesistente lancia InvalidDrawException")
    void testShouldThrowInvalidDrawExceptionOnDrawCardRequestedCardNotFound() {
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        String currNick = tgm.getCurrentPlayer().getNickname();
        int nonExistentCardId = -999;

        assertThrows(InvalidDrawException.class, () -> tgm.onDrawCardRequested(currNick, nonExistentCardId));
    }

    @Test
    @DisplayName("onSkipRequested — richiesta valida esegue lo skip e avanza fase")
    void testShouldSkipSuccessfully() {
        tgm.initGame();
        Tile t1 = new Tile(1, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.setSkippableDraw(true);
        String currNick = tgm.getCurrentPlayer().getNickname();

        assertDoesNotThrow(() -> tgm.onSkipRequested(currNick));
    }

    @Test
    @DisplayName("onSkipRequested — nickname diverso dal giocatore corrente lancia InvalidPlayerException")
    void testShouldThrowInvalidPlayerExceptionOnSkipRequested() {
        tgm.initGame();
        String wrongNick = tgm.getPlayers().stream()
                .map(Player::getNickname)
                .filter(n -> !n.equals(tgm.getCurrentPlayer().getNickname()))
                .findFirst()
                .orElseThrow();

        assertThrows(InvalidPlayerException.class, () -> tgm.onSkipRequested(wrongNick));
    }

    @Test
    @DisplayName("onSkipRequested — fase diversa da DRAW/OPTIONAL_DRAW lancia InvalidPhaseException")
    void testShouldThrowInvalidPhaseExceptionOnSkipRequested() {
        tgm.initGame();
        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhaseEnum());
        String currNick = tgm.getCurrentPlayer().getNickname();

        assertThrows(InvalidPhaseException.class, () -> tgm.onSkipRequested(currNick));
    }

    @Test
    @DisplayName("onSkipRequested — skippableDraw false lancia InvalidSkipException")
    void testShouldThrowInvalidSkipExceptionOnSkipRequested() {
        tgm.initGame();
        Tile t1 = new Tile(1, 2, new ArrayList<>(), List.of(new DrawCard(DrawCardEnum.UP_DRAW)), false, "");
        Tile t2 = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "");
        Tile t3 = new Tile(4, 1, new ArrayList<>(), new ArrayList<>(), false, "");
        tgm.getBoard().addFirst(t3);
        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);
        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.setSkippableDraw(false);
        String currNick = tgm.getCurrentPlayer().getNickname();

        assertThrows(InvalidSkipException.class, () -> tgm.onSkipRequested(currNick));
    }

    @Test
    @DisplayName("getQueueSize/getNumPlayers/getCurrTurn/incrementTurn — wiring verso GameState")
    void testShouldExposeStateCountersCorrectly() {
        tgm.initGame();
        assertEquals(3, tgm.getNumPlayers());
        assertEquals(1, tgm.getCurrTurn());
        tgm.incrementTurn();
        assertEquals(2, tgm.getCurrTurn());
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());
    }

    @Test
    @DisplayName("hasAnySkippableDraws — false quando nessun giocatore ha pesche saltabili")
    void testShouldReturnFalseWhenNoSkippableDraws() {
        tgm.initGame();
        assertFalse(tgm.hasAnySkippableDraws());
    }

    @Test
    @DisplayName("hasAnySkippableDraws — true quando un giocatore ha pesche saltabili in coda")
    void testShouldReturnTrueWhenSomeoneHasSkippableDraws() {
        tgm.initGame();
        tgm.getCurrentPlayer().addSkippableDraws(
                List.of(new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW)));
        assertTrue(tgm.hasAnySkippableDraws());
    }

    @Test
    @DisplayName("checkBoardTileEffects — non lancia eccezioni con player su tile senza effetti")
    void testShouldNotThrowOnCheckBoardTileEffects() {
        tgm.initGame();
        Tile t = tgm.getBoard().getFirst();
        tgm.move(t);
        assertDoesNotThrow(() -> tgm.checkBoardTileEffects());
    }

    @Test
    @DisplayName("showBoard — notifica tutti gli observer registrati con lo stato corrente")
    void testShouldNotifyShowBoardToAllListeners() {
        ModelObserver listener = mock(ModelObserver.class);
        tgm.addListener(listener);
        tgm.initGame();
        tgm.showBoard();
        verify(listener, atLeast(2)).showBoard(any());
    }

    @Test
    @DisplayName("getOnGameEndedCallback — restituisce il callback passato al costruttore")
    void testShouldReturnOnGameEndedCallback() {
        Runnable callback = () -> {};
        GameManager gm = new GameManager(new ArrayList<>(), tgm.getPlayers(), 2, callback);
        assertSame(callback, gm.getOnGameEndedCallback());
    }

    @Test
    @DisplayName("setOnGameStartedCallback — il callback viene invocato da initGame")
    void testShouldInvokeOnGameStartedCallback() {
        boolean[] called = {false};
        tgm.setOnGameStartedCallback(() -> called[0] = true);
        tgm.initGame();
        assertTrue(called[0]);
    }

    @Test
    @DisplayName("toSnapshot — produce uno snapshot non nullo coerente con lo stato corrente")
    void testShouldProduceConsistentSnapshot() {
        tgm.initGame();
        GameSnapshot snap = tgm.toSnapshot(42);

        assertNotNull(snap);
        assertEquals(tgm.getNumPlayers(), snap.getNumPlayers());
        assertEquals(tgm.getCurrAge(), snap.getCurrAge());
        assertEquals(tgm.getCurrTurn(), snap.getCurrTurn());
    }

    @Test
    @DisplayName("checkCanDraw — mustDraw true: notifica le carte disponibili (character in upper)")
    void testCheckCanDrawWhenMustDraw() {

        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW));
        Builder b1 = new Builder(555001, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);

        tgm.checkCanDraw();

        verify(listener).notifyDrawable(any());
    }

    @Test
    @DisplayName("checkCanDraw — mayDraw true: setta skippableDraw e notifica le carte disponibili")
    void testCheckCanDrawWhenMayDraw() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();


        tgm.getLowerList().clear();

        Building build = new Building(
                CardTypeEnum.BUILDING, 555002,
                GamePhaseEnum.SETUP_PHASE, 1, 2, 0,   // <-- foodCost = 0
                new ArrayList<>(), new ArrayList<>());
        tgm.getLowerList().add(build);

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));

        tgm.checkCanDraw();

        assertTrue(tgm.getSkippableDraw());
        verify(listener).notifyDrawable(any());
    }
    @Test
    @DisplayName("checkCanDraw — né mustDraw né mayDraw: esegue skip automatico")
    void testCheckCanDrawWhenCannotDrawSkipsAutomatically() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();
        tgm.clearToDoActions();

        tgm.checkCanDraw();

        verify(listener).notifySkip(anyString());
    }

    @Test
    @DisplayName("changeAge — incrementa l'età e notifica gli observer")
    void testShouldChangeAgeAndNotify() {
        ModelObserver listener = mock(ModelObserver.class);
        tgm.addListener(listener);
        tgm.initGame();
        int ageBefore = tgm.getCurrAge();

        tgm.changeAge();

        assertTrue(tgm.getCurrAge() >= ageBefore);
        verify(listener).onChangeAge(any());
    }

    @Test
    @DisplayName("nextPlayer — avanza il giocatore corrente e notifica l'observer")
    void testShouldAdvanceToNextPlayerAndNotify() {
        ModelObserver listener = mock(ModelObserver.class);
        tgm.addListener(listener);
        tgm.initGame();
        Player before = tgm.getCurrentPlayer();

        tgm.nextPlayer();

        verify(listener, atLeastOnce()).onCurrPlayerUpdate(anyString());
        assertNotNull(tgm.getCurrentPlayer());
    }

    @Test
    @DisplayName("refillBoard — rifornisce upperList dal deck senza eccezioni")
    void testShouldRefillBoardWithoutErrors() {
        tgm.initGame();
        int upperSizeBefore = tgm.getUpperList().size();
        int deckSizeBefore = tgm.getDeck().size();

        assertDoesNotThrow(() -> tgm.refillBoard());

        assertTrue(tgm.getUpperList().size() >= upperSizeBefore);
        assertTrue(tgm.getDeck().size() <= deckSizeBefore);
    }

    @Test
    @DisplayName("loadSkippableDraws — nessun giocatore con pesche saltabili: non notifica nulla")
    void testLoadSkippableDrawsNoPlayerHasAny() {
        ModelObserver listener = mock(ModelObserver.class);
        tgm.addListener(listener);
        tgm.initGame();
        reset(listener);

        tgm.loadSkippableDraws();

        verify(listener, never()).notifyDrawable(any());
    }

    @Test
    @DisplayName("loadSkippableDraws — carica le pesche saltabili del primo giocatore idoneo e notifica")
    void testLoadSkippableDrawsFindsEligiblePlayer() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();

        Player target = tgm.getPlayers().get(1);
        target.addSkippableDraws(List.of(new Action(target, DrawCardEnum.UP_DRAW)));

        tgm.loadSkippableDraws();

        assertEquals(target, tgm.getCurrentPlayer());
        verify(listener).notifyDrawable(any());
    }

    @Test
    @DisplayName("playEvent — risolve gli eventi, notifica e avanza fase")
    void testShouldPlayEventNotifyAndAdvancePhase() {
        ModelObserver listener = mock(ModelObserver.class);
        tgm.addListener(listener);
        tgm.initGame();
        GamePhaseEnum phaseBefore = tgm.getCurrPhaseEnum();

        assertDoesNotThrow(() -> tgm.playEvent());

        verify(listener).onEvent(any());
    }

    @Test
    @DisplayName("finalScoreCount — applica i punteggi finali senza eccezioni")
    void testShouldApplyFinalScoresWithoutErrors() {
        tgm.initGame();
        int ppBefore = tgm.getCurrentPlayer().getPP();

        assertDoesNotThrow(() -> tgm.finalScoreCount());

        assertTrue(tgm.getCurrentPlayer().getPP() >= ppBefore);
    }

    @Test
    @DisplayName("drawCard — con azioni ancora pendenti chiama checkCanDraw invece di avanzare fase")
    void testDrawCardWithRemainingActionsTriggersCheckCanDraw() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        tgm.getUpperList().clear();
        tgm.getLowerList().clear();

        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));
        tgm.addToDoAction(new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW));

        Builder b1 = new Builder(555010, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        assertDoesNotThrow(() -> tgm.onDrawCardRequested(tgm.getCurrentPlayer().getNickname(), b1.getId()));

        verify(listener, atLeastOnce()).notifySkip(anyString());
    }

    @Test
    @DisplayName("execEndTurn — sposta il giocatore in coda, notifica e avanza fase")
    void testShouldExecEndTurnMovePlayerToQueueAndNotify() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();

        Player currPlayer = tgm.getCurrentPlayer();
        Tile currPlayerTile = tgm.getBoard().getFirst();
        tgm.move(currPlayerTile);

        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));

        assertTrue(currPlayerTile.isOccupied());
        assertEquals(currPlayer, currPlayerTile.getPlayer());

        assertDoesNotThrow(() -> tgm.execEndTurn());

        assertFalse(currPlayerTile.isOccupied());
        verify(listener).onReturnToQueue(any(), any());
    }
    @Test
    @DisplayName("refillBoard — rimuove eventi e character dalla lowerList prima del refill")
    void testRefillBoardRemovesEventsAndCharactersFromLower() {
        tgm.initGame();
        tgm.getDeck().clear();

        Builder charCard = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Building buildCard = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.SETUP_PHASE, 1, 2, 0, new ArrayList<>(), new ArrayList<>());
        tgm.getLowerList().add(charCard);
        tgm.getLowerList().add(buildCard);

        tgm.refillBoard();

        assertFalse(tgm.getLowerList().contains(charCard),
                "I character card devono essere rimossi dalla lowerList");
        assertTrue(tgm.getLowerList().contains(buildCard),
                "I building card devono rimanere nella lowerList");
    }

    @Test
    @DisplayName("refillBoard — sposta character e event da upperList a lowerList")
    void testRefillBoardMovesCharactersFromUpperToLower() {
        tgm.initGame();
        tgm.getDeck().clear();
        tgm.getUpperList().clear();
        tgm.getLowerList().clear();

        Builder charCard = new Builder(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Building buildCard = new Building(CardTypeEnum.BUILDING, 4, GamePhaseEnum.SETUP_PHASE, 1, 2, 0, new ArrayList<>(), new ArrayList<>());
        tgm.getUpperList().add(charCard);
        tgm.getUpperList().add(buildCard);

        tgm.refillBoard();

        assertFalse(tgm.getUpperList().contains(charCard),
                "I character card devono essere rimossi dalla upperList");
        assertTrue(tgm.getLowerList().contains(charCard),
                "I character card devono essere spostati nella lowerList");
        assertTrue(tgm.getUpperList().contains(buildCard),
                "I building card devono rimanere nella upperList");
    }

    @Test
    @DisplayName("refillBoard — pesca esattamente numPlayers+4 carte dal deck")
    void testRefillBoardDrawsCorrectNumberOfCards() {
        tgm.initGame();
        tgm.getUpperList().clear();
        int deckSizeBefore = tgm.getDeck().size();
        int expected = Math.min(tgm.getNumPlayers() + 4, deckSizeBefore);

        tgm.refillBoard();

        assertEquals(expected, tgm.getUpperList().size(),
                "upperList deve contenere esattamente numPlayers+4 carte (o meno se il deck è esaurito)");
        assertEquals(deckSizeBefore - expected, tgm.getDeck().size());
    }

    @Test
    @DisplayName("refillBoard — restituisce true e chiama changeAge se una carta appartiene all'età successiva")
    void testRefillBoardTriggersAgeChangeWhenNewerAgeCardDrawn() {
        tgm.initGame();
        tgm.getUpperList().clear();
        tgm.getDeck().clear();

        Builder nextAgeCard = new Builder(5, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);
        tgm.getDeck().add(nextAgeCard);

        int ageBefore = tgm.getCurrAge();
        tgm.refillBoard();

        assertTrue(tgm.getCurrAge() > ageBefore,
                "L'età deve essere incrementata quando una carta di età superiore viene pescata");
    }

    @Test
    @DisplayName("refillBoard — deck vuoto non lancia eccezioni e non modifica upperList")
    void testRefillBoardWithEmptyDeckDoesNotThrow() {
        tgm.initGame();
        tgm.getDeck().clear();
        tgm.getUpperList().clear();

        assertDoesNotThrow(() -> tgm.refillBoard());
        assertTrue(tgm.getUpperList().isEmpty());
    }

    @Test
    @DisplayName("drawCard — senza azioni pendenti chiama nextPhase")
    void testDrawCardWithNoRemainingActionsCallsNextPhase() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());

        Builder b1 = new Builder(555011, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        assertDoesNotThrow(() -> tgm.onDrawCardRequested(tgm.getCurrentPlayer().getNickname(), b1.getId()));

        verify(listener, atLeastOnce()).onReturnToQueue(any(), any());
    }

    @Test
    @DisplayName("calculateRankingPoints — 3 giocatori, ranking distinto: punti corretti per ogni posizione")
    void testRankingPoints_3Players_distinctRanks() {
        Player p1 = new Player("P1", ColorPawnEnum.BLUE);
        Player p2 = new Player("P2", ColorPawnEnum.ORANGE);
        Player p3 = new Player("P3", ColorPawnEnum.PURPLE);
        p1.addPP(30); p2.addPP(20); p3.addPP(10);

        RankingCalculator rc = new RankingCalculator();
        Map<Player, Integer> pts = rc.calculateRankingPoints(List.of(p1, p2, p3), 3);

        assertEquals( 1, pts.get(p1));
        assertEquals( 0, pts.get(p2));
        assertEquals(-1, pts.get(p3));
    }

    @Test
    @DisplayName("calculateRankingPoints — pareggio PP e food: i pari condividono lo stesso punteggio")
    void testRankingPoints_tied_sharePoints() {
        Player p1 = new Player("P1", ColorPawnEnum.BLUE);
        Player p2 = new Player("P2", ColorPawnEnum.ORANGE);
        Player p3 = new Player("P3", ColorPawnEnum.PURPLE);
        p1.addPP(20); p2.addPP(20); p3.addPP(10);

        RankingCalculator rc = new RankingCalculator();
        Map<Player, Integer> pts = rc.calculateRankingPoints(List.of(p1, p2, p3), 3);

        assertEquals(pts.get(p1), pts.get(p2));
        assertEquals(1, pts.get(p1));
        assertEquals(-1, pts.get(p3));
    }

    @Test
    @DisplayName("calculateRankingPoints — tiebreak food: player con più food vince a parità di PP")
    void testRankingPoints_foodTiebreak() {
        Player p1 = new Player("P1", ColorPawnEnum.BLUE);
        Player p2 = new Player("P2", ColorPawnEnum.ORANGE);
        p1.addPP(20); p1.addFood(5);
        p2.addPP(20); // 0 food

        RankingCalculator rc = new RankingCalculator();
        Map<Player, Integer> pts = rc.calculateRankingPoints(List.of(p1, p2), 2);

        assertEquals(1, pts.get(p1));
        assertEquals(0, pts.get(p2));
    }


    @Test
    @DisplayName("calculateRankingPoints — wiring: restituisce mappa con un entry per ogni giocatore")
    void testCalculateRankingPointsReturnsEntryForEachPlayer() {
        tgm.initGame();
        tgm.getPlayers().get(0).addPP(30);
        tgm.getPlayers().get(1).addPP(20);
        tgm.getPlayers().get(2).addPP(10);

        Map<Player, Integer> pts = tgm.calculateRankingPoints();

        assertEquals(tgm.getNumPlayers(), pts.size());
        tgm.getPlayers().forEach(p -> assertTrue(pts.containsKey(p)));
    }

    @Test
    @DisplayName("calculateRankingPoints — wiring: i punti riflettono il ranking reale dei giocatori in state")
    void testCalculateRankingPointsReflectsActualState() {
        tgm.initGame();
        Player first  = tgm.getPlayers().get(0);
        Player second = tgm.getPlayers().get(1);
        Player third  = tgm.getPlayers().get(2);
        first.addPP(30); second.addPP(20); third.addPP(10);

        Map<Player, Integer> pts = tgm.calculateRankingPoints();
        assertTrue(pts.get(first) > pts.get(second));
        assertTrue(pts.get(second) > pts.get(third));
    }

    @Test
    @DisplayName("drawCard — status cambiato dopo draw: notifyStatusUpdate viene chiamato")
    void testDrawCardNotifiesStatusUpdateWhenStatusChanges() {
        ModelObserver listener = mock(ModelObserver.class);
        when(listener.getNickname()).thenReturn("Player1");
        tgm.addListener(listener);
        tgm.initGame();

        tgm.move(tgm.getBoard().getFirst());
        tgm.move(tgm.getBoard().get(1));
        tgm.move(tgm.getBoard().get(2));
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhaseEnum());
        List<CardEffectInstant> instantEffects = new ArrayList<>(List.of(new ProtectPP(ProtectPPEnum.PP_PROTECTION)));
        Builder cardWithProtect = new Builder(555099, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), instantEffects, 2, 1, CardTypeEnum.BUILDER);

        tgm.getLowerList().add(cardWithProtect);

        assertDoesNotThrow(() -> tgm.onDrawCardRequested(
                tgm.getCurrentPlayer().getNickname(), cardWithProtect.getId()));

        verify(listener, atLeastOnce()).onStatusUpdate(any());
    }
}
