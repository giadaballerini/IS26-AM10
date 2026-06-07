package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.io.Serializable;

public class GameEventDTO implements Serializable {
    String nickname;
    int cardId;
    PlayerStatsDTO newStats;
    CardTypeEnum eventType;
    PhaseDTO newPhase;
    public GameEventDTO (String nickname, int cardId, PlayerStatsDTO newStats, CardTypeEnum eventType, PhaseDTO newPhase){
        this.nickname = nickname;
        this.cardId = cardId;
        this.newStats = newStats;
        this.eventType = eventType;
        this.newPhase = newPhase;
    }

    public String getNickname() {
        return nickname;
    }

    public int getCardId() {
        return cardId;
    }

    public PlayerStatsDTO getNewStats() {
        return newStats;
    }

    public CardTypeEnum getEventType() {
        return eventType;
    }

    public PhaseDTO getNewPhase() {
        return  newPhase;
    }
}
