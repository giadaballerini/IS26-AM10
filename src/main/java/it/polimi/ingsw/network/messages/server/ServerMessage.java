package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.io.Serializable;

/**
 * Marker interface for all messages sent from the server to a client over a
 * socket connection.
 *
 * <p>Each concrete implementation represents a distinct server-side event or
 * response. The Visitor pattern is used for dispatch: the client-side reader
 * calls {@link #accept} with a {@link ServerMessageVisitor}, which then
 * invokes the appropriate {@code visit} overload.
 */
public interface ServerMessage extends Serializable {

    /**
     * Accepts a {@link ServerMessageVisitor} and dispatches to the correct
     * {@code visit} overload for this message type.
     *
     * @param visitor the visitor that will process this message
     */
    void accept(ServerMessageVisitor visitor);
}