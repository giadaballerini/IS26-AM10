package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;


@ExtendWith(MockitoExtension.class)
class GainPPVisitorTest {


    @Mock
    GainPP mockGainPP;

    @Mock
    Player mockPlayer;

    @Mock
    Crafter mockCrafter;

    @Mock
    Builder mockBuilder;

    @Mock
    Painter painter;

    @Mock
    Hunter hunter;

    @Mock
    Gatherer gatherer;

    @Mock
    Shaman shaman;

    @Mock
    Building building;

    @Mock
    Event event;


    @Test
    void testShouldVisit() {

        GainPPVisitor mockGainFoodVisitor = spy(GainPPVisitor.class);

        mockGainFoodVisitor.visit(mockCrafter, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(mockBuilder, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(painter, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(hunter, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(gatherer, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(shaman, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(building, mockPlayer, mockGainPP);
        mockGainFoodVisitor.visit(event, mockPlayer, mockGainPP);
    }
}