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
     * Prestige Points gained when the card effect triggers. {@code null} if not applicable.
     */
    @JsonProperty("ppGain")
    private Integer ppGain = null;
    /**
     * Prestige Points lost when a condition is met. {@code null} if not applicable.
     */
    @JsonProperty("ppLoss")
    private Integer ppLoss = null;
    /**
     * Food gained when the card effect triggers. {@code null} if not applicable.
     */
    @JsonProperty("food")
    private Integer food = null;
    /**
     * Number of painters threshold to gain PP during the Stone Painting event. {@code null} if not a {@link CardTypeEnum#STONE_PAINTING}.
     */
    @JsonProperty("thresh")
    private Integer thresh = null;
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
     * @return the unique identifier of this card
     */
    public int getId() {
        return id;
    }

    /**
     * @return the type of this card
     */
    public CardTypeEnum getType() {
        return type;
    }

    /**
     * @return the display text shown on the card
     */
    public String getDescription() {
        return caption;
    }

    /**
     * @return the age (1–3) this card belongs to
     */
    public int getAge() {
        return age;
    }

    /**
     * @return the food cost to buy this building, or {@code 0} if not applicable
     */
    public int getCost() {
        return cost != null ? cost : 0;
    }

    /**
     * @return the Prestige Points value of this card, or {@code 0} if not applicable
     */
    public int getPp() {
        return PP != null ? PP : 0;
    }

    /**
     * @return the Prestige Points gained when this card's effect triggers, or {@code 0} if not applicable
     */
    public int getPpGain() {
        return ppGain != null ? ppGain : 0;
    }

    /**
     * @return the Prestige Points lost when a condition is met, or {@code 0} if not applicable
     */
    public int getPpLoss() {
        return ppLoss != null ? ppLoss : 0;
    }

    /**
     * @return the food gained when this card's effect triggers, or {@code 0} if not applicable
     */
    public int getFood() {
        return food != null ? food : 0;
    }

    /**
     Number of painters threshold to gain PP during the Stone Painting event. {@code null} if not a {@link CardTypeEnum#STONE_PAINTING}.     */
    public int getThresh() {
        return thresh != null ? thresh : 0;
    }

    /**
     * Whether the hunter has the mark. {@code null} if not a {@link CardTypeEnum#HUNTER}.
     */
    public boolean isMark() {
        return mark != null && mark;
    }

    /**
     * @return the {@link CrafterSymbolEnum} associated with this card, or {@code null} if not a crafter card
     */
    public CrafterSymbolEnum getSymbol() {
        return symbol;
    }

    /**
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
}
