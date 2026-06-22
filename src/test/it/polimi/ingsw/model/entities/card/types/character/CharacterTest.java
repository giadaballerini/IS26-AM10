package it.polimi.ingsw.model.entities.card.types.character;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CharacterTest {

    Character character = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);

    @Mock DrawCardVisitor visitorDrawCard;
    @Mock PlayEventVisitor visitorEv;
    @Mock Player mockPlayer;

    @Test
    void testShouldAcceptDrawCardVisitor() {
        character.accept(visitorDrawCard);

        verify(visitorDrawCard).visit(character);
    }

    @Test
    void testAcceptPlayEventVisitorIsNoOp() {
        character.accept(visitorEv);

        verifyNoInteractions(visitorEv);
    }

    @Test
    void testAcceptCanDrawVisitor_setsMustDraw() {
        CanDrawVisitor visitor = new CanDrawVisitor(mockPlayer);
        character.accept(visitor);

        assertTrue(visitor.getMustDraw());
        assertFalse(visitor.getMayDraw());
    }
}