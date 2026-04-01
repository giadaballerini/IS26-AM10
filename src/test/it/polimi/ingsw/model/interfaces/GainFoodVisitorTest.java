package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class GainFoodVisitorTest {

    @Mock
    GainFoodVisitor mockGainFoodVisitor;

    @Mock
    GainFood mockGainFood;

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

        GainFoodVisitor mockGainFoodVisitor = spy(GainFoodVisitor.class);

        mockGainFoodVisitor.visit(mockCrafter, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(mockBuilder, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(painter, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(hunter, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(gatherer, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(shaman, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(building, mockPlayer, mockGainFood);
        mockGainFoodVisitor.visit(event, mockPlayer, mockGainFood);
    }
}