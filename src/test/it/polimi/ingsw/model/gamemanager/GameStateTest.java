package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.entities.card.types.character.Painter;
import it.polimi.ingsw.model.entities.card.types.event.Feast;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    @DisplayName("costruttore — numPlayers < 2 lancia IllegalArgumentException")
    void testShouldRejectTooFewPlayers() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameState(new ArrayList<>(), 1));
    }

    @Test
    @DisplayName("costruttore — numPlayers > 5 lancia IllegalArgumentException")
    void testShouldRejectTooManyPlayers() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameState(new ArrayList<>(), 6));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("initialize — ogni giocatore riceve cibo iniziale > 0")
    void testShouldAssignInitialFoodToAllPlayers(int numPlayers) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        GameState gs = new GameState(players, numPlayers);
        gs.initialize();
        players.forEach(p -> assertTrue(p.getNFood() > 0,
                p.getNickname() + " deve ricevere cibo iniziale"));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("initialize — il cibo totale distribuito è corretto per ogni configurazione")
    void testShouldAssignCorrectTotalFood(int numPlayers) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        GameState gs = new GameState(players, numPlayers);
        gs.initialize();

        int expected = switch (numPlayers) {
            case 2 -> 5;   // 2+3
            case 3 -> 8;   // 2+3+3
            case 4 -> 12;  // 2+3+3+4
            case 5 -> 16;  // 2+3+3+4+4
            default -> 0;
        };
        int totalFood = players.stream().mapToInt(Player::getNFood).sum();
        assertEquals(expected, totalFood,
                "cibo totale errato per " + numPlayers + " giocatori");
    }

    @Test
    @DisplayName("advanceAge — da età 1 a 2: carte spostate correttamente")
    void testShouldChangeAge1to2() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();

        Building build1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.SETUP_PHASE, 1, 4, 2, new ArrayList<>(), new ArrayList<>());
        Building build2 = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.SETUP_PHASE, 2, 4, 2, new ArrayList<>(), new ArrayList<>());
        gs.getUpperList().add(build1);
        gs.getBuildings().add(build2);

        gs.advanceAge();

        assertEquals(2, gs.getCurrAge());
        assertFalse(gs.getUpperList().contains(build1), "build1 deve essere rimossa da upperList");
        assertTrue(gs.getLowerList().contains(build1),  "build1 deve essere aggiunta a lowerList");
        assertTrue(gs.getUpperList().contains(build2),  "build2 deve essere aggiunta a upperList");
        assertFalse(gs.getLowerList().contains(build2), "build2 non deve essere in lowerList");
    }

    @Test
    @DisplayName("advanceAge — da età 2 a 3: lowerList pulita correttamente")
    void testShouldChangeAge2to3() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();
        gs.advanceAge();

        Building build3 = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());
        gs.getLowerList().add(build3);
        gs.advanceAge();

        assertEquals(3, gs.getCurrAge());
        assertFalse(gs.getLowerList().contains(build3), "build3 deve essere rimossa da lowerList");
    }

    @Test
    @DisplayName("advanceAge — a età 3 rimane 3 (terminale)")
    void testShouldStayAtAge3() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();
        gs.advanceAge();
        gs.advanceAge();
        assertEquals(3, gs.getCurrAge());

        Building build3 = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());
        gs.getLowerList().add(build3);
        gs.advanceAge();

        assertEquals(3, gs.getCurrAge(), "l'età non deve superare 3");
        assertFalse(gs.getLowerList().contains(build3));
    }

    @Test
    @DisplayName("refillBoard — rimuove eventi e personaggi dalla lowerList")
    void testShouldRefillBoardRemovesInLower() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();

        Builder b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Feast e1 = new Feast(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 2, CardTypeEnum.FEAST);
        gs.getLowerList().add(b1);
        gs.getLowerList().add(e1);
        gs.refillBoard();

        assertFalse(gs.getLowerList().contains(b1), "b1 deve essere rimosso dalla lowerList");
        assertFalse(gs.getLowerList().contains(e1), "e1 deve essere rimosso dalla lowerList");
    }

    @Test
    @DisplayName("refillBoard — sposta personaggi da upperList a lowerList")
    void testShouldRefillBoardUpperToLower() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();

        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        gs.getUpperList().add(b1);
        gs.refillBoard();

        assertFalse(gs.getUpperList().contains(b1), "b1 deve essere rimosso da upperList");
        assertTrue(gs.getLowerList().contains(b1),  "b1 deve essere aggiunto a lowerList");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("refillBoard — triggera changeAge quando la upperList si svuota")
    void testShouldRefillBoardTriggerChangeAge(int value) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        GameState gs = new GameState(players, value);
        gs.initialize();

        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        gs.getDeck().addFirst(b1);
        gs.refillBoard();

        int numValid = gs.getUpperList().stream()
                .filter(card -> card.getType() != CardTypeEnum.BUILDING)
                .toList().size();

        assertTrue(gs.getUpperList().contains(b1),  "b1 deve essere in upperList dopo refill");
        assertFalse(gs.getLowerList().contains(b1), "b1 non deve essere in lowerList");
        assertEquals(value + 4, numValid,
                "upperList deve contenere (numPlayers + 4) carte non-building");
    }

    @Test
    @DisplayName("applyFinalScores — punteggi finali calcolati correttamente")
    void testShouldApplyFinalScores() {
        Player p1 = new Player("abc", ColorPawnEnum.BLUE);
        Player p2 = new Player("def", ColorPawnEnum.ORANGE);
        GameState gs = new GameState(List.of(p1, p2), 2);

        GainPP e1 = new GainPP(CardTypeEnum.BUILDER, 3, GainPPEnum.PP_FOR_CAT);
        Building build1 = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.END_GAME, 2, 4, 2, List.of(e1), new ArrayList<>());
        Building build2 = new Building(CardTypeEnum.BUILDING, 12, null, 2, 5, 2, new ArrayList<>(), new ArrayList<>());
        Builder  b1  = new Builder(2,   GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Builder  b2  = new Builder(3,   GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 1, CardTypeEnum.BUILDER);
        Builder  b3  = new Builder(5,   GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, 2, CardTypeEnum.BUILDER);
        Crafter  c1  = new Crafter(123, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Crafter  c2  = new Crafter(456, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BOWL,     CardTypeEnum.CRAFTER);
        Crafter  c3  = new Crafter(789, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Crafter  c4  = new Crafter(999, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.ROPE,     CardTypeEnum.CRAFTER);
        Painter  pa1 = new Painter(45,  null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter  pa2 = new Painter(54,  null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter  pa3 = new Painter(543, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter  pa4 = new Painter(542, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);

        p1.addCard(b1); p1.addCard(b2); p2.addCard(b3);
        p1.addCard(c1); p1.addCard(c2); p1.addCard(c3); p2.addCard(c4);
        p1.addCard(pa1); p1.addCard(pa2); p2.addCard(pa3); p2.addCard(pa4);
        p1.addBuilding(build1); p2.addBuilding(build2);

        gs.applyFinalScores(GamePhaseEnum.END_GAME);

        assertEquals(28, p1.getPP(), "p1: 2 builder + 6 crafter + 10 painter + 4 building + 6 GainPP");
        assertEquals(18, p2.getPP(), "p2: 2 builder + 1 crafter + 10 painter + 5 building");
    }

    @Test
    @DisplayName("setCurrPlayer — il giocatore corrente viene aggiornato correttamente")
    void testShouldSetCurrPlayer() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);

        Player newCurrent = players.get(1);
        gs.setCurrPlayer(newCurrent);

        assertEquals(newCurrent, gs.getCurrPlayer());
    }

    @Test
    @DisplayName("restoreBoard — la board viene sostituita con quella fornita")
    void testShouldRestoreBoard() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);

        List<Tile> newBoard = new ArrayList<>(List.of(
                new Tile(1, 1, new ArrayList<>(), new ArrayList<>(), false, ""),
                new Tile(2, 1, new ArrayList<>(), new ArrayList<>(), false, "")));
        gs.restoreBoard(newBoard);

        assertEquals(newBoard, gs.getBoard());
    }

    @Test
    @DisplayName("applyFinalScores — building con effetto interattivo END_GAME: toDoActions popolata")
    void testShouldApplyFinalScoresWithEndGameInteractiveEffect() {
        Player p1 = new Player("P1", ColorPawnEnum.BLUE);
        GameState gs = new GameState(List.of(p1), 2);

        DrawCard drawEffect = new DrawCard(DrawCardEnum.UP_DRAW);
        Building build = new Building(CardTypeEnum.BUILDING, 99, GamePhaseEnum.END_GAME,
                1, 2, 0, new ArrayList<>(), new ArrayList<>(List.of(drawEffect)));
        p1.addBuilding(build);

        gs.applyFinalScores(GamePhaseEnum.END_GAME);

        assertTrue(p1.hasSkippableDraws());
        assertFalse(gs.getToDoActions().isEmpty());
    }

    @Test
    @DisplayName("applyDraw — carta evento in upperList: lancia InvalidDrawException")
    void testShouldThrowInvalidDrawExceptionWhenDrawingEventCard() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();

        Feast eventCard = new Feast(777, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 1, 2, 2, CardTypeEnum.FEAST);
        gs.getUpperList().add(eventCard);
        gs.getToDoActions();

        try {
            var field = GameState.class.getDeclaredField("toDoActions");
            field.setAccessible(true);
            ((List<Action>) field.get(gs)).add(new Action(players.get(0), DrawCardEnum.UP_DRAW));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        assertThrows(InvalidDrawException.class,
                () -> gs.applyDraw(eventCard, GamePhaseEnum.DRAW_PHASE));
    }

    @Test
    @DisplayName("toSnapshot — toDoAction con owner null mappa il nickname a 'SYSTEM'")
    void testShouldMapNullOwnerToSystem() {
        List<Player> players = new ArrayList<>(List.of(
                new Player("P1", ColorPawnEnum.BLUE),
                new Player("P2", ColorPawnEnum.ORANGE)));
        GameState gs = new GameState(players, 2);
        gs.initialize();

        try {
            var field = GameState.class.getDeclaredField("toDoActions");
            field.setAccessible(true);
            ((List<Action>) field.get(gs)).add(new Action(null, DrawCardEnum.UP_DRAW));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        GameSnapshot snap = gs.toSnapshot(1, GamePhaseEnum.DRAW_PHASE);

        assertEquals("SYSTEM", snap.getToDoActions().getFirst().getOwnerNickname());
    }

    @Test
    @DisplayName("EndGamePhaseState-NextState()")
    void testEndGamePhaseStateNextState() {
        EndGamePhaseState endGamePhaseState = new EndGamePhaseState();
        List<Player> players = new ArrayList<>();
        players.add(new Player("p1", ColorPawnEnum.ORANGE));
        players.add(new Player("p2", ColorPawnEnum.BLUE));
        List<ModelObserver> listeners = new ArrayList<>();

        GameManager gameManager = new GameManager(listeners, players, players.size(), null);
        gameManager.setCurrPhaseState(endGamePhaseState);

        GamePhaseState result = endGamePhaseState.nextPhase(gameManager);

        assertEquals(GamePhaseEnum.END_GAME, result.getPhase());
    }

    @Test
    @DisplayName("EndTurnPhaseState-ShouldGive-OptionalDraw")
    void testEndTurnPhaseStateShouldGiveOptionalDraw() {
        EndTurnPhaseState endTurnPhaseState = new EndTurnPhaseState();
        List<Player> players = new ArrayList<>();
        players.add(new Player("p1", ColorPawnEnum.ORANGE));
        players.add(new Player("p2", ColorPawnEnum.BLUE));
        GameState state = new GameState(players, players.size());

        List<Tile> queue = state.getQueue();
        for (int i = 0; i < players.size(); i++) {
            Tile t = new Tile(i, players.size(), null, null, false, null);
            t.setPlayer(players.get(i % players.size()));
            queue.add(t);
        }

        state.setCurrPlayer(players.get(0));

        GameManager gameManager = new GameManager(new ArrayList<>(), players, players.size(), null);
        gameManager.setState(state);

        Player p2 = players.get(1);

        p2.addSkippableDraws(List.of(new Action(p2, DrawCardEnum.UP_DRAW)));

        GamePhaseState result = endTurnPhaseState.nextPhase(gameManager);

        assertInstanceOf(OptionalDrawPhaseState.class, result,
                "Con coda piena e draw accantonabili pendenti la fase successiva deve essere OptionalDrawPhaseState");
        assertEquals(GamePhaseEnum.OPTIONAL_DRAW_PHASE, result.getPhase());

    }
}