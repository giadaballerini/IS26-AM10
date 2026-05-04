package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.CardTypeEnum;

import java.util.List;

public class EventDTO {
    CardTypeEnum type;
    List<PlayerStatsDTO> stats;
    List<CardDTO> upper;
    List<CardDTO> lower;

    public String getType() {
        return type.toString();
    }
    public List<PlayerStatsDTO> getStats() {
        return stats;
    }
    public List<CardDTO> getUpper() {
        return upper;
    }
    public List<CardDTO> getLower() {
        return lower;
    }
}
