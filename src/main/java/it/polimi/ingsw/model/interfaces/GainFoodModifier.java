package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for the different food gain behaviours a card can trigger.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.GainFoodEnum}, whose
 * values each encode a distinct rule — flat gain, gain per completed set,
 * gain per crafter symbol pair, and so on. The overload that receives the
 * source {@link Card} is used when the calculation depends on the card that
 * triggered the effect (e.g. checking whether a new set was completed).
 * The card-less overload handles the cases where the source card is irrelevant,
 * and defaults to a no-op.</p>
 */
public interface GainFoodModifier {

    /**
     * Applies the given food gain effect to the specified player, with access
     * to the card that triggered the effect.
     *
     * @param p      the player to apply the effect to; must not be {@code null}
     * @param effect the food gain effect to apply; must not be {@code null}
     * @param c      the card that triggered the effect; must not be {@code null}
     */
    void apply(Player p, GainFood effect, Card c);

    /**
     * Applies the given food gain effect to the specified player, without a
     * source card context. The default implementation is a no-op.
     *
     * @param p      the player to apply the effect to; must not be {@code null}
     * @param effect the food gain effect to apply; must not be {@code null}
     */
    default void apply(Player p, GainFood effect) {}
}