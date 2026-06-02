package it.polimi.ingsw.model.entities.card.types.character;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterTest {
    Character character  = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, 2, CardTypeEnum.BUILDER);

    @Mock
    DrawCardVisitor visitorDrawCard;

    @Mock
    PlayEventVisitor visitorEv;

    @Test
    void testShouldAccept() {
        character.accept(visitorEv);
        character.accept(visitorDrawCard);
        verify(visitorEv, never()).visit(character);
        verify(visitorDrawCard).visit(character);
    }
}