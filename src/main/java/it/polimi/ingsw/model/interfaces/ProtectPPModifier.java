package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.player.Player;

/**
 * Strategy interface for activating prestige point loss protection on a player.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.ProtectPPEnum}, whose
 * only value calls {@link Player#activatePpProtection()}, shielding the player
 * from PP deductions during rituals.</p>
 */
public interface ProtectPPModifier {

    /**
     * Activates PP loss protection on the given player.
     *
     * @param p the player to protect; must not be {@code null}
     */
    void apply(Player p);
}