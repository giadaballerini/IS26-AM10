package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountFoodEnumTest {

    @Mock
    Player mockPlayer;

    @Mock
    DiscountFood mockEffect;


    @ParameterizedTest
    @EnumSource(DiscountFoodEnum.class)
    void testIsOneTime_allTrue(DiscountFoodEnum type) {
        assertTrue(type.isOneTime());
    }


    @Test
    void apply_flat_addsFoodDiscount() {
        when(mockEffect.getFoodAmount()).thenReturn(10);
        DiscountFoodEnum.DISCOUNT_FLAT.apply(mockPlayer, mockEffect);
        verify(mockPlayer).addFoodDiscount(10);
    }


    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "GATHERER", "HUNTER", "PAINTER", "BUILDER", "SHAMAN", "CRAFTER"
    })
    void apply_discountCat_delegatesToAddCategoryDiscount(CardTypeEnum cardType) {
        when(mockEffect.getCat()).thenReturn(cardType);
        DiscountFoodEnum.DISCOUNT_CAT.apply(mockPlayer, mockEffect);
        verify(mockPlayer).addCategoryDiscount(cardType);
    }


    @Test
    void apply_discountForBuilding_addsTotBuildDiscount() {
        when(mockEffect.getFoodAmount()).thenReturn(67);
        DiscountFoodEnum.DISCOUNT_FOR_BUILDING.apply(mockPlayer, mockEffect);
        verify(mockPlayer).addTotBuildDiscount(67);
    }
}