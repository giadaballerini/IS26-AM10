package it.polimi.ingsw.model.entities.card.types.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * An event card that triggers a feast, requiring each player to feed their
 * village characters.
 *
 * <p>When resolved, each player must pay {@link #foodCost} food for each
 * character in their village, reduced by their accumulated feast discount.
 * For each unit of cost they cannot cover with food, they lose
 * {@link #ppCost} prestige points instead.</p>
 */
public class Feast extends Event {

    /** Food deducted per unpaid character during the feast. */
    private final int foodCost;

    /** PP deducted per character when the player has no food left to pay. */
    private final int ppCost;

    /**
     * Constructs a {@code Feast} event from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase during which this event is resolved
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param foodCost           food cost per character (before discounts)
     * @param ppCost             PP penalty per character when the player cannot pay food
     * @param type               the card type (always {@link CardTypeEnum#FEAST})
     */
    @JsonCreator
    public Feast(@JsonProperty("id") int id,
                 @JsonProperty("trigger") GamePhaseEnum trigger,
                 @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                 @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                 @JsonProperty("age") int age,
                 @JsonProperty("foodCost") int foodCost,
                 @JsonProperty("ppCost") int ppCost,
                 @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.foodCost = foodCost;
        this.ppCost = ppCost;
    }

    /**
     * Resolves the feast for all players.
     *
     * <p>For each player, the total cost is the number of characters in their
     * village minus their feast discount. For each unit of that cost, one food
     * is deducted if the player has any; otherwise one PP is deducted.</p>
     *
     * @param players the list of all players; must not be {@code null}
     * @param phase   the current game phase (unused in this event)
     */
    @Override
    public void execEvent(List<Player> players, GamePhaseEnum phase) {
        for (Player playerRef : players) {
            int discount = playerRef.calculateFeastDiscount();
            int totalCost = playerRef.getNumCharacters() - discount;
            for (int k = 0; k < totalCost; k++) {
                if (playerRef.getNFood() > 0) {
                    playerRef.addFood(-this.foodCost);
                } else {
                    playerRef.addPP(-this.ppCost);
                }
            }
        }
    }
}