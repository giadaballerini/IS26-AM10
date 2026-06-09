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
 * An event card that rewards players for the hunters in their village.
 *
 * <p>When resolved in the correct phase, each player gains food and prestige
 * points proportional to the number of {@link CardTypeEnum#HUNTER} cards in
 * their village. The hunt bonus is also activated on each player, causing
 * future hunter-related bonuses to apply for the rest of the game.</p>
 */
public class Hunt extends Event {

    /** Prestige points gained per hunter during this event. */
    private final int ppGain;

    /** Food gained per hunter during this event. */
    private final int foodGain;

    /**
     * Constructs a {@code Hunt} event from its JSON properties.
     *
     * @param id                 unique card identifier
     * @param trigger            the game phase during which this event is resolved
     * @param interactiveEffects interactive effects carried by this card
     * @param instantEffects     instant effects carried by this card
     * @param age                the age this card belongs to (1–3)
     * @param ppGain             PP awarded per hunter
     * @param foodGain           food awarded per hunter
     * @param type               the card type (always {@link CardTypeEnum#HUNT})
     */
    @JsonCreator
    public Hunt(@JsonProperty("id") int id,
                @JsonProperty("trigger") GamePhaseEnum trigger,
                @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                @JsonProperty("age") int age,
                @JsonProperty("ppGain") int ppGain,
                @JsonProperty("foodGain") int foodGain,
                @JsonProperty("type") CardTypeEnum type) {
        super(id, trigger, interactiveEffects, instantEffects, age, type);
        this.ppGain = ppGain;
        this.foodGain = foodGain;
    }

    /**
     * Resolves the hunt event for all players, but only if the current phase
     * matches this event's trigger.
     *
     * <p>Each player receives food and PP equal to their hunter count
     * multiplied by {@link #foodGain} and {@link #ppGain} respectively.
     * The hunt bonus is also activated on each player.</p>
     *
     * @param players the list of all players; must not be {@code null}
     * @param phase   the current game phase
     */
    @Override
    public void execEvent(List<Player> players, GamePhaseEnum phase) {
        if (phase == this.trigger) {
            for (Player player : players) {
                int hunters = player.getNumType(CardTypeEnum.HUNTER);
                player.addFood(hunters * foodGain);
                player.addPP(hunters * ppGain);
                player.applyHuntBonus();
            }
        }
    }
}