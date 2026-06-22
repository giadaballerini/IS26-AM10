package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DrawCardEnumTest {

    @Mock
    Player mockPlayer;

    @ParameterizedTest
    @EnumSource(DrawCardEnum.class)
    void apply_returnsNonNullAction(DrawCardEnum drawCardType) {
        Action result = drawCardType.apply(mockPlayer, drawCardType);
        assertNotNull(result);
        verifyNoInteractions(mockPlayer);
    }
}