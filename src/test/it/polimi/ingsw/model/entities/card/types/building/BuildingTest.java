package it.polimi.ingsw.model.entities.card.types.building;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import it.polimi.ingsw.visitors.VillageVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingTest {

    @Mock
    PlayEventVisitor visitorEv;

    @Mock
    Player mockPlayer;

    @Mock
    GainFood gainFoodMock;

    @Mock
    GainPP gainPPMock;

    @Mock
    DrawCardVisitor visitorDrawCard;

    @Mock
    VillageVisitor visitorVillage;

    @Test
    void testShouldAcceptVillageVisitor() {
        Building building = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.DRAW_PHASE,
                2, 1, 1, new ArrayList<>(), new ArrayList<>());

        building.accept(visitorVillage);

        verify(visitorVillage).visit(building);
    }


    @Test
    void testShouldGetPpValue() {
        Building building = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.DRAW_PHASE,
                2, 1, 1, new ArrayList<>(), new ArrayList<>());
        assertEquals(1, building.getPpValue());
    }

    @Test
    void testShouldGetFoodCost() {
        Building building = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.DRAW_PHASE,
                2, 1, 1, new ArrayList<>(), new ArrayList<>());
        assertEquals(1, building.getFoodCost());
    }

    @Test
    void testShouldAcceptDrawCardVisitor() {
        Building building = new Building(CardTypeEnum.BUILDING, 67, GamePhaseEnum.DRAW_PHASE,
                2, 1, 1, new ArrayList<>(), new ArrayList<>());

        building.accept(visitorDrawCard);

        verify(visitorDrawCard).visit(building);
    }

    @Test
    void testAcceptPlayEventVisitorIsNoOp() {
        Building building = new Building(CardTypeEnum.BUILDING, 67, GamePhaseEnum.DRAW_PHASE,
                2, 1, 1, new ArrayList<>(), new ArrayList<>());

        building.accept(visitorEv);

        verifyNoInteractions(visitorEv);
    }

    @Test
    void testAcceptCanDrawVisitor_playerCanAfford_setsMayDraw() {
        int foodCost = 3;
        Building building = new Building(CardTypeEnum.BUILDING, 10, GamePhaseEnum.DRAW_PHASE,
                1, 2, foodCost, new ArrayList<>(), new ArrayList<>());

        when(mockPlayer.getTotBuildDisc()).thenReturn(0);
        when(mockPlayer.getNFood()).thenReturn(5); // 5 >= 3

        CanDrawVisitor visitor = new CanDrawVisitor(mockPlayer);
        building.accept(visitor);

        assertTrue(visitor.getMayDraw());
        assertFalse(visitor.getMustDraw());
    }


    @Test
    void testAcceptCanDrawVisitor_playerCannotAfford_doesNotSetMayDraw() {
        int foodCost = 5;
        Building building = new Building(CardTypeEnum.BUILDING, 11, GamePhaseEnum.DRAW_PHASE,
                1, 2, foodCost, new ArrayList<>(), new ArrayList<>());

        when(mockPlayer.getTotBuildDisc()).thenReturn(0);
        when(mockPlayer.getNFood()).thenReturn(2); // 2 < 5

        CanDrawVisitor visitor = new CanDrawVisitor(mockPlayer);
        building.accept(visitor);

        assertFalse(visitor.getMayDraw());
    }

    @Test
    void testAcceptCanDrawVisitor_discountExceedsCost_clampedToZero() {
        int foodCost = 2;
        Building building = new Building(CardTypeEnum.BUILDING, 12, GamePhaseEnum.DRAW_PHASE,
                1, 2, foodCost, new ArrayList<>(), new ArrayList<>());

        when(mockPlayer.getTotBuildDisc()).thenReturn(10);
        when(mockPlayer.getNFood()).thenReturn(0);

        CanDrawVisitor visitor = new CanDrawVisitor(mockPlayer);
        building.accept(visitor);

        assertTrue(visitor.getMayDraw());
    }


    @Test
    void testShouldExecInteractiveEffect() {
        DrawCard mockEffect1 = mock(DrawCard.class);
        DrawCard mockEffect2 = mock(DrawCard.class);
        Action mockAction1 = mock(Action.class);
        Action mockAction2 = mock(Action.class);

        Building sut = new Building(CardTypeEnum.BUILDING, 1, GamePhaseEnum.DRAW_PHASE,
                3, 5, 4, new ArrayList<>(), Arrays.asList(mockEffect1, mockEffect2));

        when(mockEffect1.apply(mockPlayer)).thenReturn(mockAction1);
        when(mockEffect2.apply(mockPlayer)).thenReturn(mockAction2);

        List<Action> result = sut.execInteractiveEffect(mockPlayer);

        assertEquals(2, result.size(), "La lista deve contenere esattamente 2 azioni");
        assertEquals(mockAction1, result.get(0));
        assertEquals(mockAction2, result.get(1));

        verify(mockEffect1, times(1)).apply(mockPlayer);
        verify(mockEffect2, times(1)).apply(mockPlayer);
    }

    @Test
    void testExecInteractiveEffect_emptyList_returnsEmptyList() {
        Building sut = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.DRAW_PHASE,
                1, 0, 0, new ArrayList<>(), new ArrayList<>());

        List<Action> result = sut.execInteractiveEffect(mockPlayer);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}