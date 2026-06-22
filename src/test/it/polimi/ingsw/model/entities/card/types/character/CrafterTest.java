package it.polimi.ingsw.model.entities.card.types.character;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrafterTest {

    Crafter crafter = new Crafter(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CrafterSymbolEnum.BREAD, CardTypeEnum.CRAFTER);

    @Mock Village mockVillage;
    @Mock VillageVisitor visitorVillage;

    @Test
    void testShouldGetSymbol() {
        assertEquals(CrafterSymbolEnum.BREAD, crafter.getSymbol());
    }

    @Test
    void testShouldDispatch() {
        crafter.dispatch(mockVillage);

        verify(mockVillage).add(crafter);
    }


    @Test
    void testShouldAcceptVillageVisitor() {
        crafter.accept(visitorVillage);

        verify(visitorVillage).visit(crafter);
    }
}