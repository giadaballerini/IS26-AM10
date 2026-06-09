package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.EventDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server when one or more card effects are resolved during a turn,
 * carrying the triggered events and the resulting player statistics.
 */
public class EventMessage implements ServerMessage {

    /** Bundle of card events and updated player statistics. */
    private final EventDTO event;

    /**
     * Creates an {@code EventMessage} wrapping the given event bundle.
     *
     * @param event the event bundle to deliver
     */
    public EventMessage(EventDTO event) {
        this.event = event;
    }

    /**
     * Returns the event bundle carried by this message.
     *
     * @return event DTO
     */
    public EventDTO getEvent() {
        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}