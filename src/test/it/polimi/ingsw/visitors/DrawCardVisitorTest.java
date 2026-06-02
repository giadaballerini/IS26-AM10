package it.polimi.ingsw.visitors;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Feast;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DrawCardVisitorTest {
    public class TestableDrawCardVisitor extends DrawCardVisitor{
        public TestableDrawCardVisitor(Player player) {
            super(player);
        }
        public Player getCurrentPlayer(){return this.currPlayer;}
    }
    private TestableDrawCardVisitor dcv;
    @BeforeEach
    void setUp() {
        Player p1 = new Player("Player1", ColorPawnEnum.BLUE);
        dcv = new TestableDrawCardVisitor(p1);
    }
    @Test
    void testVisitEvent() {
        Feast e = new Feast(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 2, CardTypeEnum.FEAST);
        dcv.visit(e);
        assertEquals("Non puoi selezionare carte evento!", dcv.getErrorMessage());

    }

    @Test
    void testVisitBuildingIfBranch() {
        dcv.getCurrentPlayer().addFood(2);
        Building b = new Building(CardTypeEnum.BUILDING,3,GamePhaseEnum.SETUP_PHASE, 3, 4,2, new ArrayList<>(), new ArrayList<>());
        dcv.visit(b);

        assertEquals(0, dcv.getCurrentPlayer().getNFood());
        assertTrue(dcv.getCurrentPlayer().getBuildings().contains(b));
        assertEquals(1, dcv.getCurrentPlayer().getBuildings().size());
    }

    @Test
    void testVisitBuildingElseBranch() {
        Building b = new Building(CardTypeEnum.BUILDING,3,GamePhaseEnum.SETUP_PHASE, 3, 4,2, new ArrayList<>(), new ArrayList<>());
        dcv.visit(b);
        assertTrue(dcv.hasErrorMessage());
        assertEquals("Non disponi del cibo necessario per acquistare l'edificio scelto!", dcv.getErrorMessage());
    }

    @Test
    void testVisitCharacter() {
        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,2,  CardTypeEnum.BUILDER);
        dcv.visit(b1);
        assertTrue(dcv.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER) == 1);
    }

    @Test
    void hasErrorMessage() {
        assertFalse(dcv.hasErrorMessage());
        Feast e = new Feast(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 2, CardTypeEnum.FEAST);
        dcv.visit(e);
        assertTrue(dcv.hasErrorMessage());
    }

    @Test
    void getErrorMessage() {
    }
}