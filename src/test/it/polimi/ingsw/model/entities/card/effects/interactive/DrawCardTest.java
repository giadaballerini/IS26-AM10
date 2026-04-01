package it.polimi.ingsw.model.entities.card.effects.interactive;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DrawCardTest {

    @Mock
    DrawCardEnum mockDraw;

    @Mock
    DrawCard mockEff;


    @Test
    void testShouldApply(){
        DrawCard eff = new DrawCard(mockDraw);
        CardEffectInteractive eff1 =  spy(CardEffectInteractive.class);
        Player realPlayer = new Player("Giorgio", ColorPawnEnum.WHITE);

        eff.apply(realPlayer);

        verify(mockDraw).apply(realPlayer, mockDraw);

        eff1.apply(realPlayer);
        eff1.displayEffect();
    }

    @Test
    void testShouldDisplayEffect() {
       DrawCard eff = new DrawCard(DrawCardEnum.UP_DRAW);

        mockEff.displayEffect();
        eff.displayEffect();

        verify(mockEff).displayEffect();
    }
}