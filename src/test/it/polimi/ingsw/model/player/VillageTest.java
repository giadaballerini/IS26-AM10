package it.polimi.ingsw.model.player;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.types.character.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class VillageTest {

    Village v = new Village();
    @Test
    void testShouldAddCrafter() {
        Crafter c = new Crafter(123, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        assertNotNull(c);
        v.add(c);
        assertEquals(1, v.getNumType(CardTypeEnum.CRAFTER));
        assertEquals(1, v.getNumSymbolsForCrafter(CrafterSymbolEnum.AMIGDALA));
        assertEquals(1, v.getNumCharacters());
    }

    @Test
    void testShouldAddBuilder() {
        Builder builder = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,1,  CardTypeEnum.BUILDER);
        assertNotNull(builder);
        v.add(builder);
        assertEquals(1, v.getNumType(CardTypeEnum.BUILDER));

    }

    @Test
    void testShouldAddPainter() {
        Painter painter = new Painter(4,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        v.add(painter);
        assertNotNull(painter);
        assertEquals(1, v.getNumType(CardTypeEnum.PAINTER));
    }

    @Test
    void testShouldAddHunter() {
        Hunter hunter = new Hunter(4, GamePhaseEnum.DRAW_PHASE,new ArrayList<>(), new ArrayList<>(),2,true,CardTypeEnum.HUNTER);
        v.add(hunter);
        assertNotNull(hunter);
        assertEquals(1,v.getNumType(CardTypeEnum.HUNTER));
    }

    @Test
    void testShouldAddGatherer() {
        Gatherer gatherer = new Gatherer(14, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(),1, CardTypeEnum.GATHERER);
        v.add(gatherer);
        assertNotNull(gatherer);
        assertEquals(1, v.getNumType(CardTypeEnum.GATHERER));
    }

    @Test
    void testShouldAddShaman() {
        Shaman shaman = new Shaman(21, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.SHAMAN);
        v.add(shaman);
        assertNotNull(shaman);
        assertEquals(1, v.getNumType(CardTypeEnum.SHAMAN));
    }

    @Test
    void testShouldGetNumCharacters() {

        Shaman shaman = new Shaman(21, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.SHAMAN);
        Builder builder = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,1,  CardTypeEnum.BUILDER);
        assertNotNull(builder);
        Hunter hunter = new Hunter(4, GamePhaseEnum.DRAW_PHASE,new ArrayList<>(), new ArrayList<>(),2,true,CardTypeEnum.HUNTER);
        assertNotNull(hunter);
        Painter painter = new Painter(4,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        assertNotNull(painter);
        v.add(shaman);
        v.add(builder);
        v.add(hunter);
        v.add(painter);
        assertEquals(4, v.getNumCharacters());
    }

    @Test
    void testShouldGetNumType() {
        Builder builder = new Builder(2, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,1,  CardTypeEnum.BUILDER);
        assertNotNull(builder);
        Hunter hunter = new Hunter(4, GamePhaseEnum.DRAW_PHASE,new ArrayList<>(), new ArrayList<>(),2,true,CardTypeEnum.HUNTER);
        assertNotNull(hunter);
        Painter painter = new Painter(4,null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        assertNotNull(painter);
        Shaman shaman = new Shaman(21, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.SHAMAN);
        assertNotNull(shaman);
        v.add(shaman);
        v.add(builder);
        v.add(hunter);
        v.add(painter);
        assertEquals(1, v.getNumType(CardTypeEnum.SHAMAN));
        assertEquals(1, v.getNumType(CardTypeEnum.BUILDER));
        assertEquals(1, v.getNumType(CardTypeEnum.HUNTER));
        assertEquals(1, v.getNumType(CardTypeEnum.PAINTER));
        Builder builder1 = new Builder(3, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2,1,  CardTypeEnum.BUILDER);
        assertNotNull(builder1);
        v.add(builder1);
        assertEquals(2, v.getNumType(CardTypeEnum.BUILDER));
        assertEquals(-1, v.getNumType(CardTypeEnum.BUILDING));
    }

    @Test
    void testShouldGetNumSymbolsForCrafter() {
        Crafter crafter = new Crafter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        assertNotNull(crafter);
        v.add(crafter);
        Crafter crafter1 = new Crafter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.ARROWHEAD, CardTypeEnum.CRAFTER);
        assertNotNull(crafter1);
        v.add(crafter1);
        Crafter crafter2 = new Crafter(3, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.AMIGDALA, CardTypeEnum.CRAFTER);
        assertNotNull(crafter2);
        v.add(crafter2);
        assertEquals (2, v.getNumSymbolsForCrafter(CrafterSymbolEnum.AMIGDALA));

    }

    @Mock
    GainPP effect;

    @Test
    void testShouldBuilderPoints() {
        Village v2 = new Village();
        when(effect.getPpAmount()).thenReturn(6);

        for (int i = 0; i < 6; i++){
            List<CardEffectInstant> effectsInst = new ArrayList<>();
            effectsInst.add(effect);
            Builder builder = new Builder(1, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), effectsInst, 1,1,  CardTypeEnum.BUILDER);
            assertNotNull(builder);
            v2.add(builder);
        }

        assertEquals(36, v2.builderPoints());
    }
}