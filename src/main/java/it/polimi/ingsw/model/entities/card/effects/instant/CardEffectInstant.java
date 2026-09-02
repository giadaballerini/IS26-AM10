package it.polimi.ingsw.model.entities.card.effects.instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.player.Player;

/**
 * Abstract base class for instant card effects.
 *
 * <p>An instant effect is applied immediately when its owning card's trigger
 * phase matches the current game phase. Effects may be permanent (applied every
 * time the trigger fires) or one-time (removed from the card after firing once).
 * The six concrete subclasses cover food discounts, food gains, prestige point
 * gains, star gains, PP loss protection, and PP loss on food shortage.</p>
 *
 * <p>Jackson uses the {@code type} property in the JSON to deserialize the
 * correct subclass.</p>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DiscountFood.class, name = "DISCOUNT_FOOD"),
        @JsonSubTypes.Type(value = GainFood.class,     name = "GAIN_FOOD"),
        @JsonSubTypes.Type(value = GainPP.class,       name = "GAIN_PP"),
        @JsonSubTypes.Type(value = GainStars.class,    name = "GAIN_STARS"),
        @JsonSubTypes.Type(value = ProtectPP.class,    name = "PROTECT_PP"),
        @JsonSubTypes.Type(value = LossIfBroke.class,  name = "LOSS_IF_BROKE")
})
public abstract class CardEffectInstant {

    /**
     * Applies this effect to the given player without a source card context.
     * The default implementation is a no-op; subclasses that do not need the
     * source card may override this method instead of
     * {@link #apply(Player, Card)}.
     *
     * @param p the player to apply the effect to; must not be {@code null}
     */
    public void apply(Player p) {}

    /**
     * Applies this effect to the given player with access to the source card.
     * The default implementation delegates to {@link #apply(Player)};
     * subclasses that require the source card should override this method.
     *
     * @param p the player to apply the effect to; must not be {@code null}
     * @param c the card that carries this effect; must not be {@code null}
     */
    public void apply(Player p, Card c) { apply(p); }

    /**
     * Applies this effect to the given player with both the owning card and
     * an external trigger context.
     *
     * <p>Use this overload when the card carrying the effect (e.g. a building)
     * differs from the card that semantically triggered it (e.g. the character
     * just drawn). Effects that depend on the trigger card — such as
     * {@code FOOD_FOR_SET} — should override this method; all others inherit
     * the default, which ignores {@code owner} and delegates to
     * {@link #apply(Player, Card)} passing {@code context}.</p>
     *
     * @param p       the player to apply the effect to; must not be {@code null}
     * @param owner   the card that carries this effect; must not be {@code null}
     * @param context the card that semantically triggered the effect
     *                (may differ from {@code owner}); must not be {@code null}
     */
    public void apply(Player p, Card owner, Card context) {
        apply(p, context);
    }

    /**
     * Returns the prestige point amount associated with this effect.
     * The default implementation returns {@code 0}.
     *
     * @return PP amount
     */
    public int getPpAmount() {
        return 0;
    }

    /**
     * Returns the food amount associated with this effect.
     * The default implementation returns {@code 0}.
     *
     * @return food amount
     */
    public int getFoodAmount() { return 0; }

    /**
     * Returns whether this effect should be removed from its card after firing
     * once. The default implementation returns {@code false}.
     *
     * @return {@code true} if the effect is consumed on first use
     */
    @JsonIgnore
    public abstract boolean isOneTime();
    /**
     * Creates a new {@code CardEffectInstant} instance.
     * Intended for use by subclasses only.
     */
    protected CardEffectInstant() {
    }
}