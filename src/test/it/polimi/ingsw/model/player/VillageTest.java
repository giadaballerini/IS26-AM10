package it.polimi.ingsw.model.player;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.network.dto.CardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VillageTest {


    private Village village;

    @BeforeEach
    void setUp() {
        village = new Village();
    }

    private Crafter crafter(int id, CrafterSymbolEnum symbol) {
        return new Crafter(id, null, new ArrayList<>(), new ArrayList<>(), 1, symbol, CardTypeEnum.CRAFTER);
    }

    private Builder builder(int id, int ppAmount) {
        return new Builder(id, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, ppAmount, CardTypeEnum.BUILDER);
    }

    private Hunter hunter(int id) {
        return new Hunter(id, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.HUNTER);
    }

    private Painter painter(int id) {
        return new Painter(id, null, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.PAINTER);
    }

    private Gatherer gatherer(int id) {
        return new Gatherer(id, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 1, CardTypeEnum.GATHERER);
    }

    private Shaman shaman(int id) {
        return new Shaman(id, GamePhaseEnum.DRAW_PHASE, new ArrayList<>(), new ArrayList<>(), 2, CardTypeEnum.SHAMAN);
    }


    @Test
    void emptyVillageShouldHaveZeroCharacters() {
        assertEquals(0, village.getNumCharacters());
    }

    @Test
    void emptyVillageShouldHaveZeroForAnyType() {
        assertEquals(0, village.getNumType(CardTypeEnum.BUILDER));
        assertEquals(0, village.getNumType(CardTypeEnum.CRAFTER));
    }

    @Test
    void emptyVillageShouldHaveZeroBuilderPoints() {
        assertEquals(0, village.builderPoints());
    }

    @Test
    void emptyVillageShouldReturnEmptyCharactersDTO() {
        assertTrue(village.getCharactersDTO().isEmpty());
    }


    @Test
    void shouldAddCrafter() {
        village.add(crafter(1, CrafterSymbolEnum.AMIGDALA));
        assertEquals(1, village.getNumType(CardTypeEnum.CRAFTER));
    }

    @Test
    void shouldAddBuilder() {
        village.add(builder(2, 3));
        assertEquals(1, village.getNumType(CardTypeEnum.BUILDER));
    }

    @Test
    void shouldAddPainter() {
        village.add(painter(3));
        assertEquals(1, village.getNumType(CardTypeEnum.PAINTER));
    }

    @Test
    void shouldAddHunter() {
        village.add(hunter(4));
        assertEquals(1, village.getNumType(CardTypeEnum.HUNTER));
    }

    @Test
    void shouldAddGatherer() {
        village.add(gatherer(5));
        assertEquals(1, village.getNumType(CardTypeEnum.GATHERER));
    }

    @Test
    void shouldAddShaman() {
        village.add(shaman(6));
        assertEquals(1, village.getNumType(CardTypeEnum.SHAMAN));
    }

    @Test
    void getNumTypeShouldReturnZeroForAbsentType() {
        village.add(builder(1, 2));
        assertEquals(0, village.getNumType(CardTypeEnum.SHAMAN));
    }


    @Test
    void shouldCountAllCharactersRegardlessOfType() {
        village.add(shaman(1));
        village.add(builder(2, 1));
        village.add(hunter(3));
        village.add(painter(4));

        assertEquals(4, village.getNumCharacters());
    }

    @Test
    void shouldCountMultipleCardsOfSameType() {
        village.add(builder(1, 1));
        village.add(builder(2, 2));

        assertEquals(2, village.getNumType(CardTypeEnum.BUILDER));
    }


    @Test
    void shouldCountCraftersBySymbol() {
        village.add(crafter(1, CrafterSymbolEnum.AMIGDALA));
        village.add(crafter(2, CrafterSymbolEnum.ARROWHEAD));
        village.add(crafter(3, CrafterSymbolEnum.AMIGDALA));

        assertEquals(2, village.getNumSymbolsForCrafter(CrafterSymbolEnum.AMIGDALA));
        assertEquals(1, village.getNumSymbolsForCrafter(CrafterSymbolEnum.ARROWHEAD));
    }

    @Test
    void shouldReturnZeroForAbsentCrafterSymbol() {
        village.add(crafter(1, CrafterSymbolEnum.AMIGDALA));

        assertEquals(0, village.getNumSymbolsForCrafter(CrafterSymbolEnum.ARROWHEAD));
    }


    @Test
    void shouldCountSymbolsWhenCardIsCrafter() {
        Crafter reference = crafter(10, CrafterSymbolEnum.AMIGDALA);
        village.add(crafter(1, CrafterSymbolEnum.AMIGDALA));
        village.add(crafter(2, CrafterSymbolEnum.AMIGDALA));
        village.add(crafter(3, CrafterSymbolEnum.ARROWHEAD));

        assertEquals(2, village.getNumSymbolsForCrafter(reference));
    }

    @Test
    void shouldReturnZeroWhenCardIsNotCrafter() {
        village.add(crafter(1, CrafterSymbolEnum.AMIGDALA));
        Hunter nonCrafter = hunter(99);

        assertEquals(0, village.getNumSymbolsForCrafter(nonCrafter));
    }



    @Test
    void builderPointsShouldSumAllBuilders() {
        for (int i = 0; i < 6; i++) {
            village.add(builder(i, 6));
        }
        assertEquals(36, village.builderPoints());
    }

    @Test
    void builderPointsShouldIgnoreNonBuilderCharacters() {
        village.add(builder(1, 5));
        village.add(shaman(2));
        village.add(hunter(3));

        assertEquals(5, village.builderPoints());
    }

    @Test
    void builderPointsShouldReflectDifferentPpAmounts() {
        village.add(builder(1, 2));
        village.add(builder(2, 4));
        village.add(builder(3, 6));

        assertEquals(12, village.builderPoints());
    }


    @Test
    void getCharactersDTOShouldReturnCorrectIdAndType() {
        village.add(builder(42, 3));
        village.add(hunter(7));

        List<CardDTO> dtos = village.getCharactersDTO();

        assertEquals(2, dtos.size());
        CardDTO builderDTO = dtos.stream().filter(d -> d.getId() == 42).findFirst().orElseThrow();
        assertEquals(CardTypeEnum.BUILDER, builderDTO.getType());

        CardDTO hunterDTO = dtos.stream().filter(d -> d.getId() == 7).findFirst().orElseThrow();
        assertEquals(CardTypeEnum.HUNTER, hunterDTO.getType());
    }

    @Test
    void getCharactersDTOShouldReturnDefensiveCopy() {
        village.add(builder(1, 1));
        List<CardDTO> dtos = village.getCharactersDTO();

        dtos.clear();
        assertEquals(1, village.getNumCharacters());
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — characters null → villaggio vuoto senza NPE")
    void jsonCreator_nullCharacters_createsEmptyVillage() {
        Village v = new Village(null);
        assertEquals(0, v.getNumCharacters());
        assertDoesNotThrow(() -> v.builderPoints());
        assertDoesNotThrow(() -> v.getCharactersDTO());
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@JsonCreator — lista non nulla → caratteri ripristinati correttamente")
    void jsonCreator_nonNullCharacters_restoredCorrectly() {
        Builder b = builder(1, 3);
        Hunter h = hunter(2);
        Village v = new Village(List.of(b, h));

        assertEquals(2, v.getNumCharacters());
        assertEquals(1, v.getNumType(CardTypeEnum.BUILDER));
        assertEquals(1, v.getNumType(CardTypeEnum.HUNTER));
    }
}