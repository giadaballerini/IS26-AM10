package it.polimi.ingsw.visitors;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.EventDTO;

import java.util.List;

/**
 * Visitor that executes event cards during the event phase of a round.
 * <p>
 * Implements the Visitor pattern on {@link CardVisitor}: visits event cards
 * and executes their effects on all players. Feast events ({@link CardTypeEnum#FEAST})
 * are deferred and executed last via {@link #feastIfPresent()}, ensuring they
 * are always resolved after all other events.
 * </p>
 */
public class PlayEventVisitor implements CardVisitor {

    /**
     * The feast event encountered during visitation, if any.
     * Kept separate to guarantee it is executed after all other events.
     */
    Event feastEvent = null;

    /** The list of players on which event effects are applied. */
    private final List<Player> players;

    /** The current game phase, passed to each event for context-dependent effects. */
    private final GamePhaseEnum currPhase;

    /** The DTO accumulating the events executed during this visitation. */
    private final EventDTO events;

    /**
     * Constructs a new {@code PlayEventVisitor}.
     *
     * @param players   the list of players affected by the events
     * @param currPhase the current game phase
     * @param events    the DTO to populate with the executed events
     */
    public PlayEventVisitor(List<Player> players, GamePhaseEnum currPhase, EventDTO events) {
        this.players = players;
        this.currPhase = currPhase;
        this.events = events;
    }

    /**
     * Visits an {@link Event} card: if it is a feast event, it is stored for
     * deferred execution; otherwise it is executed immediately and added to the
     * event DTO.
     *
     * @param e the event card to process
     */
    public void visit(Event e) {
        if (e.getType() == CardTypeEnum.FEAST) {
            feastEvent = e;
        } else {
            e.execEvent(players, currPhase);
            events.addEvents(e.toDTO());
        }
    }

    /**
     * Executes the feast event if one was encountered during visitation.
     * This method should be called after all other events have been visited,
     * to ensure the feast is resolved last.
     */
    public void feastIfPresent() {
        if (feastEvent != null) {
            feastEvent.execEvent(players, currPhase);
            events.addEvents(feastEvent.toDTO());
        }
    }

}