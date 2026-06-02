package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GainPPEnumTest {

    @Mock GainPP effect;
    @Mock private Player player;
    @Mock private Card card;

    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "BUILDER", "GATHERER", "CRAFTER", "HUNTER", "SHAMAN", "PAINTER"
    })
    void testShouldApply_PPForCat(CardTypeEnum cardType) {
        when(effect.getCat()).thenReturn(cardType);
        when(player.getNumType(cardType)).thenReturn(6);
        when(effect.getPpAmount()).thenReturn(7);

        GainPPEnum.PP_FOR_CAT.apply(player, effect, card);

        verify(player).addPP(42);
    }

    @Test
    void testShouldApply_PPForSet_Increment() {
        for (CardTypeEnum type : CardTypeEnum.values()) {
            if (type.isCharacter()) when(player.getNumType(type)).thenReturn(1);
        }
        when(card.getType()).thenReturn(CardTypeEnum.BUILDER);
        when(effect.getPpAmount()).thenReturn(5);

        GainPPEnum.PP_FOR_SET.apply(player, effect, card);

        verify(player).addPP(5);
        assertFalse(GainPPEnum.PP_FOR_SET.isOneTime());
    }

    @Test
    void testShouldApply_PPForSet_NotIncrement() {
        when(card.getType()).thenReturn(CardTypeEnum.BUILDER);
        for (CardTypeEnum type : CardTypeEnum.values()) {
            if (type.isCharacter())
                when(player.getNumType(type)).thenReturn(card.getType().equals(type) ? 1 : 0);
        }

        GainPPEnum.PP_FOR_SET.apply(player, effect, card);

        verify(player, never()).addPP(anyInt());
    }

    @Test
    void testShouldApply_PPFlat() {
        when(effect.getPpAmount()).thenReturn(67);

        GainPPEnum.PP_FLAT.apply(player, effect, card);

        verify(player).addPP(67);
        assertTrue(GainPPEnum.PP_FLAT.isOneTime());
    }

    // --- DOUBLE_PP_SHAMAN: ora attiva doubleShaman tramite activateDoubleShaman() ---

    @Test
    void testShouldActivateDoubleShaman() {
        Player real = new Player("Player1", ColorPawnEnum.ORANGE);

        GainPPEnum.DOUBLE_PP_SHAMAN.apply(real, effect, card);

        // verifica tramite comportamento: applyRitualGain deve raddoppiare
        real.applyRitualGain(4);
        assertEquals(8, real.getPP());
    }

    @Test
    void testShouldAddDoubleBuilder() {
        when(player.getBuilderPoints()).thenReturn(67);

        GainPPEnum.DOUBLE_BUILDER.apply(player, effect, card);

        verify(player).addPP(67);
        assertFalse(GainPPEnum.DOUBLE_BUILDER.isOneTime());
    }

    @Test
    void isOneTime() {
        when(effect.getPpAmount()).thenReturn(100);

        GainPPEnum.PP_FLAT.apply(player, effect, card);

        verify(player).addPP(100);
        assertTrue(GainPPEnum.PP_FLAT.isOneTime());
        assertFalse(GainPPEnum.DOUBLE_BUILDER.isOneTime());
    }
}
