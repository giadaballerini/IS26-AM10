package it.polimi.ingsw.model.entities.card.types.character;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.interfaces.GainPPVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HunterTest {

    Hunter hunter = new Hunter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, true, CardTypeEnum.BUILDER);

    @Mock
    Player mockPlayer;

    @Mock
    GainFood GainFoodMock;

    @Mock
    GainPP GainPPMock;

    @Mock
    Village mockVillage;

    @Mock
    GainFoodVisitor visitorGFood;

    @Mock
    GainPPVisitor visitorPP;


    @Test
    void testShouldDispatch() {

        hunter.dispatch(mockVillage);

        verify(mockVillage).add(hunter);
    }

    @Test
    void testShouldAccept() {
        hunter.accept(visitorGFood, mockPlayer, GainFoodMock);
        hunter.accept(visitorPP, mockPlayer, GainPPMock);

        verify(visitorGFood).visit(hunter, mockPlayer, GainFoodMock);
        verify(visitorPP).visit(hunter, mockPlayer, GainPPMock);
    }
}