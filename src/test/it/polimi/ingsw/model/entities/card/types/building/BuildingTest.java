package it.polimi.ingsw.model.entities.card.types.building;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BuildingTest {

    @Mock
    GainFoodVisitor visitorGFood;

    @Mock
    GainPPVisitor visitorPP;

    @Mock
    PlayEventVisitor visitorEv;

    @Mock
    Player mockPlayer;

    @Mock
    GainFood GainFoodMock;

    @Mock
    GainPP GainPPMock;

    @Mock
    DrawCardVisitor visitorDrawCard;


    @Test
    void testShouldGetPpValue() {
        Building building = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.DRAW_PHASE, 2, 1, 1, new ArrayList<>(), new ArrayList<>());
        assertEquals(1, building.getPpValue());
    }

    @Test
    void testShouldGetFoodCost() {
        Building building = new Building(CardTypeEnum.BUILDING, 2, GamePhaseEnum.DRAW_PHASE, 2, 1, 1, new ArrayList<>(), new ArrayList<>());
        assertEquals(1, building.getFoodCost());
    }

    @Test
    void testShouldAccept() {

        Building building = new Building(CardTypeEnum.BUILDING,67, GamePhaseEnum.DRAW_PHASE, 2, 1, 1, new ArrayList<>(), new ArrayList<>());

        building.accept(visitorGFood, mockPlayer, GainFoodMock);
        building.accept(visitorEv);

        verify(visitorGFood).visit(building, mockPlayer, GainFoodMock);

        building.accept(visitorPP, mockPlayer, GainPPMock);

        verify(visitorPP).visit(building, mockPlayer, GainPPMock);

        building.accept(visitorDrawCard);

        verify(visitorDrawCard).visit(building);
    }

    @Test
    void testShouldExecInteractiveEffect() {
        Player mockPlayer = mock(Player.class);
        DrawCard mockEffect1 = mock(DrawCard.class);
        DrawCard mockEffect2 = mock(DrawCard.class);
        Action mockAction1 = mock(Action.class);
        Action mockAction2 = mock(Action.class);

        Building sut = spy(new Building(CardTypeEnum.BUILDING, 1,GamePhaseEnum.DRAW_PHASE, 3, 5, 4, new ArrayList<>(), Arrays.asList(mockEffect1, mockEffect2)));


        when(mockEffect1.apply(mockPlayer)).thenReturn(mockAction1);
        when(mockEffect2.apply(mockPlayer)).thenReturn(mockAction2);

        List<Action> result = sut.execInteractiveEffect(mockPlayer);

        assertEquals(2, result.size(), "La lista deve contenere esattamente 2 azioni");

        assertEquals(mockAction1, result.get(0));
        assertEquals(mockAction2, result.get(1));

        verify(mockEffect1, times(1)).apply(mockPlayer);
        verify(mockEffect2, times(1)).apply(mockPlayer);
    }
}