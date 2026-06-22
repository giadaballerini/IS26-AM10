package it.polimi.ingsw.model.action;

import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents a pending game action assigned to a specific player.
 *
 * <p>An {@code Action} pairs a {@link Player} with a {@link DrawCardEnum} type,
 * describing what kind of draw or effect that player is required to resolve.</p>
 */
public class Action {

    /** The player who owns and must resolve this action. */
    private final Player owner;

    /** The type of draw or effect to be resolved. */
    private final DrawCardEnum type;

    /**
     * Constructs an {@code Action} for the given player and action type.
     *
     * @param owner the player who must resolve this action; must not be {@code null}
     * @param type  the type of draw or effect to resolve; must not be {@code null}
     */
    public Action(Player owner, DrawCardEnum type) {
        this.owner = owner;
        this.type = type;
    }

    /**
     * Returns the type of draw or effect to be resolved.
     *
     * @return the {@link DrawCardEnum} value
     */
    public DrawCardEnum getType() {
        return this.type;
    }

    /**
     * Returns the player who owns and must resolve this action.
     *
     * @return the owner {@link Player}; never {@code null}
     */
    public Player getOwner() {
        return this.owner;
    }
}