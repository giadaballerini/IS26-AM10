package it.polimi.ingsw.model.entities.card.effects.interactive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.DrawCountVisitor;

/**
 * An interactive card effect that grants the owning player a card draw.
 *
 * <p>The draw type (upper or lower row) is determined by {@link #drawCardType}.
 * Applying this effect delegates to {@link DrawCardEnum#apply(Player, DrawCardEnum)},
 * which creates and returns the corresponding pending {@link Action}.</p>
 */
public class DrawCard extends CardEffectInteractive {

    /** Whether this effect grants an upper-row or lower-row draw. */
    public final DrawCardEnum drawCardType;

    /**
     * Constructs a {@code DrawCard} effect with the given draw type.
     *
     * @param drawCardType the type of draw this effect grants;
     *                     must not be {@code null}
     */
    @JsonCreator
    public DrawCard(@JsonProperty("drawCardType") DrawCardEnum drawCardType) {
        this.drawCardType = drawCardType;
    }

    /**
     * Applies this effect to the given player, creating and returning the
     * pending draw action to be resolved.
     *
     * @param p the player this effect is applied to; must not be {@code null}
     * @return the pending {@link Action}; never {@code null}
     */
    @Override
    public Action apply(Player p) {
        return drawCardType.apply(p, drawCardType);
    }


    /**
     * Returns {@code 1} if this effect grants an upper-row draw, {@code 0}
     * otherwise.
     *
     * @return number of upper draws granted
     */
    public int getUpDraws() {
        return drawCardType.equals(DrawCardEnum.UP_DRAW) ? 1 : 0;
    }

    /**
     * Returns {@code 1} if this effect grants a lower-row draw, {@code 0}
     * otherwise.
     *
     * @return number of lower draws granted
     */
    public int getDownDraws() {
        return drawCardType.equals(DrawCardEnum.DOWN_DRAW) ? 1 : 0;
    }

    /**
     * Forwards this effect to the given {@link DrawCountVisitor}, contributing
     * its upper- or lower-row draw count to the visitor's running totals.
     *
     * @param visitor the visitor to accept; must not be {@code null}
     */
    public void accept(DrawCountVisitor visitor){ visitor.visit(this);}
}