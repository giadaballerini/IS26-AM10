package it.polimi.ingsw.network.messages.service;

import it.polimi.ingsw.network.messages.server.ServerMessage;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server at regular intervals to verify that the client is still
 * reachable.
 *
 * <p>The client must respond with a {@link PongMessage} within the configured
 * timeout; failure to do so is treated as a disconnection.
 */
public class PingMessage implements ServerMessage {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
    /**
     * Creates a new {@code PingMessage} instance.
     */
    public PingMessage() {
    }
}