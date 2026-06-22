package it.polimi.ingsw.model.entities.card.types.character;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Village;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import it.polimi.ingsw.visitors.VillageVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BuilderTest {

    @Mock Player mockPlayer;
    @Mock GainFood gainFoodMock;
    @Mock GainPP gainPPMock;
    @Mock Village mockVillage;
    @Mock VillageVisitor visitorVillage;
    @Mock DrawCardVisitor visitorDrawCard;
    @Mock PlayEventVisitor visitorEv;
    @Mock CanDrawVisitor visitorCanDraw;

    Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);

    @Test
    void testShouldGetPpAmount() {
        assertEquals(2, builder.getPpAmount());
    }

    @Test
    void testShouldDispatch() {
        builder.dispatch(mockVillage);

        verify(mockVillage).add(builder);
    }

    @Test
    void testShouldAcceptVillageVisitor() {
        builder.accept(visitorVillage);

        verify(visitorVillage).visit(builder);
    }
}