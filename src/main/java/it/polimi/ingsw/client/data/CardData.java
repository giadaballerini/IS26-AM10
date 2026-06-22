package it.polimi.ingsw.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.CrafterSymbolEnum;
/**
 * Represents the static data associated with a game card, as loaded from the
 * client-side JSON resource file. Each instance holds the display and gameplay
 * attributes of a card (such as type, age, cost, and effects), which vary depending
 * on the card type. Optional fields are {@code null} when not applicable to a
 * given card type and default to {@code 0} or {@code false} through their getters.
 *
 * @see CardRegistry
 */
public class CardData {
    /**
     * The unique identifier of this card.
     */
    @JsonProperty("id")
    private int id;
    /**
     * The type of this card, determining its gameplay role and applicable fields.
     */
    @JsonProperty("type")
    private CardTypeEnum type;
    /**
     * The text shown on the card.
     */
    @JsonProperty("caption")
    private String caption;
    /**
     * The age (1–3) this card belongs to.
     */
    @JsonProperty("age")
    private int age;
    /**
     * Food cost to buy the building. {@code null} if the card is not a {@link CardTypeEnum#BUILDING}
     */
    @JsonProperty("cost")
    private Integer cost = null;
    /**
     * Prestige Points value of the card. {@code null} if the card type does not grant prestige points.
     */
    @JsonProperty("PP")
    private Integer PP = null;

    /**
     * Food gained when the card's effect triggers. {@code null} if the card does not grant food.
     */
    @JsonProperty("food")
    private Integer food = null;

    /**
     * Whether the hunter has the mark. {@code null} if not a {@link CardTypeEnum#HUNTER}.
     */
    @JsonProperty("mark")
    private Boolean mark = null;
    /**
     * The crafter symbol associated with this card. {@code null} if not a {@link CardTypeEnum#CRAFTER} card.
     */
    @JsonProperty("symbol")
    private CrafterSymbolEnum symbol = null;
    /**
     * Food discount granted by this card. {@code null} if not applicable.
     */
    @JsonProperty("foodDiscount")
    private Integer foodDiscount = null;

    /**
     * The unique identifier of this card.
     * @return the unique identifier of this card
     */
    public int getId() {
        return id;
    }

    /**
     * The type of this card.
     * @return the type of this card
     */
    public CardTypeEnum getType() {
        return type;
    }

    /**
     * The display text shown on the card.
     * @return the display text shown on the card
     */
    public String getDescription() {
        return caption;
    }

    /**
     * The age this card belongs to.
     * @return the age this card belongs to
     */
    public int getAge() {
        return age;
    }

    /**
     * The food cost to buy this building, or {@code 0} if not applicable.
     * @return the food cost to buy this building, or {@code 0} if not applicable
     */
    public int getCost() {
        return cost != null ? cost : 0;
    }

    /**
     * The Prestige Points value of this card, or {@code 0} if not applicable.
     * @return the Prestige Points value of this card, or {@code 0} if not applicable
     */
    public int getPp() {
        return PP != null ? PP : 0;
    }

    /**
     * The food gained when this card's effect triggers, or {@code 0} if not applicable.
     * @return the food gained when this card's effect triggers, or {@code 0} if not applicable
     */
    public int getFood() {
        return food != null ? food : 0;
    }

    /**
     * Whether the hunter has the mark. {@code null} if not a {@link CardTypeEnum#HUNTER}.
     *
     * @return {@code true} if the hunter has the mark, {@code false} otherwise
     */
    public boolean isMark() {
        return mark != null && mark;
    }

    /**
     * The crafter symbol associated with this card, or {@code null} if not a crafter card.
     * @return the {@link CrafterSymbolEnum} associated with this card, or {@code null} if not a crafter card
     */
    public CrafterSymbolEnum getSymbol() {
        return symbol;
    }

    /**
     * The food discount granted by this card, or {@code 0} if not applicable.
     * @return the food discount granted by this card, or {@code 0} if not applicable
     */
    public int getFoodDiscount() {
        return foodDiscount != null ? foodDiscount : 0;
    }

    /**
     * Returns the resource name of the card back image, determined by the card's type and age.
     * Age-3 {@link CardTypeEnum#FEAST} and {@link CardTypeEnum#RITUAL} cards share a single
     * final-age back image, building cards use an age-specific building back, and all other
     * cards use a standard age-specific back.
     *
     * @return the resource name of the appropriate card back image
     */
    public String getBackImagePath() {
        if (age == 3 && (type.equals(CardTypeEnum.FEAST) || type.equals(CardTypeEnum.RITUAL))) {
            return "Back_card_final";
        } else if (type.equals(CardTypeEnum.BUILDING)) {
            return "Back_build_" + age;
        } else
            return "Back_card_" + age;
    }
    /** No-arg constructor required by Jackson for JSON deserialization. */
    private CardData() { }
}
