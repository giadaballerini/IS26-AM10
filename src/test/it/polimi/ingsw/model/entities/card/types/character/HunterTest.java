package it.polimi.ingsw.model.entities.card.types.character;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import it.polimi.ingsw.visitors.VillageVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HunterTest {

    Hunter hunter = new Hunter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.HUNTER);

    @Mock Village mockVillage;
    @Mock Player mockPlayer;
    @Mock GainFood gainFoodMock;
    @Mock GainPP gainPPMock;
    @Mock VillageVisitor visitorVillage;

    @Test
    void testShouldDispatch() {
        hunter.dispatch(mockVillage);

        verify(mockVillage).add(hunter);
    }

    @Test
    void testShouldAcceptVillageVisitor() {
        hunter.accept(visitorVillage);

        verify(visitorVillage).visit(hunter);
    }
}