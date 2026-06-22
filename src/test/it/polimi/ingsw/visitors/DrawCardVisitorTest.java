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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DrawCardVisitorTest {

    private static class TestableDrawCardVisitor extends DrawCardVisitor {
        public TestableDrawCardVisitor(Player player) {
            super(player);
        }
        public Player getCurrentPlayer() { return this.currPlayer; }
    }

    private TestableDrawCardVisitor dcv;

    @BeforeEach
    void setUp() {
        Player p1 = new Player("Player1", ColorPawnEnum.BLUE);
        dcv = new TestableDrawCardVisitor(p1);
    }

    @Test
    @DisplayName("visit(Event) — imposta il messaggio di errore per le carte evento")
    void visitEvent_setsCannotSelectEventError() {
        Feast e = new Feast(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 2, CardTypeEnum.FEAST);

        dcv.visit(e);

        assertEquals("Non puoi selezionare carte evento!", dcv.getErrorMessage());
    }

    @Test
    @DisplayName("visit(Building) — con cibo sufficiente scala il costo e aggiunge l'edificio")
    void visitBuilding_sufficientFood_deductsFoodAndAddsBuilding() {
        dcv.getCurrentPlayer().addFood(2);
        Building b = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());

        dcv.visit(b);

        assertEquals(0, dcv.getCurrentPlayer().getNFood());
        assertTrue(dcv.getCurrentPlayer().getBuildings().contains(b));
        assertEquals(1, dcv.getCurrentPlayer().getBuildings().size());
        assertFalse(dcv.hasErrorMessage());
    }

    @Test
    @DisplayName("visit(Building) — senza cibo sufficiente imposta il messaggio di errore")
    void visitBuilding_insufficientFood_setsErrorMessage() {
        Building b = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());

        dcv.visit(b);

        assertTrue(dcv.hasErrorMessage());
        assertEquals("Non disponi del cibo necessario per acquistare l'edificio scelto!", dcv.getErrorMessage());
        assertTrue(dcv.getCurrentPlayer().getBuildings().isEmpty());
    }

    @Test
    @DisplayName("visit(Character) — aggiunge la carta personaggio al giocatore")
    void visitCharacter_addsCardToPlayer() {
        Character b1 = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);

        dcv.visit(b1);

        assertEquals(1, dcv.getCurrentPlayer().getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    @DisplayName("hasErrorMessage — false inizialmente, true dopo un errore")
    void hasErrorMessage_falseInitially_trueAfterError() {
        assertFalse(dcv.hasErrorMessage());

        Feast e = new Feast(1, GamePhaseEnum.END_ROUND, new ArrayList<>(), new ArrayList<>(), 2, 2, 2, CardTypeEnum.FEAST);
        dcv.visit(e);

        assertTrue(dcv.hasErrorMessage());
    }

    @Test
    @DisplayName("getErrorMessage — stringa vuota prima di qualsiasi errore")
    void getErrorMessage_emptyBeforeAnyError() {
        assertEquals("", dcv.getErrorMessage());
    }

    @Test
    @DisplayName("getErrorMessage — restituisce il messaggio impostato dopo un errore")
    void getErrorMessage_returnsSetMessage_afterError() {
        Building b = new Building(CardTypeEnum.BUILDING, 3, GamePhaseEnum.SETUP_PHASE, 3, 4, 2, new ArrayList<>(), new ArrayList<>());

        dcv.visit(b);

        assertEquals("Non disponi del cibo necessario per acquistare l'edificio scelto!", dcv.getErrorMessage());
    }

    @Test
    void visit_building_discountExceedsCost_setsCanDraw() {
        Building cheapBuilding = new Building(
                CardTypeEnum.BUILDING, 99,
                GamePhaseEnum.NONE, 1, 0, 1,
                Collections.emptyList(), Collections.emptyList()
        );
        Player richDiscPlayer = new Player("test", ColorPawnEnum.BLUE);
        richDiscPlayer.addTotBuildDiscount(3);

        CanDrawVisitor visitor = new CanDrawVisitor(richDiscPlayer);
        visitor.visit(cheapBuilding);

        assertFalse(visitor.getMustDraw());
        assertTrue(visitor.getMayDraw());
    }
}