package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.interfaces.GainPPModifier;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the available prestige points (PP) gain effects that can be applied to a player.
 *
 * <p>Each constant defines a specific PP gain strategy by implementing {@link GainPPModifier}.
 * Some effects depend on the player's card composition or passives.</p>
 */
public enum GainPPEnum implements GainPPModifier {

    /**
     * Grants PP based on the number of cards of a specific category the player owns.
     */
    PP_FOR_CAT((p, e, c) -> p.addPP(e.getPpAmount() * p.getNumType(e.getCat()))),

    /**
     * Grants PP when the player completes a new set of all character types.
     * Checks whether the newly drawn card increases the number of complete sets.
     */
    PP_FOR_SET((p, e, c) -> {
        if (p.completesNewCharacterSet(c.getType()))
            p.addPP(e.getPpAmount());
    }),

    /**
     * Grants a flat amount of PP to the player.
     * This is a one-time effect.
     */
    PP_FLAT((p, e, c) -> p.addPP(e.getPpAmount())) { public boolean isOneTime() { return true; } },

    /**
     * Activates the shaman's double PP passive for the player.
     */
    DOUBLE_PP_SHAMAN((p, e, c) -> p.activateDoubleShaman()),

    /**
     * Grants PP equal to the player's current builder points.
     */
    DOUBLE_BUILDER((p, e, c) -> p.addPP(p.getBuilderPoints()));


    /** The strategy used to apply this PP gaining effect to a player. */
    private final GainPPModifier modifier;

    /**
     * Creates a new {@code GainPPEnum} constant with the given strategy.
     *
     * @param modifier the {@link GainPPModifier} that defines how the PP gain effect is applied
     */
    GainPPEnum(GainPPModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the PP gain effect to the given player using the provided card context.
     *
     * @param p the player to apply the effect to
     * @param effect the {@link GainPP} effect containing the PP amount
     * @param c the card that triggered the effect
     */
    @Override
    public void apply(Player p, GainPP effect, Card c) {
        modifier.apply(p, effect, c);
    }

    /**
     * Returns whether this effect is applied only once.
     *
     * @return {@code false} by default; overridden to {@code true} in one-time constants
     */
    public boolean isOneTime() { return false; }
}
