package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GainPPEnumTest {

    @Mock GainPP effect;
    @Mock Card card;


    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "BUILDER", "GATHERER", "CRAFTER", "HUNTER", "SHAMAN", "PAINTER"
    })
    void testPPForCat_multipliesAmountByCount(CardTypeEnum cardType) {
        Player player = mock(Player.class);
        when(effect.getCat()).thenReturn(cardType);
        when(player.getNumType(cardType)).thenReturn(6);
        when(effect.getPpAmount()).thenReturn(7);

        GainPPEnum.PP_FOR_CAT.apply(player, effect, card);

        verify(player).addPP(42);
        assertFalse(GainPPEnum.PP_FOR_CAT.isOneTime());
    }


    @Test
    void testPPForSet_completesNewSet_addsPP() {
        Player real = new Player("Test", ColorPawnEnum.BLUE);
        when(effect.getPpAmount()).thenReturn(5);

        real.addCard(new Gatherer(1, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.GATHERER));
        real.addCard(new Hunter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER));
        real.addCard(new Painter(3, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER));
        real.addCard(new Shaman(4, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.SHAMAN));
        real.addCard(new Crafter(5, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BOWL, CardTypeEnum.CRAFTER));
        Builder trigger = new Builder(6, null, new ArrayList<>(), new ArrayList<>(), 1, 1, CardTypeEnum.BUILDER);
        real.addCard(trigger);

        when(card.getType()).thenReturn(CardTypeEnum.BUILDER);
        GainPPEnum.PP_FOR_SET.apply(real, effect, card);

        assertEquals(5, real.getPP());
        assertFalse(GainPPEnum.PP_FOR_SET.isOneTime());
    }

    @Test
    void testPPForSet_doesNotCompleteSet_noPP() {
        Player real = new Player("Test", ColorPawnEnum.BLUE);
        Builder trigger = new Builder(1, null, new ArrayList<>(), new ArrayList<>(), 1, 1, CardTypeEnum.BUILDER);
        real.addCard(trigger);

        when(card.getType()).thenReturn(CardTypeEnum.BUILDER);
        GainPPEnum.PP_FOR_SET.apply(real, effect, card);

        assertEquals(0, real.getPP());
        verify(effect, never()).getPpAmount();
    }


    @Test
    void testPPFlat_addsCorrectAmount() {
        Player player = mock(Player.class);
        when(effect.getPpAmount()).thenReturn(67);

        GainPPEnum.PP_FLAT.apply(player, effect, card);

        verify(player).addPP(67);
        assertTrue(GainPPEnum.PP_FLAT.isOneTime());
    }


    @Test
    void testDoublePPShaman_activatesDoubleShaman() {
        Player real = new Player("Test", ColorPawnEnum.BLUE);

        GainPPEnum.DOUBLE_PP_SHAMAN.apply(real, effect, card);
        real.applyRitualGain(4);

        assertEquals(8, real.getPP());
        assertFalse(GainPPEnum.DOUBLE_PP_SHAMAN.isOneTime());
    }


    @Test
    void testDoubleBuilder_addsPPFromBuilderPoints() {
        Player player = mock(Player.class);
        when(player.getBuilderPoints()).thenReturn(67);

        GainPPEnum.DOUBLE_BUILDER.apply(player, effect, card);

        verify(player).addPP(67);
        assertFalse(GainPPEnum.DOUBLE_BUILDER.isOneTime());
    }


    @Test
    void testIsOneTime_onlyPPFlatIsTrue() {
        assertTrue(GainPPEnum.PP_FLAT.isOneTime());
        assertFalse(GainPPEnum.PP_FOR_CAT.isOneTime());
        assertFalse(GainPPEnum.PP_FOR_SET.isOneTime());
        assertFalse(GainPPEnum.DOUBLE_PP_SHAMAN.isOneTime());
        assertFalse(GainPPEnum.DOUBLE_BUILDER.isOneTime());
    }
}