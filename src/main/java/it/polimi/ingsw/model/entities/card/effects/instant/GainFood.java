package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GainFoodEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

/**
 * An instant effect that grants food to the owning player.
 *
 * <p>The exact gain rule is determined by {@link #gainFoodType}: it may award
 * a flat amount, an amount based on the number of cards of a given type in the
 * player's village, or an amount based on completed crafter symbol sets.
 * The {@link #foodAmount} field carries the base parameter used by the rule.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GainFood extends CardEffectInstant {

    /** The base food amount used as a parameter by the gain rule. */
    private final int foodAmount;

    /** The gain rule that determines how food is awarded. */
    private final GainFoodEnum gainFoodType;

    /**
     * Constructs a {@code GainFood} effect.
     *
     * @param foodAmount   the base food amount used by the gain rule
     * @param gainFoodType the rule determining how food is calculated and awarded;
     *                     must not be {@code null}
     */
    @JsonCreator
    public GainFood(@JsonProperty("foodAmount") int foodAmount,
                    @JsonProperty("gainFoodType") GainFoodEnum gainFoodType) {
        this.foodAmount = foodAmount;
        this.gainFoodType = gainFoodType;
    }

    /**
     * Applies the food gain to the given player with access to the source card,
     * delegating to {@link GainFoodEnum#apply(Player, GainFood, Card)}.
     *
     * @param p the player to apply the effect to; must not be {@code null}
     * @param c the card that carries this effect; must not be {@code null}
     */
    @Override
    public void apply(Player p, Card c) {
        gainFoodType.apply(p, this, c);
    }

    /**
     * Applies the food gain to the given player without a source card context,
     * delegating to {@link GainFoodEnum#apply(Player, GainFood)}.
     *
     * @param p the player to apply the effect to; must not be {@code null}
     */
    @Override
    public void apply(Player p) {
        gainFoodType.apply(p, this);
    }

    /**
     * Prints the food amount granted, used for debugging or TUI display.
     */
    @Override
    public void displayEffect() {
        System.out.println("\nAggiunto " + foodAmount + " di cibo");
    }

    /**
     * Returns the base food amount used as a parameter by the gain rule.
     *
     * @return base food amount
     */
    @Override
    public int getFoodAmount() {
        return foodAmount;
    }

    /**
     * Returns whether this effect is consumed after firing once, as determined
     * by the gain rule.
     *
     * @return {@code true} if the effect is one-time
     */
    @Override
    public boolean isOneTime() { return gainFoodType.isOneTime(); }

    /**
     * Returns {@code true}, since this effect always provides a food reward.
     * Used to determine whether the queue food bonus applies when a player
     * enters a tile carrying this effect.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFoodEffect() { return true; }
}