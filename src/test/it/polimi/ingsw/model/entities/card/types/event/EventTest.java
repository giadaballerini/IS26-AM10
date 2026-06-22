package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventTest {

    Event event = new Hunt(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 67, 78, CardTypeEnum.HUNT);

    @Mock PlayEventVisitor visitorEv;
    @Mock DrawCardVisitor visitorDrawCard;
    @Mock Player mockPlayer;
    @Mock GainFood gainFoodMock;
    @Mock GainPP gainPPMock;
    @Mock VillageVisitor visitorVillage;

    @Test
    void testShouldAcceptVillageVisitor() {
        event.accept(visitorVillage);

        verify(visitorVillage).visit(event);
    }

    @Test
    void testShouldAcceptPlayEventVisitor() {
        event.accept(visitorEv);

        verify(visitorEv).visit(event);
    }

    @Test
    void testShouldAcceptDrawCardVisitor() {
        event.accept(visitorDrawCard);

        verify(visitorDrawCard).visit(event);
    }

    @Test
    void testAcceptCanDrawVisitor_doesNotSetMustDrawNorMayDraw() {
        CanDrawVisitor visitor = new CanDrawVisitor(mockPlayer);
        event.accept(visitor);

        assertFalse(visitor.getMustDraw());
        assertFalse(visitor.getMayDraw());
    }
}