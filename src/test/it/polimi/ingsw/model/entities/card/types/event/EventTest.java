package it.polimi.ingsw.model.entities.card.types.event;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventTest {

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
    void testShouldAccept() {
        Event event = new Hunt(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 67, 78, CardTypeEnum.BUILDING);

        event.accept(visitorGFood, mockPlayer, GainFoodMock);
        event.accept(visitorEv);
        verify(visitorEv).visit(event);

        verify(visitorGFood).visit(event, mockPlayer, GainFoodMock);

        event.accept(visitorPP, mockPlayer, GainPPMock);

        verify(visitorPP).visit(event, mockPlayer, GainPPMock);

        event.accept(visitorDrawCard);

        verify(visitorDrawCard).visit(event);
    }

}