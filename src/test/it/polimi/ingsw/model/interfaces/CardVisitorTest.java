package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class CardVisitorTest {


    @Mock
    Event mockEvent;

    @Mock
    Building mockBuilding;

    @Mock
    Builder mockCharacter;

    @Test
    void testShouldVisit() {

        CardVisitor mockCardVisitor = spy(CardVisitor.class);

        mockCardVisitor.visit(mockEvent);
        mockCardVisitor.visit(mockCharacter);
        mockCardVisitor.visit(mockBuilding);
    }
}