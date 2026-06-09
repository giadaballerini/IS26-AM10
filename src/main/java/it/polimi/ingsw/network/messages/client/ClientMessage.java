package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.io.Serializable;

/**
 * Marker interface for all messages sent from a client to the server over a
 * socket connection.
 *
 * <p>Each concrete implementation represents a distinct player action or
 * session request. The Visitor pattern is used for dispatch: the server-side
 * reader calls {@link #accept} with a {@link ClientMessageVisitor}, which
 * then invokes the appropriate {@code visit} overload.
 */
public interface ClientMessage extends Serializable {

    /**
     * Accepts a {@link ClientMessageVisitor} and dispatches to the correct
     * {@code visit} overload for this message type.
     *
     * @param visitor the visitor that will process this message
     */
    void accept(ClientMessageVisitor visitor);
}