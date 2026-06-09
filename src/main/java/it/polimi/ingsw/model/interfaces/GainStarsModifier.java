package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.GainStars;
import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for star gain behaviours a card can trigger.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.GainStarsEnum}.
 * Currently, the only rule is a flat gain of a fixed number of stars, but the
 * interface allows additional strategies to be added without modifying
 * existing code.</p>
 */
public interface GainStarsModifier {

    /**
     * Applies the given star gain effect to the specified player.
     *
     * @param p      the player to apply the effect to; must not be {@code null}
     * @param effect the star gain effect to apply; must not be {@code null}
     */
    void apply(Player p, GainStars effect);
}