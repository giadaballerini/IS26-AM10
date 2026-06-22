package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestoredGameManagerTest {


    private static class TestableRestoredGameManager extends RestoredGameManager {
        TestableRestoredGameManager(GameSnapshot snap, List<ModelObserver> obs, Runnable cb) {
            super(snap, obs, cb);
        }
        GamePhaseEnum getCurrPhaseEnum() { return super.getCurrPhase(); }
    }


    private Player p1, p2;
    private ModelObserver obs1, obs2;

    @BeforeEach
    void setUp() {
        p1 = new Player("Alice", ColorPawnEnum.BLUE);
        p2 = new Player("Bob", ColorPawnEnum.ORANGE);
        obs1 = mock(ModelObserver.class);
        obs2 = mock(ModelObserver.class);
        when(obs1.getNickname()).thenReturn("Alice");
        when(obs2.getNickname()).thenReturn("Bob");
    }


    private GameSnapshot snapshot(GamePhaseEnum phase, String currNick) {
        return new GameSnapshot(
                42, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(
                        new Tile(1, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(
                        new Tile(10, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(11, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(p1, p2),
                currNick,
                phase,
                1, 3,
                false,
                new ArrayList<>()
        );
    }


    @Test
    @DisplayName("costruttore — numPlayers ripristinato correttamente dallo snapshot")
    void constructor_restoresNumPlayers() {
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.SETUP_PHASE, "Alice"), List.of(obs1, obs2), () -> {});
        assertEquals(2, rgm.getNumPlayers());
    }

    @Test
    @DisplayName("costruttore — currTurn ripristinato correttamente dallo snapshot")
    void constructor_restoresCurrTurn() {
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.SETUP_PHASE, "Alice"), List.of(obs1, obs2), () -> {});
        assertEquals(3, rgm.getCurrTurn());
    }

    @ParameterizedTest
    @EnumSource(value = GamePhaseEnum.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("costruttore — fase ripristinata correttamente per ogni GamePhaseEnum valido")
    void constructor_restoresPhaseForAllValidValues(GamePhaseEnum phase) {
        TestableRestoredGameManager rgm = new TestableRestoredGameManager(
                snapshot(phase, "Alice"), List.of(obs1, obs2), () -> {});
        assertEquals(phase, rgm.getCurrPhaseEnum());
    }

    @Test
    @DisplayName("costruttore — fase NONE lancia IllegalArgumentException")
    void constructor_nonePhaseThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                new RestoredGameManager(
                        snapshot(GamePhaseEnum.NONE, "Alice"), List.of(obs1, obs2), () -> {}));
    }

    @Test
    @DisplayName("costruttore — callback passato al costruttore viene restituito da getOnGameEndedCallback")
    void constructor_storesOnGameEndedCallback() {
        Runnable cb = () -> {};
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.SETUP_PHASE, "Alice"), List.of(obs1, obs2), cb);
        assertSame(cb, rgm.getOnGameEndedCallback());
    }

    @Test
    @DisplayName("costruttore — toDoActions ripristinate: azione pendente viene ricreata")
    void constructor_restoresPendingToDoAction() {
        GameSnapshot snap = new GameSnapshot(
                1, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(
                        new Tile(1, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(
                        new Tile(10, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(11, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(p1, p2),
                "Alice",
                GamePhaseEnum.DRAW_PHASE,
                1, 2, false,
                List.of(new GameSnapshot.PendingAction("Alice", DrawCardEnum.UP_DRAW))
        );

        RestoredGameManager rgm = new RestoredGameManager(snap, List.of(obs1, obs2), () -> {});

        assertEquals(1, rgm.getToDoActions().size());
        assertEquals(DrawCardEnum.UP_DRAW, rgm.getToDoActions().getFirst().getType());
    }


    @Test
    @DisplayName("reconnectTilePlayers — tile board occupata: player object viene ripristinato")
    void constructor_reconnectsBoardTileToPlayer() {
        Tile occupied = new Tile(1, 2, new ArrayList<>(), new ArrayList<>(), true, "Alice");
        Tile free     = new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "");

        GameSnapshot snap = new GameSnapshot(
                1, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(occupied, free),
                List.of(
                        new Tile(10, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(11, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(p1, p2), "Alice",
                GamePhaseEnum.SETUP_PHASE, 1, 1, false, new ArrayList<>()
        );

        new RestoredGameManager(snap, List.of(obs1, obs2), () -> {});

        assertNotNull(occupied.getPlayer());
        assertEquals("Alice", occupied.getPlayer().getNickname());
    }

    @Test
    @DisplayName("reconnectTilePlayers — tile queue occupata: player object viene ripristinato")
    void constructor_reconnectsQueueTileToPlayer() {
        Tile qOccupied = new Tile(10, 2, new ArrayList<>(), new ArrayList<>(), true, "Bob");
        Tile qFree     = new Tile(11, 2, new ArrayList<>(), new ArrayList<>(), false, "");

        GameSnapshot snap = new GameSnapshot(
                1, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(
                        new Tile(1, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(qOccupied, qFree),
                List.of(p1, p2), "Alice",
                GamePhaseEnum.SETUP_PHASE, 1, 1, false, new ArrayList<>()
        );

        new RestoredGameManager(snap, List.of(obs1, obs2), () -> {});

        assertNotNull(qOccupied.getPlayer());
        assertEquals("Bob", qOccupied.getPlayer().getNickname());
    }

    @Test
    @DisplayName("reconnectTilePlayers — tile libera (nickname vuoto): player rimane null")
    void constructor_freeTilePlayerRemainsNull() {
        Tile free = new Tile(1, 2, new ArrayList<>(), new ArrayList<>(), false, "");

        GameSnapshot snap = new GameSnapshot(
                1, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(free, new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "")),
                List.of(
                        new Tile(10, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(11, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(p1, p2), "Alice",
                GamePhaseEnum.SETUP_PHASE, 1, 1, false, new ArrayList<>()
        );

        new RestoredGameManager(snap, List.of(obs1, obs2), () -> {});

        assertNull(free.getPlayer());
    }

    @Test
    @DisplayName("reconnectTilePlayers — nickname sconosciuto: player rimane null")
    void constructor_unknownNicknameLeavesPlayerNull() {
        Tile ghost = new Tile(1, 2, new ArrayList<>(), new ArrayList<>(), true, "NonEsiste");

        GameSnapshot snap = new GameSnapshot(
                1, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                List.of(ghost, new Tile(2, 2, new ArrayList<>(), new ArrayList<>(), false, "")),
                List.of(
                        new Tile(10, 2, new ArrayList<>(), new ArrayList<>(), false, ""),
                        new Tile(11, 2, new ArrayList<>(), new ArrayList<>(), false, "")
                ),
                List.of(p1, p2), "Alice",
                GamePhaseEnum.SETUP_PHASE, 1, 1, false, new ArrayList<>()
        );

        new RestoredGameManager(snap, List.of(obs1, obs2), () -> {});

        assertNull(ghost.getPlayer());
    }

    @Test
    @DisplayName("resume — showBoard notificato su tutti gli observer")
    void resume_notifiesShowBoard() {
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.SETUP_PHASE, "Alice"), List.of(obs1, obs2), () -> {});
        rgm.resume();
        verify(obs1, times(1)).showBoard(any());
        verify(obs2, times(1)).showBoard(any());
    }

    @Test
    @DisplayName("resume — onPhaseUpdate notificato su tutti gli observer")
    void resume_notifiesPhaseUpdate() {
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.DRAW_PHASE, "Alice"), List.of(obs1, obs2), () -> {});
        rgm.resume();
        verify(obs1, times(1)).onPhaseUpdate(any());
        verify(obs2, times(1)).onPhaseUpdate(any());
    }

    @Test
    @DisplayName("resume — onCurrPlayerUpdate notificato su tutti gli observer")
    void resume_notifiesCurrPlayerUpdate() {
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.SETUP_PHASE, "Alice"), List.of(obs1, obs2), () -> {});
        rgm.resume();
        verify(obs1, times(1)).onCurrPlayerUpdate(any());
        verify(obs2, times(1)).onCurrPlayerUpdate(any());
    }

    @Test
    @DisplayName("resume — le 3 notifiche vengono inviate esattamente una volta per observer")
    void resume_exactlyThreeNotificationsPerObserver() {
        RestoredGameManager rgm = new RestoredGameManager(
                snapshot(GamePhaseEnum.END_ROUND, "Bob"), List.of(obs1, obs2), () -> {});
        rgm.resume();

        verify(obs1, times(1)).showBoard(any());
        verify(obs1, times(1)).onPhaseUpdate(any());
        verify(obs1, times(1)).onCurrPlayerUpdate(any());

        verify(obs2, times(1)).showBoard(any());
        verify(obs2, times(1)).onPhaseUpdate(any());
        verify(obs2, times(1)).onCurrPlayerUpdate(any());
    }
}