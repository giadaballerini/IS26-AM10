package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for the different discount behaviours a card can apply
 * to a player's future costs.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.DiscountFoodEnum}, whose
 * values each encode a distinct rule — a flat food discount on feast costs,
 * a per-card-category discount, or a building cost reduction.</p>
 */
public interface DiscountFoodModifier {

    /**
     * Applies the given food discount effect to the specified player.
     *
     * @param p      the player to apply the discount to; must not be {@code null}
     * @param effect the food discount effect to apply; must not be {@code null}
     */
    void apply(Player p, DiscountFood effect);
}