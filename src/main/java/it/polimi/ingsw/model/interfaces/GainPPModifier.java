package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for the different prestige point gain behaviours a card
 * can trigger.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.GainPPEnum}.
 * The source {@link Card} is always passed along
 * because several rules depend on the card that triggered the effect.</p>
 */
public interface GainPPModifier {

    /**
     * Applies the given prestige point gain effect to the specified player.
     *
     * @param p      the player to apply the effect to; must not be {@code null}
     * @param effect the PP gain effect to apply; must not be {@code null}
     * @param c      the card that triggered the effect; must not be {@code null}
     */
    void apply(Player p, GainPP effect, Card c);
}