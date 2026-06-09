package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.interfaces.InteractiveEffectModifier;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the available card draw modes in the game.
 *
 * <p>Each constant defines whether a card is drawn from the upper or the lower list,
 * and implements {@link InteractiveEffectModifier} to produce the corresponding {@link Action},
 * using the Strategy pattern.</p>
 */
public enum DrawCardEnum implements InteractiveEffectModifier {

    /** Draws a card from the lower list. */
    DOWN_DRAW(Action::new),

    /** Draws a card from the upper list. */
    UP_DRAW(Action::new);

    /** The strategy used to apply this draw action to a player. */
    private final InteractiveEffectModifier modifier;

    /**
     * Creates a new {@code DrawCardEnum} constant with the given strategy.
     *
     * @param modifier the {@link InteractiveEffectModifier} that defines how the draw action is applied
     */
    DrawCardEnum(InteractiveEffectModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the draw action for the given player returning the resulting {@link Action}.
     *
     * @param p the player performing the draw
     * @param drawCard the draw mode to apply
     * @return the {@link Action} produced by this draw
     */
    @Override
    public Action apply(Player p, DrawCardEnum drawCard) {
        return modifier.apply(p, this);
    }
}