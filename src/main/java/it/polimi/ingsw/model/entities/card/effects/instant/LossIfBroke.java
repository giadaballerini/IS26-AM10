package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.LossIfBrokeEnum;
import it.polimi.ingsw.model.player.Player;

/**
 * An instant effect that penalises a player who cannot pay a food cost.
 *
 * <p>When applied, the rule defined by {@link #lossIfBrokeType} deducts
 * {@link #foodCost} food from the player if they have enough, or
 * {@link #ppCost} prestige points instead if they do not.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LossIfBroke extends CardEffectInstant {

    /** The prestige point penalty applied when the player cannot pay the food cost. */
    private final int ppCost;

    /** The food cost the player must pay to avoid the PP penalty. */
    private final int foodCost;

    /** The penalty rule that determines how the cost is resolved. */
    private final LossIfBrokeEnum lossIfBrokeType;

    /**
     * Constructs a {@code LossIfBroke} effect.
     *
     * @param ppCost          the PP penalty applied if the player has no food
     * @param foodCost        the food cost the player must pay to avoid the penalty
     * @param lossIfBrokeType the rule determining how the cost is resolved;
     *                        must not be {@code null}
     */
    @JsonCreator
    public LossIfBroke(@JsonProperty("ppCost") int ppCost,
                       @JsonProperty("foodCost") int foodCost,
                       @JsonProperty("lossIfBrokeType") LossIfBrokeEnum lossIfBrokeType) {
        this.ppCost = ppCost;
        this.foodCost = foodCost;
        this.lossIfBrokeType = lossIfBrokeType;
    }

    /**
     * Applies the penalty to the given player, delegating to
     * {@link LossIfBrokeEnum#apply(Player, LossIfBroke)}.
     *
     * @param p the player to apply the penalty to; must not be {@code null}
     */
    @Override
    public void apply(Player p) {
        lossIfBrokeType.apply(p, this);
    }

    /**
     * Returns the prestige point penalty applied when the player cannot pay
     * the food cost.
     *
     * @return PP penalty amount
     */
    public int getPpCost() { return ppCost; }

    /**
     * Returns the food cost the player must pay to avoid the PP penalty.
     *
     * @return food cost
     */
    public int getFoodCost() { return foodCost; }

    /**
     * Prints the PP penalty amount, used for debugging or TUI display.
     */
    @Override
    public void displayEffect() { System.out.println("\nDetratti " + ppCost + "PP"); }

    /**
     * Returns whether this effect is consumed after firing once, as determined
     * by the penalty rule.
     *
     * @return {@code true} if the effect is one-time
     */
    @Override
    public boolean isOneTime() { return lossIfBrokeType.isOneTime(); }
}