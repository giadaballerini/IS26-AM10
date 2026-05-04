
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
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.visitors.ClientMessageVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GameManagerTest{

    private static class TestableGameManager extends GameManager {
        public TestableGameManager(List<ModelObserver> l, List<Player> p, int n) {
            super(l, p, n);
        }
        public List<Tile> getBoard() { return this.board; }
        public int getCurrAge(){return this.currAge;}
        public List<Player> getPlayers(){return this.players;}
        
        public List<Card> getUpperList(){return this.upperList;}

        public List<Card> getLowerList(){return this.lowerList;}

        public List<Tile> getQueue(){return this.queue;}

        public List<Card> getBuildings(){return this.buildings;}

        public List<Card> getDeck(){return this.deck;}

        public GamePhaseState getCurrPhase(){return this.currPhaseState;}

        public void consumeAction() {
            if (!getToDoActions().isEmpty()) {
                getToDoActions().removeFirst();
            }
        }
        public List<ModelObserver> getListeners() {
            return this.listeners;
        }

        public void setTurn(int i){this.currTurn = i;}
        public List<Action> getToDoActions(){
            return this.toDoActions;
        }

    }

    private TestableGameManager tgm;
    @BeforeEach
    void setUp() {
        List<ModelObserver> listeners = new ArrayList<ModelObserver>();
        List<Player> players = new ArrayList<Player>();
        players.add(new Player("Player1", ColorPawnEnum.BLUE));
        players.add(new Player("Player2", ColorPawnEnum.ORANGE));
        tgm = new TestableGameManager(listeners, players, 2);

    }



    @ParameterizedTest
    @ValueSource(ints= {2, 3, 4, 5})
    @DisplayName("Test initGame")
    void testShouldInitGameForValidValues(int value) {

        List<Player> players = new ArrayList<>();
        List<ModelObserver> listeners = new ArrayList<>();
        for(int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        tgm = new TestableGameManager(listeners, players, value);
        tgm.initGame();
        
        assertNotNull(tgm.getBoard(), "la board non può essere null");
        assertFalse(tgm.getBoard().isEmpty(), "la board deve contenere delle tile");
        
        int expectedUpper = value + 4;
        int sub = switch (value) {
            case 2 -> 1;
            case 3,4 -> 2;
            case 5 -> 3;
            default -> 0;
            };

        assertEquals(expectedUpper,tgm.getUpperList().size() - sub);

        assertFalse(tgm.getLowerList().isEmpty());
        assertEquals(value + 1,tgm.getLowerList().size());

        assertEquals(1, tgm.getCurrAge());
        assertNotNull(tgm.getCurrentPlayer());
    }

    @ParameterizedTest
    @ValueSource(ints= {0,1})
    @DisplayName("Test notInitGame")
    void testShouldNotInitGameForInvalidValues(int value) {

        List<Player> players = new ArrayList<>();
        List<ModelObserver> listeners = new ArrayList<>();
        for(int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        tgm = new TestableGameManager(listeners, players, value);
        tgm.initGame();

        assertEquals(0,tgm.deck.size());
        assertEquals(0,tgm.getBuildings().size());

        assertTrue(tgm.getBoard().isEmpty());

        assertTrue(tgm.getQueue().isEmpty());

        assertTrue(tgm.getUpperList().isEmpty());

        assertTrue(tgm.getLowerList().isEmpty());
    }


    @Test
    @DisplayName("Test changeAge1to2")
    void testShouldChangeAge1to2() {
        tgm.initGame();

        Building build1 = new Building(CardTypeEnum.BUILDING, 1,GamePhaseEnum.SETUP_PHASE, 1, 4,2, new ArrayList<>(), new ArrayList<>());
        tgm.getUpperList().add(build1);

        Building build2 = new Building(CardTypeEnum.BUILDING, 1,GamePhaseEnum.SETUP_PHASE, 2, 4,2, new ArrayList<>(), new ArrayList<>());
        tgm.getBuildings().add(build2);

        tgm.changeAge();


        assertEquals(2, tgm.getCurrAge());
        assertFalse(tgm.getUpperList().contains(build1));
        assertTrue(tgm.getLowerList().contains(build1));
        assertTrue(tgm.getUpperList().contains(build2));
        assertFalse(tgm.getLowerList().contains(build2));
    }

    @Test
    @DisplayName("Test changeAge2to3")
    void testShouldChangeAge2to3() {
        tgm.initGame();

        tgm.changeAge();

        Building build3 = new Building(CardTypeEnum.BUILDING,3,GamePhaseEnum.SETUP_PHASE, 3, 4,2,new ArrayList<>(), new ArrayList<>());
        tgm.getLowerList().add(build3);

        tgm.changeAge();

        assertEquals(3, tgm.getCurrAge());

        assertFalse(tgm.getLowerList().contains(build3));
    }


    @Test
    @DisplayName("Test changeAge3to3")
    void testShouldChangeAge3to3() {
        tgm.initGame();

        tgm.changeAge();

        Building build3 = new Building(CardTypeEnum.BUILDING, 3,GamePhaseEnum.SETUP_PHASE,3, 4,2,new ArrayList<>(), new ArrayList<>());
        tgm.getLowerList().add(build3);

        tgm.changeAge();

        assertEquals(3, tgm.getCurrAge());

        assertFalse(tgm.getLowerList().contains(build3));

        tgm.getLowerList().add(build3);

        tgm.changeAge();

        assertEquals(3, tgm.getCurrAge());
        assertFalse(tgm.getLowerList().contains(build3));

    }

    @Test
    @DisplayName("Test nextPhase setup")
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
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t = new Tile('G', 1, new ArrayList<>(), effs);

        tgm.getBoard().addFirst(t);

        tgm.move(t);

        tgm.move(tgm.getBoard().get(1));

        assertTrue(tgm.isQueueEmpty());

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

    }

    @Test
    @DisplayName("Test nextPhase draw to end")
    void testShouldNextPhaseDrawToEnd() {

        tgm.initGame();

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t1 = new Tile('G', 1, new ArrayList<>(), effs);
        Tile t2 = new Tile('H', 1, new ArrayList<>(), new ArrayList<>());

        tgm.getBoard().addFirst(t2);
        tgm.getBoard().addFirst(t1);

        tgm.move(tgm.getBoard().getFirst());

        tgm.move(tgm.getBoard().get(1));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.consumeAction();

        assertTrue(tgm.getToDoActions().isEmpty());
        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(),tgm.getQueueSize());

    }

    @Test
    @DisplayName("Test nextPhase draw to draw")
    void testShouldNextPhaseDrawToDraw() throws IllegalAccessException, NoSuchFieldException {

        tgm.initGame();
        Action a = new Action(tgm.getPlayers().get(1),DrawCardEnum.DOWN_DRAW);
        tgm.getToDoActions().add(a);


        tgm.move(tgm.getBoard().getFirst());
        tgm.nextPlayer();
        tgm.move(tgm.getBoard().get(1));
        tgm.nextPhase();
        GamePhaseState stateBefore = tgm.getCurrPhase();
        tgm.nextPhase();

        assertSame(stateBefore, tgm.getCurrPhase());
        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

    }


    void testShouldNextPhaseEndToDraw() {

        tgm.initGame();

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t1 = new Tile('G', 2, new ArrayList<>(), effs);
        Tile t2 = new Tile('H', 2, new ArrayList<>(), new ArrayList<>());

        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);


        tgm.move(tgm.getBoard().getFirst());

        tgm.move(tgm.getBoard().get(1));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

    }

    @Test
    @DisplayName("Test nextPhase EndTurn to EndRound")
    void testShouldNextPhaseEndTurnToEndRound() {


        tgm.initGame();

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t1 = new Tile('G', 2, new ArrayList<>(), effs);
        Tile t2 = new Tile('H', 2, new ArrayList<>(), new ArrayList<>());

        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);


        tgm.move(tgm.getBoard().getFirst());

        tgm.move(tgm.getBoard().get(1));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.getToDoActions().clear();

        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());

    }

    @Test
    @DisplayName("Test nextPhase EndRound to SetUp")
    void testShouldNextPhaseEndRoundToSetUp() {


        tgm.initGame();

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t1 = new Tile('G', 2, new ArrayList<>(), effs);
        Tile t2 = new Tile('H', 2, new ArrayList<>(), new ArrayList<>());

        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);


        tgm.move(tgm.getBoard().getFirst());

        tgm.move(tgm.getBoard().get(1));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.getToDoActions().clear();

        tgm.nextPhase();
        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());

        assertEquals(GamePhaseEnum.SETUP_PHASE, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Next Phase test from endRound to playEvent")
    void testShouldNextPhaseEndRoundToPlayEvent() {
        tgm.initGame();



        tgm.initGame();

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t1 = new Tile('G', 2, new ArrayList<>(), effs);
        Tile t2 = new Tile('H', 2, new ArrayList<>(), new ArrayList<>());

        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);


        tgm.move(tgm.getBoard().getFirst());

        tgm.move(tgm.getBoard().get(1));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.getToDoActions().clear();

        tgm.setTurn(10);

        tgm.nextPhase();


        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());

        //assert per verificare che playEvent venga chiamato DA AGGIUNGERE

        assertEquals(GamePhaseEnum.END_GAME, tgm.getCurrPhase().getPhase());
    }
    @Test
    @DisplayName("Next Phase test from playEvent to endGame")
    void testShouldNextPhasePlayEventToEndGame() {
        tgm.initGame();



        tgm.initGame();

        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Tile t1 = new Tile('G', 2, new ArrayList<>(), effs);
        Tile t2 = new Tile('H', 2, new ArrayList<>(), new ArrayList<>());

        tgm.getBoard().addFirst(t1);
        tgm.getBoard().addFirst(t2);


        tgm.move(tgm.getBoard().getFirst());

        tgm.move(tgm.getBoard().get(1));

        assertEquals(GamePhaseEnum.DRAW_PHASE, tgm.getCurrPhase().getPhase());

        tgm.getToDoActions().clear();

        tgm.setTurn(10);
        tgm.nextPhase();

        assertEquals(tgm.getNumPlayers(), tgm.getQueueSize());

        assertEquals(GamePhaseEnum.END_GAME, tgm.getCurrPhase().getPhase());
    }

    @Test
    @DisplayName("Test refillBoardInLower")
    void testShouldRefillBoardRemovesInLower() {
        tgm.initGame();
        Builder b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        Event e1 = new Feast(1,GamePhaseEnum.END_ROUND,new ArrayList<>(),new ArrayList<>(),2,2,2,CardTypeEnum.FEAST);
        tgm.getLowerList().add(b1);
        tgm.getLowerList().add(e1);
        tgm.refillBoard();

        assertFalse(tgm.getLowerList().contains(b1));
        assertFalse(tgm.getLowerList().contains(e1));

    }

    @Test
    @DisplayName("Test refillBoardInLower")
    void testShouldRefillBoardUpperToLower() {
        tgm.initGame();
        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,  CardTypeEnum.BUILDER);

        tgm.getUpperList().add(b1);
        tgm.refillBoard();

        assertFalse(tgm.getUpperList().contains(b1));
        assertTrue(tgm.getLowerList().contains(b1));


    }

    @ParameterizedTest
    @ValueSource(ints= {2, 3, 4, 5})
    @DisplayName("Test refillBoardChangeAge")
    void testShouldRefillBoardChangeAge(int value) {

        List<Player> players = new ArrayList<>();
        List<ModelObserver> listeners = new ArrayList<>();
        for(int i = 0; i < value; i++)
            players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));

        tgm = new TestableGameManager(listeners, players, value);
        tgm.initGame();
        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,  CardTypeEnum.BUILDER);
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


    @Test
    @DisplayName("Test don't move")
    void testShouldNotMove() throws OccupiedTileException {

        tgm.initGame();
        Tile targetTile = tgm.getBoard().getFirst();

        Player testP = new Player("TestPlayer", ColorPawnEnum.WHITE);
        try{
            targetTile.setPlayer(testP);
        } catch(OccupiedTileException e){
            fail("La tile doveva essere libera durante il setup del test");
        }
        assertThrows(OccupiedTileException.class, () -> {
            tgm.move(targetTile);
        });
        assertEquals(testP, targetTile.getPlayer());
    }


     @Test
     @DisplayName("Test move")
     void testShouldMove() {
         tgm.initGame();

         Tile targetTile = tgm.getBoard().getFirst();
         Player currPlayer = tgm.getCurrentPlayer();

         tgm.move(targetTile);

         assertTrue(targetTile.isOccupied());
         assertEquals(currPlayer, targetTile.getPlayer());

     }


    @Test
    @DisplayName("Final score test")
    void testShouldFinalScoreCount() {

        List<Player> players = new ArrayList<>();
        Player p1 = new Player("abc", ColorPawnEnum.BLUE);
        assertNotNull(p1);
        Player p2 = new Player("def", ColorPawnEnum.ORANGE);
        assertNotNull(p2);
        players.add(p1);
        players.add(p2);
        GameManager gm = new GameManager(new ArrayList<>(), players, 2);
        
        CardEffectInstant e1 = new GainPP(CardTypeEnum.BUILDER, 3, GainPPEnum.PP_FOR_CAT);

        List<CardEffectInstant> eff = new ArrayList<>();
        eff.add(e1);

        Building build1 = new Building(CardTypeEnum.BUILDING,1,GamePhaseEnum.SETUP_PHASE, 2, 4,2, eff, new ArrayList<>());
        Building build2 = new Building(CardTypeEnum.BUILDING,12,null, 2, 5,2,new ArrayList<>(), new ArrayList<>());
        Builder b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        Builder b2 = new Builder(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        Builder b3 = new Builder(5, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.BUILDER);
        Crafter c1 = new Crafter(123, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Crafter c2 = new Crafter(456, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BOWL, CardTypeEnum.CRAFTER);
        Crafter c3 = new Crafter(789, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        Painter pa1 = new Painter(45,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter pa2 = new Painter(54,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter pa3 = new Painter(543,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter pa4 = new Painter(542,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);

        p1.addCard(b1);
        p1.addCard(b2);
        p2.addCard(b3);
        p1.addCard(c1);
        p1.addCard(c2);
        p1.addCard(c3);
        p2.addCard(c3);
        p1.addCard(pa1);
        p1.addCard(pa2);
        p2.addCard(pa3);
        p2.addCard(pa4);
        p1.addBuilding(build1);
        p2.addBuilding(build2);
        
        gm.finalScoreCount();

        assertEquals(26, p1.getPP());
        assertEquals(16, p2.getPP());


    }

    @Test
    @DisplayName("Food winners")
    void testShouldGameWinnersTiedPP() {
        tgm.initGame();
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);

        p1.addPP(50);
        p1.addFood(2);

        p2.addPP(50);
        p2.addFood(5);

        List<Player> winners = tgm.gameWinners();

        assertEquals(1, winners.size());
        assertEquals(p2, winners.getFirst());
    }

    @Test
    @DisplayName("Tied winners")
    void testShouldGameWinnersTiedAll() {
        tgm.initGame();
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);

        p1.addPP(50);
        p1.addFood(1);
        p2.addPP(50);

        List<Player> winners = tgm.gameWinners();

        assertEquals(2, winners.size());
        assertTrue(winners.contains(p1));
        assertTrue(winners.contains(p2));
    }

    @Test
    @DisplayName("standard winners")
    void testShouldGameWinnersStandard() {
        tgm.initGame();
        Player p1 = tgm.getPlayers().get(0);
        Player p2 = tgm.getPlayers().get(1);

        p1.addPP(50);
        p1.addFood(2);

        p2.addPP(20);
        p2.addFood(50);

        List<Player> winners = tgm.gameWinners();

        assertEquals(1, winners.size());
        assertEquals(p1, winners.getFirst());
    }
    @Test
    @DisplayName("No players in game win")
    void testShouldNoWinners(){
        TestableGameManager errorGm = new TestableGameManager(null, new ArrayList<>(),2);
        List<Player> winners = errorGm.gameWinners();

        assertNotNull(winners);
        assertEquals(0, winners.size());
    }
    @Test
    void testShouldGetCurrentPlayer() {
        tgm.initGame();
        assertEquals(tgm.getQueue().getFirst().getPlayer(), tgm.getCurrentPlayer());
    }

    @Test
    void testShouldAddListener() {
        ModelObserver l = new ModelObserver(){
            @Override
            public void onErrorMessage(String errorMsg) {

            }
            @Override
            public void onEvent(String e){

            }
            @Override
            public void onClientMessage(ClientMessage m) {

            }

            @Override
            public String getNickname() {
                return "";
            }

            @Override
            public void onCurrPlayerUpdate(String nickname) {

            }

            @Override
            public void onMoveUpdate(TileDTO tile, String nextPlayer) {

            }

            @Override
            public void onPhaseUpdate(PhaseDTO phaseDTO) {

            }

            @Override
            public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) {

            }

            @Override
            public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos) {

            }

            @Override
            public void onDrawUpdate(CardDTO cardDTO, String nickname) {

            }

            @Override
            public void onStatusUpdate(PlayerStatusDTO status) {

            }
            @Override
            public void onChangeAge(ChangeAgeDTO ageDTO) {

            }

            @Override
            public void onStatsUpdate(PlayerStatsDTO stats, int cardId) {

            }

            @Override
            public void refresh(List<PlayerDTO> listPlayers, BoardDTO board) {

            }

            @Override
            public void showBoard(BoardDTO board) {

            }


            @Override
            public void notifySkip(String nickname) {

            }

            @Override
            public void notifyDrawable(ActionsDTO actions) {

            }

            @Override
            public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO stats) {

            }
            @Override
            public void setVisitor(ClientMessageVisitor v){

            }
        };
        assertEquals(0, tgm.getListeners().size());
        tgm.addListener(l);
        assertEquals(1, tgm.getListeners().size());
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
    @DisplayName("Draw Card from lower test ")
    void testShouldDrawCardLowerFine() {
        tgm.initGame();
        Action a = new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW);
        tgm.getToDoActions().add(a);
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);
        tgm.drawCard(b1);

        assertDoesNotThrow(() -> tgm.drawCard(b1));

        assertFalse(tgm.getLowerList().contains(b1));
        assertEquals( 1, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("Draw Card test")
    void testShouldDrawCardUpperFine() {
        tgm.initGame();
        Action a = new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW);
        tgm.getToDoActions().add(a);
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);
        tgm.drawCard(b1);

        assertDoesNotThrow(() -> tgm.drawCard(b1));

        assertFalse(tgm.getUpperList().contains(b1));
        assertEquals( 1, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("Draw Card test")
    void testShouldDrawCardUpperNotFine() {
        tgm.initGame();
        Action a = new Action(tgm.getCurrentPlayer(), DrawCardEnum.UP_DRAW);
        tgm.getToDoActions().add(a);
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        tgm.getLowerList().add(b1);

        InvalidDrawException ex = assertThrows(InvalidDrawException.class, () -> {
            tgm.drawCard(b1);
        });

        assertEquals("Fila non valida", ex.getMessage());

        assertTrue(tgm.getLowerList().contains(b1));
        assertEquals( 0, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("Draw Card test")
    void testShouldDrawCardLowerNotFine() {
        tgm.initGame();
        Action a = new Action(tgm.getCurrentPlayer(), DrawCardEnum.DOWN_DRAW);
        tgm.getToDoActions().add(a);
        Builder b1 = new Builder(240921, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.BUILDER);
        tgm.getUpperList().add(b1);

        InvalidDrawException ex = assertThrows(InvalidDrawException.class, () -> {
            tgm.drawCard(b1);
        });

        assertEquals("Fila non valida", ex.getMessage());

        assertTrue(tgm.getUpperList().contains(b1));
        assertEquals( 0, tgm.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("defautlt initFood")
    void testShouldInitFood(){
        int value = 6;
        List<Player> players = new ArrayList<Player>();
        for(int i = 0; i < value; i++){
            if(i < 5)
                players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
            else
                players.add(new Player("Player" + i, ColorPawnEnum.BLUE));
        }
        GameManager gm = new GameManager(new ArrayList<>(),players,value);

        gm.initGame();

        assertEquals(0, players.get(value - 1).getNFood());
    }

    @Test
    void testShouldGetToDoActions(){
        int value = 5;
        List<Player> players = new ArrayList<Player>();
        for(int i = 0; i < value; i++){
                players.add(new Player("Player" + i, ColorPawnEnum.values()[i]));
        }
        GameManager gm = new GameManager(new ArrayList<>(),players,value);

        assertEquals(0, gm.getToDoActions().size());
    }


    @Test
    @DisplayName("checkEffects with non empty list")
    void checkEffectsNotEmpty(){
        tgm.initGame();
        CardEffectInteractive eff = new DrawCard(DrawCardEnum.UP_DRAW);
        List<CardEffectInteractive> effs = new ArrayList<>();
        effs.add(eff);
        Building build1 = new Building(CardTypeEnum.BUILDING, 1,GamePhaseEnum.SETUP_PHASE,2, 4,2, new ArrayList<>(), effs);
        tgm.getCurrentPlayer().addBuilding(build1);
        tgm.checkEffects();

        assertEquals(1, tgm.getToDoActions().size());
        assertSame(DrawCardEnum.UP_DRAW, tgm.getToDoActions().getFirst().getType());

    }

}