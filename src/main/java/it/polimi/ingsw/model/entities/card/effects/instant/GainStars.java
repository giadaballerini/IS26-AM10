package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GainStarsEnum;
import it.polimi.ingsw.model.player.Player;

/**
 * An instant effect that grants stars to the owning player.
 *
 * <p>The gain rule is determined by {@link #gainStarsType}. Currently, the only
 * rule awards a flat number of stars equal to {@link #starsAmount}.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GainStars extends CardEffectInstant {

    /** The number of stars awarded by this effect. */
    private final int starsAmount;

    /** The gain rule that determines how stars are awarded. */
    private final GainStarsEnum gainStarsType;

    /**
     * Constructs a {@code GainStars} effect.
     *
     * @param starsAmount   the number of stars to award
     * @param gainStarsType the rule determining how stars are awarded;
     *                      must not be {@code null}
     */
    @JsonCreator
    public GainStars(@JsonProperty("starsAmount") int starsAmount,
                     @JsonProperty("gainStarsEnum") GainStarsEnum gainStarsType) {
        this.starsAmount = starsAmount;
        this.gainStarsType = gainStarsType;
    }

    /**
     * Applies the star gain to the given player, delegating to
     * {@link GainStarsEnum#apply(Player, GainStars)}.
     *
     * @param p the player to apply the effect to; must not be {@code null}
     */
    @Override
    public void apply(Player p) {
        gainStarsType.apply(p, this);
    }

    /**
     * Prints the number of stars granted, used for debugging or TUI display.
     */
    @Override
    public void displayEffect() {
        System.out.println("\nAggiunte " + starsAmount + " stelle");
    }

    /**
     * Returns the number of stars awarded by this effect.
     *
     * @return stars amount
     */
    public int getStarsAmount() {
        return starsAmount;
    }

    /**
     * Returns whether this effect is consumed after firing once, as determined
     * by the gain rule.
     *
     * @return {@code true} if the effect is one-time
     */
    @Override
    public boolean isOneTime() { return gainStarsType.isOneTime(); }
}