package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GainFoodEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GainFoodTest {

    @Mock
    GainFood mockEff;

    @Test
    void apply() {
        Player p = new Player("Player1", ColorPawnEnum.ORANGE);
        CardEffectInstant eff = new GainFood(2, GainFoodEnum.FOOD_FLAT);
        Card c = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1,2, CardTypeEnum.BUILDER);
        eff.apply(p, c);
        assertEquals(2, p.getNFood());
    }


    @Test
    void getFoodAmount() {
        GainFood eff = new GainFood(1, GainFoodEnum.FOOD_FLAT);
        assertEquals(1, eff.getFoodAmount());
    }

    @Test
    void isOneTime() {
        GainFood eff = new GainFood(1, GainFoodEnum.FOOD_FLAT);
        assertTrue(eff.isOneTime());
    }
}