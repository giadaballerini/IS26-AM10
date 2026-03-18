package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.interfaces.GainFoodModifier;
import it.polimi.ingsw.model.player.Player;

public enum GainFoodEnum implements GainFoodModifier {
    FOOD_FOR_SET((p, e, g) -> {}),
    FOOD_FOR_CRAFTER((p, e, g) -> {}),
    FOOD_FOR_HUNTER_HUNT((p, e, g) -> {}),
    FOOD_FOR_ARTIST_PAINT((p, e, g) -> {}),
    FOOD_FLAT((p, e, g) -> {});

    private final GainFoodModifier modifier;
    GainFoodEnum(GainFoodModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void apply(Player p, GainFood effect, GamePhaseEnum g) {
        modifier.apply(p, effect, g);
    }
}
