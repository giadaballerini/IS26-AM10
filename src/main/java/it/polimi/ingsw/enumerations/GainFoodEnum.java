package it.polimi.ingsw.enumerations;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.character.Crafter;
import it.polimi.ingsw.model.interfaces.GainFoodModifier;
import it.polimi.ingsw.model.interfaces.GainFoodVisitor;
import it.polimi.ingsw.model.player.Player;

/**
 * Represents the available food gain effects that can be applied to a player.
 *
 * <p>Each constant defines a specific food gain strategy by implementing
 * {@link GainFoodModifier}. Some constants also implement {@link GainFoodVisitor}
 * for effects that depend on the type of card being played.</p>
 */
public enum GainFoodEnum implements GainFoodModifier, GainFoodVisitor {

    /**
     * Grants food when the player completes a new set of all character types.
     * Checks whether the played card increases the number of complete sets.
     */
    FOOD_FOR_SET((p, e, c) -> {
        int newNSets = Integer.MAX_VALUE;
        int oldNSets = Integer.MAX_VALUE;
        for (CardTypeEnum type : CardTypeEnum.values()) {
            if (type.isCharacter()) {
                newNSets = Math.min(newNSets, p.getNumType(type));
                if (type.equals(c.getType()))
                    oldNSets = Math.min(oldNSets, p.getNumType(type) - 1);
                else
                    oldNSets = Math.min(oldNSets, p.getNumType(type));
            }
        }
        if (newNSets > oldNSets)
            p.addFood(e.getFoodAmount());
    }),

    /**
     * Grants food based on the number of identical crafters the player has collected.
     * Food is awarded when the player reaches an even count of identical crafter cards, even the symbol.
     */
    FOOD_FOR_CRAFTER((p, e, c) -> {}) {
        @Override
        public void apply(Player p, GainFood gainFood, Card c) {
            c.accept(this, p, gainFood);
        }

        @Override
        public void visit(Crafter c, Player p, GainFood gainFood) {
            CrafterSymbolEnum symbol = c.getSymbol();
            int count = p.getNumSymbolsForCrafter(symbol);
            if (count != 0 && count % 2 == 0)
                p.addFood(gainFood.getFoodAmount());
        }
    },

    /**
     * Activates a food bonus for the hunters during a hunt event.
     * This is a one-time effect, to activate the player's passive effect.
     */
    FOOD_FOR_HUNTER_HUNT((p, e, c) -> {
        p.activateHuntBonus();
    }) { public boolean isOneTime() { return true; } },

    /**
     * Activates a food bonus for the painters during a paint event.
     * This is a one-time effect, to activate the player's passive effect.
     */
    FOOD_FOR_ARTIST_PAINT((p, e, c) -> {
        p.activatePaintBonus();
    }) { public boolean isOneTime() { return true; } },

    /**
     * Grants a flat amount of food to the player.
     * This is a one-time effect.
     */
    FOOD_FLAT((p, e, c) -> {
        p.addFood(e.getFoodAmount());
    }) {
        public boolean isOneTime() { return true; }
        public void apply(Player p, GainFood gainFood) {
            p.addFood(gainFood.getFoodAmount());
        }
    },

    /**
     * Activates an extra food bonus on the player's queue.
     * This is a one-time effect, to activate the player's passive effect.
     */
    FOOD_EXTRA((p, e, c) -> {
        p.activateExtraFoodOnQueue();
    }) { public boolean isOneTime() { return true; } };

    /** The strategy used to apply this food gain effect to a player. */
    private final GainFoodModifier modifier;

    /**
     * Creates a new {@code GainFoodEnum} constant with the given strategy.
     *
     * @param modifier the {@link GainFoodModifier} that defines how the food gain effect is applied
     */
    GainFoodEnum(GainFoodModifier modifier) {
        this.modifier = modifier;
    }

    /**
     * Applies the food gain effect to the given player using the provided card context.
     *
     * @param p the player to apply the effect to
     * @param effect the {@link GainFood} effect containing the food amount
     * @param c the card that triggered the effect
     */
    @Override
    public void apply(Player p, GainFood effect, Card c) {
        modifier.apply(p, effect, c);
    }

    /**
     * Applies the food gain effect to the given player without a card context.
     *
     * @param p the player to apply the effect to
     * @param gainFood the {@link GainFood} effect containing the food amount
     */
    public void apply(Player p, GainFood gainFood) {
        modifier.apply(p, gainFood);
    }

    /**
     * Returns whether this effect is applied only once.
     *
     * @return {@code false} by default; overridden to {@code true} in one-time constants
     */
    public boolean isOneTime() { return false; }
}
