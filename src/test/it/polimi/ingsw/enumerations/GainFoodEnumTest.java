package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GainFoodEnumTest {

    private Player player;
    private GainFood effect;

    @BeforeEach
    void setUp() {
        player = new Player("Test", ColorPawnEnum.BLUE);
        effect  = mock(GainFood.class);
    }


    @Test
    @DisplayName("FOOD_FOR_SET — aggiunge cibo quando viene completato un nuovo set")
    void testFoodForSet_completesNewSet() {
        when(effect.getFoodAmount()).thenReturn(5);

        player.addCard(new Gatherer(1, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.GATHERER));
        player.addCard(new Hunter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER));
        player.addCard(new Painter(3, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER));
        player.addCard(new Shaman(4, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.SHAMAN));
        player.addCard(new Crafter(5, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BOWL, CardTypeEnum.CRAFTER));

        Builder trigger = new Builder(6, null, new ArrayList<>(), new ArrayList<>(), 1, 1, CardTypeEnum.BUILDER);
        player.addCard(trigger);

        GainFoodEnum.FOOD_FOR_SET.apply(player, effect, trigger);

        assertEquals(5, player.getNFood(), "deve ricevere 5 cibo completando il primo set");
    }

    @Test
    @DisplayName("FOOD_FOR_SET — non aggiunge cibo se il set non è completato")
    void testFoodForSet_doesNotCompleteSet() {
        Builder trigger = new Builder(1, null, new ArrayList<>(), new ArrayList<>(), 1, 1, CardTypeEnum.BUILDER);
        player.addCard(trigger);

        GainFoodEnum.FOOD_FOR_SET.apply(player, effect, trigger);

        assertEquals(0, player.getNFood(), "non deve ricevere cibo senza un set completo");
        verify(effect, never()).getFoodAmount();
    }

    @Test
    @DisplayName("FOOD_FOR_SET — isOneTime restituisce false")
    void testFoodForSet_isNotOneTime() {
        assertFalse(GainFoodEnum.FOOD_FOR_SET.isOneTime());
    }

    @Test
    @DisplayName("FOOD_FOR_SET — apply(p, effect) a due argomenti è no-op (non implementato)")
    void testFoodForSet_twoArgApply_isNoOp() {
        GainFoodEnum.FOOD_FOR_SET.apply(player, effect);

        assertEquals(0, player.getNFood(), "il default a due argomenti non deve modificare il cibo");
        verify(effect, never()).getFoodAmount();
    }


    @Test
    @DisplayName("FOOD_FOR_CRAFTER — aggiunge cibo al secondo crafter con lo stesso simbolo")
    void testFoodForCrafter_evenCount() {
        when(effect.getFoodAmount()).thenReturn(3);

        Crafter c1 = new Crafter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BREAD, CardTypeEnum.CRAFTER);
        Crafter c2 = new Crafter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BREAD, CardTypeEnum.CRAFTER);
        player.addCard(c1);
        player.addCard(c2);

        GainFoodEnum.FOOD_FOR_CRAFTER.apply(player, effect, c2);

        assertEquals(3, player.getNFood(), "deve ricevere cibo con conteggio simboli pari");
    }

    @Test
    @DisplayName("FOOD_FOR_CRAFTER — non aggiunge cibo con conteggio simboli dispari")
    void testFoodForCrafter_oddCount() {
        Crafter c1 = new Crafter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BREAD, CardTypeEnum.CRAFTER);
        player.addCard(c1);

        GainFoodEnum.FOOD_FOR_CRAFTER.apply(player, effect, c1);

        assertEquals(0, player.getNFood(), "non deve ricevere cibo con conteggio simboli dispari");
        verify(effect, never()).getFoodAmount();
    }

    @Test
    @DisplayName("FOOD_FOR_CRAFTER — non aggiunge cibo con zero crafter del simbolo")
    void testFoodForCrafter_zeroCount() {
        Crafter c = new Crafter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CrafterSymbolEnum.BREAD, CardTypeEnum.CRAFTER);

        GainFoodEnum.FOOD_FOR_CRAFTER.apply(player, effect, c);

        assertEquals(0, player.getNFood(), "non deve ricevere cibo senza crafter del simbolo");
        verify(effect, never()).getFoodAmount();
    }

    @Test
    @DisplayName("FOOD_FOR_CRAFTER — isOneTime restituisce false")
    void testFoodForCrafter_isNotOneTime() {
        assertFalse(GainFoodEnum.FOOD_FOR_CRAFTER.isOneTime());
    }


    @Test
    @DisplayName("FOOD_FOR_HUNTER_HUNT — attiva huntBonus: applyHuntBonus dà cibo e PP per ogni hunter")
    void testFoodForHunterHunt_activatesBonus() {
        Hunter h1 = new Hunter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER);
        Hunter h2 = new Hunter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER);
        player.addCard(h1);
        player.addCard(h2);

        GainFoodEnum.FOOD_FOR_HUNTER_HUNT.apply(player, effect, h1);
        player.applyHuntBonus();

        assertEquals(2, player.getNFood(), "deve ricevere 1 cibo per ogni hunter");
        assertEquals(2, player.getPP(),    "deve ricevere 1 PP per ogni hunter");
    }

    @Test
    @DisplayName("FOOD_FOR_HUNTER_HUNT — senza hunter applyHuntBonus non dà nulla")
    void testFoodForHunterHunt_noHunters() {
        GainFoodEnum.FOOD_FOR_HUNTER_HUNT.apply(player, effect, mock(Hunter.class));
        player.applyHuntBonus();

        assertEquals(0, player.getNFood());
    }

    @Test
    @DisplayName("FOOD_FOR_HUNTER_HUNT — isOneTime restituisce true")
    void testFoodForHunterHunt_isOneTime() {
        assertTrue(GainFoodEnum.FOOD_FOR_HUNTER_HUNT.isOneTime());
    }


    @Test
    @DisplayName("FOOD_FOR_ARTIST_PAINT — attiva paintBonus: applyPaintBonus dà cibo per ogni painter")
    void testFoodForArtistPaint_activatesBonus() {
        Painter p1 = new Painter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        Painter p2 = new Painter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
        player.addCard(p1);
        player.addCard(p2);

        GainFoodEnum.FOOD_FOR_ARTIST_PAINT.apply(player, effect, p1);
        player.applyPaintBonus();

        assertEquals(2, player.getNFood(), "deve ricevere 1 cibo per ogni painter");
    }

    @Test
    @DisplayName("FOOD_FOR_ARTIST_PAINT — senza painter applyPaintBonus non dà nulla")
    void testFoodForArtistPaint_noPainters() {
        GainFoodEnum.FOOD_FOR_ARTIST_PAINT.apply(player, effect, mock(Painter.class));
        player.applyPaintBonus();

        assertEquals(0, player.getNFood());
    }

    @Test
    @DisplayName("FOOD_FOR_ARTIST_PAINT — isOneTime restituisce true")
    void testFoodForArtistPaint_isOneTime() {
        assertTrue(GainFoodEnum.FOOD_FOR_ARTIST_PAINT.isOneTime());
    }


    @Test
    @DisplayName("FOOD_FLAT — aggiunge esattamente la quantità flat (apply a tre argomenti)")
    void testFoodFlat_addsCorrectAmount() {
        when(effect.getFoodAmount()).thenReturn(7);
        Builder card = new Builder(1, null, new ArrayList<>(), new ArrayList<>(), 1, 1, CardTypeEnum.BUILDER);

        GainFoodEnum.FOOD_FLAT.apply(player, effect, card);

        assertEquals(7, player.getNFood());
    }

    @Test
    @DisplayName("FOOD_FLAT — apply a due argomenti aggiunge la quantità flat senza carta")
    void testFoodFlat_twoArgApply_addsCorrectAmount() {
        when(effect.getFoodAmount()).thenReturn(4);

        GainFoodEnum.FOOD_FLAT.apply(player, effect);

        assertEquals(4, player.getNFood(), "l'override a due argomenti di FOOD_FLAT deve aggiungere il cibo flat");
    }

    @Test
    @DisplayName("FOOD_FLAT — isOneTime restituisce true")
    void testFoodFlat_isOneTime() {
        assertTrue(GainFoodEnum.FOOD_FLAT.isOneTime());
    }


    @Test
    @DisplayName("FOOD_EXTRA — isOneTime restituisce true")
    void testFoodExtra_isOneTime() {
        assertTrue(GainFoodEnum.FOOD_EXTRA.isOneTime());
    }

    @Test
    @DisplayName("FOOD_EXTRA — attiva extraFoodOnQueue: applyQueueFoodBonus dà 1 cibo extra su tile con food effect")
    void testFoodExtra_activatesQueueBonus() {
        Builder trigger = new Builder(1, null, new ArrayList<>(), new ArrayList<>(), 1, 1, CardTypeEnum.BUILDER);

        GainFoodEnum.FOOD_EXTRA.apply(player, effect, trigger);
        player.applyQueueFoodBonus(true);

        assertEquals(1, player.getNFood(), "deve ricevere 1 cibo extra quando il bonus è attivo e la tile dà cibo");
    }


    @Test
    @DisplayName("FOOD_FOR_HUNTER — aggiunge cibo pari al numero di hunter posseduti")
    void testFoodForHunter_addsHunterCount() {
        Hunter h1 = new Hunter(1, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER);
        Hunter h2 = new Hunter(2, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.HUNTER);
        player.addCard(h1);
        player.addCard(h2);

        GainFoodEnum.FOOD_FOR_HUNTER.apply(player, effect, h1);

        assertEquals(2, player.getNFood(), "deve ricevere 1 cibo per ogni hunter posseduto");
    }

    @Test
    @DisplayName("FOOD_FOR_HUNTER — isOneTime restituisce true")
    void testFoodForHunter_isOneTime() {
        assertTrue(GainFoodEnum.FOOD_FOR_HUNTER.isOneTime());
    }


    @Test
    @DisplayName("valueOf — tutte le 7 costanti sono raggiungibili")
    void testEnumValues_allPresent() {
        assertEquals(7, GainFoodEnum.values().length);
        assertNotNull(GainFoodEnum.valueOf("FOOD_FOR_SET"));
        assertNotNull(GainFoodEnum.valueOf("FOOD_FOR_CRAFTER"));
        assertNotNull(GainFoodEnum.valueOf("FOOD_FOR_HUNTER_HUNT"));
        assertNotNull(GainFoodEnum.valueOf("FOOD_FOR_ARTIST_PAINT"));
        assertNotNull(GainFoodEnum.valueOf("FOOD_FLAT"));
        assertNotNull(GainFoodEnum.valueOf("FOOD_EXTRA"));
        assertNotNull(GainFoodEnum.valueOf("FOOD_FOR_HUNTER"));
    }
}