package it.polimi.ingsw.model.entities.card.effects.interactive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;

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
     * Prints a confirmation message indicating that a draw action was created.
     */
    @Override
    public void displayEffect() {
        System.out.printf("Pescata effettuata");
    }

    /**
     * Returns {@code 1} if this effect grants an upper-row draw, {@code 0}
     * otherwise.
     *
     * @return number of upper draws granted
     */
    @Override
    public int getUpDraws() {
        return drawCardType.equals(DrawCardEnum.UP_DRAW) ? 1 : 0;
    }

    /**
     * Returns {@code 1} if this effect grants a lower-row draw, {@code 0}
     * otherwise.
     *
     * @return number of lower draws granted
     */
    @Override
    public int getDownDraws() {
        return drawCardType.equals(DrawCardEnum.DOWN_DRAW) ? 1 : 0;
    }
}