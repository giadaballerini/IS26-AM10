package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.interfaces.ProtectPPModifier;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the PP protection effect that can be applied to a player.
 *
 * <p>Implements {@link ProtectPPModifier} to activate a shield that prevents
 * the player from losing PP, ONLY from Ritual events. This is a one-time effect.</p>
 */
public enum ProtectPPEnum implements ProtectPPModifier {

    /**
     * Activates PP protection for the player.
     */
    PP_PROTECTION(Player::activatePpProtection);

    /** The strategy used to apply this PP protection effect to a player. */
    private final ProtectPPModifier modifier;

    /**
     * Creates a new {@code ProtectPPEnum} constant with the given strategy.
     *
     * @param modifier the {@link ProtectPPModifier} that defines how the PP protection effect is applied
     */
    ProtectPPEnum(ProtectPPModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the PP protection effect to the given player.
     *
     * @param p the player to apply the protection to
     */
    @Override
    public void apply(Player p) {
        modifier.apply(p);
    }

    /**
     * Returns whether this effect is applied only once.
     *
     * @return {@code true}, as this effect is always one-time
     */
    public boolean isOneTime() { return true; }
}
