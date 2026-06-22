package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CardVisitorTest {

    @Mock Event mockEvent;
    @Mock Building mockBuilding;
    @Mock Character mockCharacter;

    private CardVisitor visitor;

    @BeforeEach
    void setUp() {
        visitor = mock(CardVisitor.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void testVisitEventDefaultIsNoop() {
        assertDoesNotThrow(() -> visitor.visit(mockEvent));
    }

    @Test
    void testVisitBuildingDefaultIsNoop() {
        assertDoesNotThrow(() -> visitor.visit(mockBuilding));
    }

    @Test
    void testVisitCharacterDefaultIsNoop() {
        assertDoesNotThrow(() -> visitor.visit(mockCharacter));
    }
}