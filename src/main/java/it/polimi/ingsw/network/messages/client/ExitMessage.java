package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client when the player terminates the session and disconnects
 * from the server.
 *
 * <p>Unlike {@link QuitMessage}, this message signals a full application exit:
 * the server removes the player entirely, and the socket is closed immediately
 * after sending.
 */
public class ExitMessage implements ClientMessage {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }
    /**
     * Creates a new {@code ExitMessage} instance.
     */
    public ExitMessage() {
    }
}