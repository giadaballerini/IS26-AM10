package it.polimi.ingsw.model.entities.card.effects.interactive;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.DrawCountVisitor;

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
     * Accepts a {@link DrawCountVisitor}, allowing it to accumulate the draw
     * counts contributed by this effect.
     *
     * <p>Concrete subclasses that grant draw actions must forward themselves
     * to {@link DrawCountVisitor#visit(DrawCard)}; subclasses that do not
     * grant draws should implement this as a no-op.</p>
     *
     * @param visitor the visitor to accept; must not be {@code null}
     */
    public abstract void accept(DrawCountVisitor visitor);
    /**
     * Creates a new {@code CardEffectInteractive} instance.
     * Intended for use by subclasses only.
     */
    protected CardEffectInteractive() {
    }
}