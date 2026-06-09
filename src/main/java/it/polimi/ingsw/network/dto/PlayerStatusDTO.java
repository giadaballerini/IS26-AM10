package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

/**
 * Carries the boolean status flags and active discounts for a single player.
 *
 * <p>Sent by the server whenever any of these flags changes so that the client
 * can update the relevant UI indicators.
 */
public class PlayerStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The player these status flags belong to. */
    private final String nickname;

    /** Whether the player currently has an active hunting bonus. */
    private boolean huntBonus;

    /**
     * Set of card categories for which the player has an active cost discount.
     * Immutable after construction.
     */
    private final Set<CardTypeEnum> categoryDiscounts;

    /** Whether the player has the paint special flag active. */
    private boolean paintFlag;

    /** Whether the player has the extra-action flag active. */
    private boolean extraFlag;

    /** Whether the player is currently protected from negative card effects. */
    private boolean hasProtection;

    /** Whether the player is currently receiving double income from shaman cards. */
    private boolean hasDoubleShamanIncome;

    /**
     * Creates a fully specified {@code PlayerStatusDTO}.
     *
     * @param nickname               the player's nickname
     * @param hasProtection          {@code true} if the player is protected
     * @param hasDoubleShamanIncome  {@code true} if the player has double shaman income
     * @param extraFlag              {@code true} if the extra-action flag is active
     * @param paintFlag              {@code true} if the paint flag is active
     * @param categoryDiscounts      set of card categories with an active discount
     * @param huntBonus              {@code true} if the hunting bonus is active
     */
    public PlayerStatusDTO(String nickname, boolean hasProtection, boolean hasDoubleShamanIncome,
                           boolean extraFlag, boolean paintFlag,
                           Set<CardTypeEnum> categoryDiscounts, boolean huntBonus) {
        this.nickname = nickname;
        this.hasProtection = hasProtection;
        this.hasDoubleShamanIncome = hasDoubleShamanIncome;
        this.categoryDiscounts = Set.copyOf(categoryDiscounts);
        this.extraFlag = extraFlag;
        this.paintFlag = paintFlag;
        this.huntBonus = huntBonus;
    }

    /**
     * Creates a default {@code PlayerStatusDTO} with all flags set to
     * {@code false} and no active discounts.
     *
     * @param nickname the player's nickname
     */
    public PlayerStatusDTO(String nickname) {
        this.nickname = nickname;
        this.hasProtection = false;
        this.hasDoubleShamanIncome = false;
        this.categoryDiscounts = EnumSet.noneOf(CardTypeEnum.class);
        this.extraFlag = false;
        this.paintFlag = false;
    }

    /**
     * Returns whether the player currently has an active hunting bonus.
     *
     * @return {@code true} if the hunting bonus is active
     */
    public boolean isHuntBonus() {
        return huntBonus;
    }

    /**
     * Returns whether the player has an active discount for the given card
     * category.
     *
     * @param type the card category to check
     * @return {@code true} if a discount exists for that category
     */
    public boolean hasDiscountFor(CardTypeEnum type) {
        return categoryDiscounts.contains(type);
    }

    /**
     * Returns the immutable set of card categories for which the player has
     * an active cost discount.
     *
     * @return set of discounted card categories
     */
    public Set<CardTypeEnum> getCategoryDiscounts() {
        return categoryDiscounts;
    }

    /**
     * Returns whether the paint special flag is currently active for the player.
     *
     * @return {@code true} if the paint flag is active
     */
    public boolean isPaintFlag() {
        return paintFlag;
    }

    /**
     * Returns whether the extra-action flag is currently active for the player.
     *
     * @return {@code true} if the extra flag is active
     */
    public boolean isExtraFlag() {
        return extraFlag;
    }

    /**
     * Returns whether the player is currently protected from negative card
     * effects.
     *
     * @return {@code true} if protection is active
     */
    public boolean hasProtection() {
        return hasProtection;
    }

    /**
     * Returns whether the player is currently receiving double income from
     * shaman cards.
     *
     * @return {@code true} if double shaman income is active
     */
    public boolean hasDoubleShamanIncome() {
        return hasDoubleShamanIncome;
    }

    /**
     * Returns the nickname of the player these status flags belong to.
     *
     * @return player nickname
     */
    public String getNickname() {
        return nickname;
    }
}