package it.polimi.ingsw.network.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bundles the outcome of a card-event phase for transmission to all clients.
 *
 * <p>Built by {@link it.polimi.ingsw.model.gamemanager.GameState#applyEvents}
 * at the end of each event phase (upper or lower card list depending on the
 * current {@link it.polimi.ingsw.enumerations.GamePhaseEnum}). It carries two
 * parallel pieces of information:
 *
 * <ul>
 *   <li>{@link #events} — the {@link CardDTO}s of every card whose effect
 *       fired during the phase, in application order.</li>
 *   <li>{@link #stats} — a post-application snapshot of every player's
 *       statistics, so the client can update its display in one round-trip.</li>
 * </ul>
 *
 * <p>Sent to clients via {@link it.polimi.ingsw.network.messages.server.EventMessage}.
 * If {@link #isEmpty()} returns {@code true} the phase produced no visible
 * effects and the message may be suppressed.
 */
public class EventDTO implements Serializable {
    /**
     *  Required by the {@link Serializable} interface.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Post-event statistics snapshot for every player in the game. */
    private final List<PlayerStatsDTO> stats;

    /** Cards whose effects fired during the event phase, in application order. */
    private final List<CardDTO> events;

    /**
     * Creates an empty {@code EventDTO}. Cards and statistics are added
     * incrementally by the event visitor and by
     * {@link it.polimi.ingsw.model.gamemanager.GameState#applyEvents}.
     */
    public EventDTO() {
        stats = new ArrayList<>();
        events = new ArrayList<>();
    }

    /**
     * Returns the post-event statistics snapshot for every player.
     *
     * @return mutable list of {@link PlayerStatsDTO}, one entry per player
     */
    public List<PlayerStatsDTO> getStats() {
        return stats;
    }

    /**
     * Returns the cards whose effects fired during the event phase.
     *
     * @return mutable list of {@link CardDTO}, in application order
     */
    public List<CardDTO> getEvents() {
        return events;
    }

    /**
     * Appends a player statistics snapshot to this bundle.
     *
     * @param stats the statistics to add
     */
    public void addStats(PlayerStatsDTO stats) {
        this.stats.add(stats);
    }

    /**
     * Appends a card whose effect fired during the event phase.
     *
     * @param lowerEvent the card to add
     */
    public void addEvents(CardDTO lowerEvent) {
        this.events.add(lowerEvent);
    }

    /**
     * Returns {@code true} if no card effects fired during the event phase.
     *
     * @return {@code true} when {@link #events} is empty
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }
}