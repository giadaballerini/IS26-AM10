package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GainPPTest {

    @Mock
    GainPP mockEff;

    @Test
    void apply() {
    Player p = new Player("Player", ColorPawnEnum.ORANGE);
    GainPP eff = new GainPP(1, null, GainPPEnum.PP_FLAT);
    Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.BUILDER);
    eff.apply(p, builder);
    assertEquals(1, p.getPP());
    }

    @Test
    void displayEffect() {
        GainPP eff = new GainPP(1, null, GainPPEnum.PP_FLAT);

        mockEff.displayEffect();
        eff.displayEffect();

        verify(mockEff).displayEffect();
    }

    @Test
    void getPpAmount() {
        GainPP eff = new GainPP(1, null, GainPPEnum.PP_FLAT);
        assertEquals(1, eff.getPpAmount());
    }

    @Test
    void getCat() {
        GainPP eff = new GainPP(1, CardTypeEnum.BUILDER, GainPPEnum.PP_FLAT);
        assertEquals(CardTypeEnum.BUILDER, eff.getCat());
    }

    @Test
    void isOneTime() {
        GainPP eff = new GainPP(1, CardTypeEnum.BUILDER, GainPPEnum.PP_FLAT);
        assertTrue(eff.isOneTime());
        GainPP eff1 = new GainPP(1, CardTypeEnum.BUILDER, GainPPEnum.PP_FOR_CAT);
        assertFalse(eff1.isOneTime());
    }
}