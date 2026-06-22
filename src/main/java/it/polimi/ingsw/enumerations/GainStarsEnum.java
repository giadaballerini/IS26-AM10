package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.interfaces.GainStarsModifier;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the available stars gain effects that can be applied to a player.
 *
 * <p>Implements {@link GainStarsModifier} to define how stars are awarded.
 * This is a one-time effect.</p>
 */
public enum GainStarsEnum implements GainStarsModifier {

    /**
     * Grants a flat amount of stars to the player.
     */
    GAIN_STARS((p, e) -> p.addStars(e.getStarsAmount()));


    /** The strategy used to apply this stars gain effect to a player. */
    private final GainStarsModifier modifier;

    /**
     * Creates a new {@code GainStarsEnum} constant with the given strategy.
     *
     * @param modifier the {@link GainStarsModifier} that defines how the stars gain effect is applied
     */
    GainStarsEnum(GainStarsModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the star gain effect to the given player.
     *
     * @param p the player to apply the effect to
     * @param effect the {@link GainStars} effect containing the stars amount
     */
    @Override
    public void apply(Player p, GainStars effect) {
        modifier.apply(p, effect);
    }

    /**
     * Returns whether this effect is applied only once.
     *
     * @return {@code true}, as this effect is always one-time
     */
    public boolean isOneTime() { return true; }
}
