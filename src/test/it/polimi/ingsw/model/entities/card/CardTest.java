package it.polimi.ingsw.model.entities.card;

import it.polimi.ingsw.enumerations.*;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.types.character.Builder;
import it.polimi.ingsw.model.entities.card.types.character.Hunter;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CardTest {

    @Mock
    GainPP mockEffect;

    @Mock
    GainPP mockEffect1;

    Card card = new Hunter(67, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), new ArrayList<>(), 2, false,  CardTypeEnum.HUNTER);
    Player player = new Player("Giorgio", ColorPawnEnum.PURPLE);

    @Test
    void testShouldGetAge(){
        assertEquals(2,card.getAge());
    }

    @Test
    void testShouldGetId(){
        assertEquals(67,card.getId());
    }

    @Test
    void testShouldGetType(){
        assertEquals(CardTypeEnum.HUNTER,card.getType());
    }

    @Test
    void testShouldGetInteractiveEffects(){
        assertNotNull(card.getInteractiveEffects());
        assertEquals(0,card.getInteractiveEffects().size());
    }

    @Test
    void testShouldGetInstantEffects(){
        assertNotNull(card.getInstantEffects());
        assertEquals(0,card.getInstantEffects().size());
    }

    @Test
    void testShouldExecInteractiveEffect(){
        assertNotNull(card.execInteractiveEffect(player));
        assertEquals(0,card.execInteractiveEffect(player).size());
    }

    @Test
    void testShouldExecInstantEffects_enters(){
        List<CardEffectInstant> lista = new ArrayList<>();
        DiscountFood effect = new DiscountFood(CardTypeEnum.BUILDER, 67, DiscountFoodEnum.DISCOUNT_FLAT);
        lista.add(effect);
        when(mockEffect.isOneTime()).thenReturn(false);
        when(mockEffect.canApply(GamePhaseEnum.PLAY_EVENT, GamePhaseEnum.PLAY_EVENT)).thenReturn(true);
        lista.add(mockEffect);

        Card card1 = new Builder(67, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), lista, 2,   CardTypeEnum.HUNTER);

        card1.execInstantEffect(player, GamePhaseEnum.PLAY_EVENT);
        verify(mockEffect).apply(player, card1);

    }

    @Test
    void testShouldExecInstantEffects_leaves(){

        List<CardEffectInstant> lista = new ArrayList<>();
        DiscountFood effect = new DiscountFood(CardTypeEnum.BUILDER, 67, DiscountFoodEnum.DISCOUNT_FLAT);
        lista.add(effect);
        lista.add(mockEffect1);

        Card card1 = new Builder(67, GamePhaseEnum.PLAY_EVENT, new ArrayList<>(), lista, 2,   CardTypeEnum.HUNTER);

        when(mockEffect1.canApply(GamePhaseEnum.PLAY_EVENT, GamePhaseEnum.DRAW_PHASE)).thenReturn(false);
        card1.execInstantEffect(player, GamePhaseEnum.DRAW_PHASE);
        verify(mockEffect1, never()).apply(player, card1);
    }


}