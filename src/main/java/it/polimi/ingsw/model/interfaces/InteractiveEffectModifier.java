package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for creating the {@link Action} produced by an
 * interactive card effect.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.DrawCardEnum}, whose
 * values ({@code UP_DRAW} and {@code DOWN_DRAW}) each construct a new
 * {@link Action} pairing the given player with the draw type. The resulting
 * action is queued on the player and resolved later, either immediately or
 * at the player's discretion if it is skippable.</p>
 */
public interface InteractiveEffectModifier {

    /**
     * Creates and returns the {@link Action} that the given player must resolve
     * as a result of an interactive card effect.
     *
     * @param p        the player who triggered the effect; must not be {@code null}
     * @param drawCard the type of draw the effect requires; must not be {@code null}
     * @return the action to be queued for the player; never {@code null}
     */
    Action apply(Player p, DrawCardEnum drawCard);
}