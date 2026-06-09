package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.LossIfBroke;
import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for the penalty applied to a player who cannot pay a
 * food cost.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.LossIfBrokeEnum}.
 * The only current rule deducts food from the player if they have any, or
 * deducts prestige points instead if they have no food left.</p>
 */
public interface LossIfBrokeModifier {

    /**
     * Applies the penalty defined by the given effect to the specified player.
     *
     * @param p      the player to penalise; must not be {@code null}
     * @param effect the penalty effect carrying the PP and food costs;
     *               must not be {@code null}
     */
    void apply(Player p, LossIfBroke effect);
}