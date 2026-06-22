package it.polimi.ingsw.visitors;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.entities.card.types.event.Feast;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PlayEventVisitorTest {
    public class TestablePlayEventVisitor extends PlayEventVisitor{
        public TestablePlayEventVisitor(List<Player> players, GamePhaseEnum currPhase, EventDTO eve) {
            super(players, currPhase, eve);
        }
        public void setEvent(Event e){this.feastEvent = e;}
    }
    private TestablePlayEventVisitor pev;
    @BeforeEach
    void setUp() {
        pev = new TestablePlayEventVisitor(null, null, new EventDTO());
    }
    @Test
    void visitValidityEvent() {
        Feast mockFeast = mock(Feast.class);
        when(mockFeast.getType()).thenReturn(CardTypeEnum.FEAST);
        pev.visit(mockFeast);
        assertEquals(mockFeast, pev.feastEvent);
    }
    @Test
    void visitInvalidityEvent() {

    }
    @Test
    void feastIfPresentValid() {
        Feast mockFeast = mock(Feast.class);
        pev.setEvent(mockFeast);
        pev.feastIfPresent();
        verify(mockFeast, times(1)).execEvent(any(), any());
    }
    @Test
    void feastIfPresentInvalid() {

    }
}