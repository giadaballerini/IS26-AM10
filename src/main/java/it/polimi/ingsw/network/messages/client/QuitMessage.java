package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client when the player voluntarily leaves the current match
 * without closing the session.
 *
 * <p>The server acknowledges with a
 * {@link it.polimi.ingsw.network.messages.server.QuitAckMessage} and notifies
 * the remaining players.
 */
public class QuitMessage implements ClientMessage {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }
    /**
     * Creates a new {@code QuitMessage} instance.
     */
    public QuitMessage() {
    }
}