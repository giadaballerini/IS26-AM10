package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.ProtectPPEnum;
import it.polimi.ingsw.model.player.Player;

/**
 * An instant effect that activates prestige point loss protection on the
 * owning player.
 *
 * <p>Once applied, the player is shielded from PP deductions during rituals
 * for the remainder of the game. The protection rule is delegated to
 * {@link #protectPpType}.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProtectPP extends CardEffectInstant {

    /** The protection rule that activates PP loss immunity on the player. */
    private final ProtectPPEnum protectPpType;

    /**
     * Constructs a {@code ProtectPP} effect.
     *
     * @param protectPpType the rule that activates PP protection;
     *                      must not be {@code null}
     */
    @JsonCreator
    public ProtectPP(@JsonProperty("protectPpType") ProtectPPEnum protectPpType) {
        this.protectPpType = protectPpType;
    }

    /**
     * Activates PP loss protection on the given player, delegating to
     * {@link ProtectPPEnum#apply(Player)}.
     *
     * @param p the player to protect; must not be {@code null}
     */
    @Override
    public void apply(Player p) {
        protectPpType.apply(p);
    }



    /**
     * Returns whether this effect is consumed after firing once, as determined
     * by the protection rule.
     *
     * @return {@code true} if the effect is one-time
     */
    @Override
    public boolean isOneTime() { return protectPpType.isOneTime(); }
}