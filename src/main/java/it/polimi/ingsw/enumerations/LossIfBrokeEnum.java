package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.interfaces.LossIfBrokeModifier;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the penalty effect applied to a player when they cannot afford a food cost.
 *
 * <p>Implements {@link LossIfBrokeModifier} to define how the penalty is resolved:
 * if the player has no food, they lose PP instead; otherwise they lose food.</p>
 */
public enum LossIfBrokeEnum implements LossIfBrokeModifier {

    /**
     * Applies a food cost to the player, or a PP penalty if the player has no food.
     */
    LOSS_IF_BROKE((p, e) -> {
        if (p.getNFood() == 0) {
            p.addPP(-e.getPpCost());
        } else p.addFood(-e.getFoodCost());
    });


    /** The strategy used to apply this penalty effect to a player. */
    private final LossIfBrokeModifier modifier;

    /**
     * Creates a new {@code LossIfBrokeEnum} constant with the given strategy.
     *
     * @param modifier the {@link LossIfBrokeModifier} that defines how the penalty effect is applied
     */
    LossIfBrokeEnum(LossIfBrokeModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the penalty effect to the given player.
     *
     * @param p the player to apply the effect to
     * @param effect the {@link LossIfBroke} effect containing the food and PP costs
     */
    public void apply(Player p, LossIfBroke effect) {
        modifier.apply(p, effect);
    }

    /**
     * Returns whether this effect is applied only once.
     *
     * @return {@code false}, as this effect can be applied multiple times
     */
    public boolean isOneTime() { return false; }
}
