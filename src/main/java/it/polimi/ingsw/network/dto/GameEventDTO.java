package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.io.Serializable;

/**
 * Describes a single game event triggered by a card effect.
 *
 * <p>Carries all the information needed by the client to display the event
 * and apply the resulting state changes: which player was affected, which card
 * caused the effect, the updated statistics, the event category, and the new
 * game phase (if the phase changed as a consequence).
 */
public class GameEventDTO implements Serializable {

    /** Nickname of the player affected by this event. */
    String nickname;

    /** ID of the card that triggered this event. */
    int cardId;

    /** Player statistics after this event has been applied. */
    PlayerStatsDTO newStats;

    /** Category of the card effect. */
    CardTypeEnum eventType;

    /** New game phase resulting from this event, if the phase changed. */
    PhaseDTO newPhase;

    /**
     * Creates a {@code GameEventDTO} describing a single card-triggered event.
     *
     * @param nickname  nickname of the affected player
     * @param cardId    ID of the card that caused the event
     * @param newStats  player statistics after the event
     * @param eventType category of the card effect
     * @param newPhase  new game phase, or the current phase if unchanged
     */
    public GameEventDTO(String nickname, int cardId, PlayerStatsDTO newStats,
                        CardTypeEnum eventType, PhaseDTO newPhase) {
        this.nickname = nickname;
        this.cardId = cardId;
        this.newStats = newStats;
        this.eventType = eventType;
        this.newPhase = newPhase;
    }

    /**
     * Returns the nickname of the player affected by this event.
     *
     * @return affected player's nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the ID of the card that triggered this event.
     *
     * @return triggering card ID
     */
    public int getCardId() {
        return cardId;
    }

    /**
     * Returns the player statistics after this event has been applied.
     *
     * @return updated player statistics
     */
    public PlayerStatsDTO getNewStats() {
        return newStats;
    }

    /**
     * Returns the category of the card effect.
     *
     * @return event type
     */
    public CardTypeEnum getEventType() {
        return eventType;
    }

    /**
     * Returns the game phase resulting from this event.
     *
     * @return new (or unchanged) game phase
     */
    public PhaseDTO getNewPhase() {
        return newPhase;
    }
}