package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscountFoodEnumTest {

    @ParameterizedTest
    @EnumSource(value =  DiscountFoodEnum.class, names = {"DISCOUNT_FLAT"})
    void testShouldIsOneTime_forFlat(DiscountFoodEnum cardType){
        assertTrue(cardType.isOneTime());
    }

    @ParameterizedTest
    @EnumSource(value =  DiscountFoodEnum.class, names = {"DISCOUNT_CAT"})
    void testShouldIsOneTime_forCat(DiscountFoodEnum cardType){
        assertFalse(cardType.isOneTime());
    }

    @ExtendWith(MockitoExtension.class)

    @Mock
    Player mockPlayer;

    @Mock
    DiscountFood mockEffect;

    @Test
    void apply_flat() {

        DiscountFoodEnum discountType = DiscountFoodEnum.DISCOUNT_FLAT;

        when(mockEffect.getFoodAmount()).thenReturn(10);

        discountType.apply(mockPlayer, mockEffect);

        verify(mockPlayer).addFoodDiscount(10);
    }

    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "GATHERER", "HUNTER", "PAINTER", "BUILDER", "SHAMAN", "CRAFTER"
    })
    void testDiscountCat_EvenCase(CardTypeEnum cardType) {

        when(mockEffect.getCat()).thenReturn(cardType);
        when(mockPlayer.getNumType(cardType)).thenReturn(2);
        when(mockEffect.getFoodAmount()).thenReturn(3);

        DiscountFoodEnum.DISCOUNT_CAT.apply(mockPlayer, mockEffect);

        verify(mockPlayer).addFoodDiscount(3);
        assertFalse(DiscountFoodEnum.DISCOUNT_CAT.isOneTime());
    }


    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "GATHERER", "HUNTER", "PAINTER", "BUILDER", "SHAMAN", "CRAFTER"
    })
    void testDiscountCat_OddCase(CardTypeEnum cardType) {

        when(mockEffect.getCat()).thenReturn(cardType);
        when(mockPlayer.getNumType(cardType)).thenReturn(3);

        DiscountFoodEnum.DISCOUNT_CAT.apply(mockPlayer, mockEffect);

        verify(mockPlayer).addFoodDiscount(0);
    }

    @Test
    void testShouldDiscountForBuilding(){
        DiscountFoodEnum discountType = DiscountFoodEnum.DISCOUNT_FOR_BUILDING;

        when(mockEffect.getFoodAmount()).thenReturn(67);

        discountType.apply(mockPlayer, mockEffect);

        verify(mockPlayer).addTotBuildDiscount(67);

    }

}