package it.polimi.ingsw.model.entities.card.effects.interactive;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DrawCardTest {

    @Mock
    DrawCardEnum mockDraw;


    @Test
    void testShouldApply(){
        DrawCard eff = new DrawCard(mockDraw);
        CardEffectInteractive eff1 =  mock(CardEffectInteractive.class);
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.WHITE);

        eff.apply(realPlayer);

        verify(mockDraw).apply(realPlayer, mockDraw);

        eff1.apply(realPlayer);
    }

}