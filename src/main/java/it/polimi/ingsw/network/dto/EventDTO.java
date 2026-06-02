package it.polimi.ingsw.network.dto;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<PlayerStatsDTO> stats;
    private final List<CardDTO> events;

    public EventDTO(){
        stats = new ArrayList<>();
        events = new ArrayList<>();
    }
    public List<PlayerStatsDTO> getStats() {
        return stats;
    }
    public List<CardDTO> getEvents() {
        return events;
    }

    public void addStats(PlayerStatsDTO stats) {
        this.stats.add(stats);
    }

    public void addEvents(CardDTO lowerEvent) {
        this.events.add(lowerEvent);
    }
    public boolean isEmpty(){
        return events.isEmpty();
    }
}
