package it.polimi.ingsw.model.entities.card.effects.instant;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.enumerations.GainStarsEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.character.Shaman;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class GainStarsTest {

    @Test
    void shouldApply() {
        GainStars eff = new GainStars(1, GainStarsEnum.GAIN_STARS);
        Player p = new Player("Player", ColorPawnEnum.ORANGE);
        List<CardEffectInstant> effects = new ArrayList<>();
        effects.add(eff);
        Shaman s = new Shaman(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), effects, 1, CardTypeEnum.SHAMAN);
        s.execInstantEffect(p, GamePhaseEnum.DRAW_PHASE);
        assertEquals(1, p.getNStars());
    }


    @Test
    void getStarsAmount() {
        GainStars eff = new GainStars(1, GainStarsEnum.GAIN_STARS);
        assertEquals(1, eff.getStarsAmount());
    }

    @Test
    void isOneTime() {
        GainStars eff = new GainStars(1, GainStarsEnum.GAIN_STARS);
        assertTrue(eff.isOneTime());
    }
}