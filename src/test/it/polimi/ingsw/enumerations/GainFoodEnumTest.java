package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GainFoodEnumTest {

    @Mock
    private Player player;
    @Mock
    private GainFood effect;
    @Mock
    private Card card;
    @Mock
    private Crafter crafter;

    @Test
    void testFoodForSet_FoodIncrement() {

        for (CardTypeEnum type : CardTypeEnum.values()) {
            if(type.isCharacter())
                when(player.getNumType(type)).thenReturn(1);
        }
        when(card.getType()).thenReturn(CardTypeEnum.BUILDER);
        when(effect.getFoodAmount()).thenReturn(5);

        GainFoodEnum.FOOD_FOR_SET.apply(player, effect, card);

        verify(player).addFood(5);
        assertFalse(GainFoodEnum.FOOD_FOR_SET.isOneTime());
    }


    @Test
    void testFoodForSet_FoodNotIncremented() {

        for (CardTypeEnum type : CardTypeEnum.values()) {
            if(type.isCharacter())
                if(type != CardTypeEnum.BUILDER)
                    when(player.getNumType(type)).thenReturn(0);
                else
                    when(player.getNumType(type)).thenReturn(1);
        }

        when(card.getType()).thenReturn(CardTypeEnum.BUILDER);

        GainFoodEnum.FOOD_FOR_SET.apply(player, effect, card);

        verify(player, never()).addFood(5);
    }


    @Test
    void testFoodForCrafter_Incremented() {

        when(crafter.getSymbol()).thenReturn(CrafterSymbolEnum.BREAD);
        when(player.getNumSymbolsForCrafter(CrafterSymbolEnum.BREAD)).thenReturn(2);
        when(effect.getFoodAmount()).thenReturn(3);

        GainFoodEnum.FOOD_FOR_CRAFTER.visit(crafter, player, effect);

        verify(player).addFood(3);
    }

    @Mock
    Player player1;

    @Test
    void testFoodForCrafter_NotIncremented_Odd() {

        when(crafter.getSymbol()).thenReturn(CrafterSymbolEnum.BREAD);
        when(player1.getNumSymbolsForCrafter(CrafterSymbolEnum.BREAD)).thenReturn(3);

        GainFoodEnum.FOOD_FOR_CRAFTER.visit(crafter, player1, effect);

        verify(player1, never()).addFood(anyInt());
    }

    @Test
    void testFoodForCrafter_NotIncremented_Zero() {

        when(crafter.getSymbol()).thenReturn(CrafterSymbolEnum.BREAD);
        when(player1.getNumSymbolsForCrafter(CrafterSymbolEnum.BREAD)).thenReturn(0);

        GainFoodEnum.FOOD_FOR_CRAFTER.visit(crafter, player1, effect);

        verify(player1, never()).addFood(anyInt());
    }

    @Test
    void testFoodForCrafter_ApplyDelegation() {
        GainFoodEnum.FOOD_FOR_CRAFTER.apply(player, effect, crafter);
        verify(crafter).accept(GainFoodEnum.FOOD_FOR_CRAFTER, player, effect);
        assertFalse(GainFoodEnum.FOOD_FOR_CRAFTER.isOneTime());
    }

    @Test
    void testFoodForHunter() {
        Player realPlayer = new Player("Mockito", ColorPawnEnum.BLUE);
        GainFoodEnum.FOOD_FOR_HUNTER_HUNT.apply(realPlayer,effect,card);
        assertTrue(realPlayer.hasHuntFlag());
        assertTrue(GainFoodEnum.FOOD_FOR_HUNTER_HUNT.isOneTime());
    }

    @Test
    void testFoodForArtist() {
        when(player.getNumType(CardTypeEnum.PAINTER)).thenReturn(2);
        when(effect.getFoodAmount()).thenReturn(4);

        GainFoodEnum.FOOD_FOR_ARTIST_PAINT.apply(player, effect, card);

        verify(player).addFood(8);
    }

    @Test
    void testFoodFlatAndIsOneTime() {
        when(effect.getFoodAmount()).thenReturn(100);

        GainFoodEnum.FOOD_FLAT.apply(player, effect, card);

        verify(player).addFood(100);
        assertTrue(GainFoodEnum.FOOD_FLAT.isOneTime());
        assertFalse(GainFoodEnum.FOOD_FOR_SET.isOneTime());
    }

    @Test
    void testEnumInfrastructure() {
        assertNotNull(GainFoodEnum.valueOf("FOOD_FLAT"));
        assertEquals(5, GainFoodEnum.values().length);
    }
}