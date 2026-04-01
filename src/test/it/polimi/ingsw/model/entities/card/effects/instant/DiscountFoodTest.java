package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.DiscountFoodEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

@ExtendWith(MockitoExtension.class)
class DiscountFoodTest {

    @Mock
    Player mockPlayer;

    @Mock
    Builder builder;

    @Mock
    DiscountFood mockDiscountFood;

    @ParameterizedTest
    @EnumSource(DiscountFoodEnum.class)
    void testShouldApply(DiscountFoodEnum discountFoodType) {

        DiscountFood effect = new DiscountFood(CardTypeEnum.BUILDER, 67, discountFoodType);

        effect.apply(mockPlayer, builder);

        if(discountFoodType == DiscountFoodEnum.DISCOUNT_FOR_BUILDING)
            verify(mockPlayer).addTotBuildDiscount(anyInt());
        else
            verify(mockPlayer).addFoodDiscount(anyInt());
    }

    @Test
    void testShouldDisplayEffect() {
        DiscountFood effect = new DiscountFood(null, 67, DiscountFoodEnum.DISCOUNT_FLAT);

        mockDiscountFood.displayEffect();
        effect.displayEffect();

        verify(mockDiscountFood).displayEffect();
    }

    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "BUILDER", "HUNTER", "PAINTER", "CRAFTER", "SHAMAN", "GATHERER"
    })
    void testShouldGetCat(CardTypeEnum catType) {
        DiscountFood effect = new DiscountFood(catType, 67, DiscountFoodEnum.DISCOUNT_FLAT);

        assertEquals(catType, effect.getCat());
    }

    @Test
    void testShouldGetFoodAmount() {
        DiscountFood effect = new DiscountFood(null, 67, DiscountFoodEnum.DISCOUNT_FLAT);

        assertEquals(67, effect.getFoodAmount());
    }

    @ParameterizedTest
    @EnumSource(DiscountFoodEnum.class)
    void testShouldIsOneTime(DiscountFoodEnum discountFoodType) {
        DiscountFood effect = new DiscountFood(CardTypeEnum.BUILDER, 67, discountFoodType);

        if(discountFoodType != DiscountFoodEnum.DISCOUNT_CAT)
            assertTrue(effect.isOneTime());
        else
            assertFalse(effect.isOneTime());
    }
}