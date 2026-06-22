package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.DiscountFood;
import it.polimi.ingsw.model.interfaces.DiscountFoodModifier;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the available food discount effects that can be applied to a player.
 *
 * <p>Each constant defines a specific discount strategy by implementing
 * {@link DiscountFoodModifier}. All variants are one-time effects.</p>
 */
public enum DiscountFoodEnum implements DiscountFoodModifier {

    /**
     * Applies a discount based on a specific card category.
     * Adds a category-based discount to the player.
     */
    DISCOUNT_CAT((p, e) -> p.addCategoryDiscount(e.getCat())),

    /**
     * Applies a flat food discount to the player.
     */
    DISCOUNT_FLAT((p, e) -> p.addFoodDiscount(e.getFoodAmount())),

    /**
     * Applies a food discount for building cards.
     */
    DISCOUNT_FOR_BUILDING((p, e) -> p.addTotBuildDiscount(e.getFoodAmount()));


    /** The strategy that defines how this discount is applied. */
    private final DiscountFoodModifier modifier;

    /**
     * Creates a new {@code DiscountFoodEnum} constant with the given discount strategy.
     *
     * @param modifier the {@link DiscountFoodModifier} that defines how the discount is applied
     */
    DiscountFoodEnum(DiscountFoodModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the discount effect to the given player.
     *
     * @param p      the player to apply the discount to
     * @param effect the {@link DiscountFood} effect containing the discount details
     */
    @Override
    public void apply(Player p, DiscountFood effect) {
        modifier.apply(p, effect);
    }

    /**
     * Returns whether this effect is applied only once.
     *
     * @return {@code false} by default; overridden to {@code true} in all constants
     */
    public boolean isOneTime() { return true; }
}
