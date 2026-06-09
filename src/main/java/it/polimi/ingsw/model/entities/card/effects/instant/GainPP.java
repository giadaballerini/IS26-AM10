package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GainPPEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

/**
 * An instant effect that grants prestige points to the owning player.
 *
 * <p>The exact gain rule is determined by {@link #gainPpType}: it may award a
 * flat amount, an amount based on the number of cards of a given category in
 * the village, double the builder points, or activate the double shaman income
 * bonus. The {@link #ppAmount} and {@link #cat} fields carry the parameters
 * used by the chosen rule.</p>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GainPP extends CardEffectInstant {

    /** The base PP amount used as a parameter by the gain rule. */
    private final int ppAmount;

    /**
     * The card category relevant to category-based PP gain rules.
     * May be {@code null} for rules that do not depend on a category.
     */
    private final CardTypeEnum cat;

    /** The gain rule that determines how PP is calculated and awarded. */
    private final GainPPEnum gainPpType;

    /**
     * Constructs a {@code GainPP} effect.
     *
     * @param cat       the card category relevant to this gain rule;
     *                  may be {@code null}
     * @param ppAmount  the base PP amount used by the gain rule
     * @param gainPpType the rule determining how PP is calculated and awarded;
     *                  must not be {@code null}
     */
    @JsonCreator
    public GainPP(@JsonProperty("category") CardTypeEnum cat,
                  @JsonProperty("ppAmount") int ppAmount,
                  @JsonProperty("gainPpType") GainPPEnum gainPpType) {
        this.ppAmount = ppAmount;
        this.cat = cat;
        this.gainPpType = gainPpType;
    }

    /**
     * Applies the PP gain to the given player with access to the source card,
     * delegating to {@link GainPPEnum#apply(Player, GainPP, Card)}.
     *
     * @param p the player to apply the effect to; must not be {@code null}
     * @param c the card that carries this effect; must not be {@code null}
     */
    @Override
    public void apply(Player p, Card c) {
        gainPpType.apply(p, this, c);
    }

    /**
     * No-op overload retained for compatibility.
     */
    public void apply() {}

    /**
     * Prints the PP amount granted, used for debugging or TUI display.
     */
    @Override
    public void displayEffect() {
        System.out.println("\nAggiunti " + ppAmount + "PP");
    }

    /**
     * Returns the base PP amount used as a parameter by the gain rule.
     *
     * @return base PP amount
     */
    @Override
    public int getPpAmount() {
        return ppAmount;
    }

    /**
     * Returns the card category relevant to this gain rule.
     *
     * @return the category, or {@code null} if not applicable
     */
    public CardTypeEnum getCat() { return cat; }

    /**
     * Returns whether this effect is consumed after firing once, as determined
     * by the gain rule.
     *
     * @return {@code true} if the effect is one-time
     */
    @Override
    public boolean isOneTime() { return gainPpType != null && gainPpType.isOneTime(); }
}