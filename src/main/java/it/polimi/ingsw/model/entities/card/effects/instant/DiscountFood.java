package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DiscountFoodEnum;
import it.polimi.ingsw.model.player.Player;

/**
 * An instant effect that applies a cost discount to the owning player.
 *
 * <p>The exact discount rule is determined by {@link #discountFoodType}: it may
 * add a flat food discount, grant a per-card-category discount, or reduce
 * building costs. The {@link #cat} and {@link #foodAmount} fields carry the
 * parameters needed by the chosen rule.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DiscountFood extends CardEffectInstant {

    /**
     * The card category used by category-based discount rules.
     * May be {@code null} for flat or building discount rules.
     */
    private final CardTypeEnum cat;

    /** The discount amount applied by this effect. */
    private final int foodAmount;

    /** The discount rule to apply. */
    private final DiscountFoodEnum discountFoodType;

    /**
     * Constructs a {@code DiscountFood} effect.
     *
     * @param cat              the card category relevant to this discount;
     *                         may be {@code null}
     * @param foodAmount       the discount amount
     * @param discountFoodType the rule determining how the discount is applied;
     *                         must not be {@code null}
     */
    @JsonCreator
    public DiscountFood(@JsonProperty("category") CardTypeEnum cat,
                        @JsonProperty("foodAmount") int foodAmount,
                        @JsonProperty("discountFoodType") DiscountFoodEnum discountFoodType) {
        this.cat = cat;
        this.foodAmount = foodAmount;
        this.discountFoodType = discountFoodType;
    }

    /**
     * Applies the discount to the given player by delegating to
     * {@link DiscountFoodEnum#apply(Player, DiscountFood)}.
     *
     * @param p the player to apply the discount to; must not be {@code null}
     */
    @Override
    public void apply(Player p) {
        discountFoodType.apply(p, this);
    }


    /**
     * Returns the card category relevant to this discount rule.
     *
     * @return the category, or {@code null} if not applicable
     */
    public CardTypeEnum getCat() {
        return cat;
    }

    /**
     * Returns the discount amount applied by this effect.
     *
     * @return the discount amount
     */
    @Override
    public int getFoodAmount() {
        return foodAmount;
    }

    /**
     * Returns whether this effect is consumed after firing once, as determined
     * by the discount rule.
     *
     * @return {@code true} if the effect is one-time
     */
    @Override
    public boolean isOneTime() { return discountFoodType.isOneTime(); }
}