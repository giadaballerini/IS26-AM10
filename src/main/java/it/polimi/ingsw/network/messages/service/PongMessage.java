package it.polimi.ingsw.network.messages.service;

import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client in response to a {@link PingMessage} to signal that the
 * connection is still alive.
 */
public class PongMessage implements ClientMessage {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }
    /**
     * Creates a new {@code PongMessage} instance.
     */
    public PongMessage() {
    }
}