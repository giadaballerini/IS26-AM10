package it.polimi.ingsw.model.entities.card.effects.interactive;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;

/**
 * Abstract base class for interactive card effects.
 *
 * <p>An interactive effect, when applied, generates a pending {@link Action}
 * that the owning player must resolve, typically a draw from the upper or
 * lower card row. The concrete subclass {@link DrawCard} is the only current
 * implementation. Jackson uses the {@code type} property in the JSON to
 * deserialize the correct subclass.</p>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DrawCard.class, name = "DRAW_CARD")
})
public abstract class CardEffectInteractive {

    /**
     * Applies this interactive effect to the given player, generating and
     * returning the resulting pending action.
     *
     * @param p the player this effect is applied to; must not be {@code null}
     * @return the {@link Action} the player must resolve; never {@code null}
     */
    public abstract Action apply(Player p);

    /**
     * Prints a human-readable description of this effect, used for debugging
     * or TUI display.
     */
    public abstract void displayEffect();

    /**
     * Returns the number of upper-row draws this effect grants.
     * The default implementation returns {@code 0}.
     *
     * @return number of upper draws
     */
    public int getUpDraws() {
        return 0;
    }

    /**
     * Returns the number of lower-row draws this effect grants.
     * The default implementation returns {@code 0}.
     *
     * @return number of lower draws
     */
    public int getDownDraws() {
        return 0;
    }
}