package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.entities.card.effects.interactive.DrawCard;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

class DrawCardEnumTest {

    @ExtendWith(MockitoExtension.class)

    @Mock
    Player mockPlayer;

    @Mock
    DrawCard mockDrawCard;

    @ParameterizedTest
    @EnumSource(value = DrawCardEnum.class)
    void testShouldApply(DrawCardEnum drawCardType) {
        Action result = drawCardType.apply(mockPlayer, drawCardType);
        assertNotNull(result);
        verifyNoInteractions(mockPlayer);
    }
}