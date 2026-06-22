package it.polimi.ingsw.enumerations;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardTypeEnumTest {

    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "GATHERER", "HUNTER", "PAINTER", "BUILDER", "SHAMAN", "CRAFTER"
    })
    void isCharacter_ShouldReturnTrue_ForCharacterTypes(CardTypeEnum cardType) {
        assertTrue(cardType.isCharacter(), "Dovrebbe essere un personaggio: " + cardType);
        assertFalse(cardType.isEvent(), "Non dovrebbe essere un evento: " + cardType);
    }

    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {
            "FEAST", "HUNT", "STONE_PAINTING", "RITUAL"
    })
    void isEvent_ShouldReturnTrue_ForEventTypes(CardTypeEnum cardType) {
        assertTrue(cardType.isEvent(), "Dovrebbe essere un evento: " + cardType);
        assertFalse(cardType.isCharacter(), "Non dovrebbe essere un personaggio: " + cardType);
    }

    @ParameterizedTest
    @EnumSource(value = CardTypeEnum.class, names = {"BUILDING"})
    void isBuilding_ShouldReturnFalseForAll(CardTypeEnum cardType) {
        assertFalse(cardType.isCharacter(), "Non dovrebbe essere un personaggio: " + cardType);
        assertFalse(cardType.isEvent(), "Non dovrebbe essere un evento: " + cardType);
    }
}