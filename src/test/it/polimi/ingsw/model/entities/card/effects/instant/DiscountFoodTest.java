package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DiscountFoodEnum;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiscountFoodTest {

    @Mock
    Player mockPlayer;

    @Mock
    Builder builder;

    @ParameterizedTest
    @EnumSource(DiscountFoodEnum.class)
    void testShouldApply(DiscountFoodEnum discountFoodType) {

        DiscountFood effect = new DiscountFood(CardTypeEnum.BUILDER, 67, discountFoodType);

        effect.apply(mockPlayer, builder);

        if(discountFoodType == DiscountFoodEnum.DISCOUNT_FOR_BUILDING)
            verify(mockPlayer).addTotBuildDiscount(anyInt());
        else if(discountFoodType == DiscountFoodEnum.DISCOUNT_CAT)
            verify(mockPlayer).addCategoryDiscount(CardTypeEnum.BUILDER);
        else
            verify(mockPlayer).addFoodDiscount(anyInt());
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

        assertTrue(effect.isOneTime());
    }
}