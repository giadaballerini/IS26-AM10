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
class GathererTest {

    Gatherer gatherer = new Gatherer(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.GATHERER);

    @Mock Village mockVillage;
    @Mock Player mockPlayer;
    @Mock GainFood gainFoodMock;
    @Mock GainPP gainPPMock;
    @Mock VillageVisitor visitorVillage;

    @Test
    void testShouldDispatch() {
        gatherer.dispatch(mockVillage);

        verify(mockVillage).add(gatherer);
    }

    @Test
    void testShouldAcceptVillageVisitor() {
        gatherer.accept(visitorVillage);

        verify(visitorVillage).visit(gatherer);
    }
}