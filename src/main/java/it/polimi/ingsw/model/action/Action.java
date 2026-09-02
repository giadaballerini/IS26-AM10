package it.polimi.ingsw.model.action;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents a pending game action assigned to a specific player.
 *
 * <p>An {@code Action} pairs a {@link Player} with a {@link DrawCardEnum} type,
 * describing what kind of draw or effect that player is required to resolve.</p>
 */
public class Action {

    /**
     * The player who owns and must resolve this action.
     *
     * <p>Excluded from JSON serialization to avoid a circular reference back
     * to {@link Player} (which holds a list of pending {@code Action}s).
     * After deserialization this field is always {@code null}; callers
     * restoring a match from a saved snapshot must repair it via
     * {@link #withOwner(Player)} (see
     * {@link Player#reconnectSkippableDrawOwners()}).</p>
     */
    @JsonIgnore
    private final Player owner;

    /** The type of draw or effect to be resolved. */
    private final DrawCardEnum type;

    /**
     * Constructs an {@code Action} for the given player and action type.
     *
     * <p>Annotated as the Jackson creator so that {@code Action} instances
     * nested inside a deserialized {@link Player}'s pending skippable draws
     * can be reconstructed; {@code owner} is always {@code null} when
     * deserialized, since the corresponding JSON property is excluded by
     * {@link #getOwner()}'s {@code @JsonIgnore}.</p>
     *
     * @param owner the player who must resolve this action; {@code null} when
     *              reconstructed by Jackson, must not be {@code null} otherwise
     * @param type  the type of draw or effect to resolve; must not be {@code null}
     */
    @JsonCreator
    public Action(@JsonProperty("owner") Player owner, @JsonProperty("type") DrawCardEnum type) {
        this.owner = owner;
        this.type = type;
    }

    /**
     * Returns a copy of this action with its owner replaced by the given player,
     * keeping the same action type.
     *
     * <p>Used to repair {@code owner} references after JSON deserialization,
     * since the field is excluded from serialization (see {@link #getOwner()}).</p>
     *
     * @param newOwner the player to set as the owner of the returned action
     * @return a new {@code Action} with {@code newOwner} as owner and the same type
     */
    public Action withOwner(Player newOwner) {
        return new Action(newOwner, this.type);
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
    @JsonIgnore
    public Player getOwner() {
        return this.owner;
    }
}