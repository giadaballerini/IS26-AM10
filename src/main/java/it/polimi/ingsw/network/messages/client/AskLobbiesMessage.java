package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client to request the list of currently available game lobbies.
 *
 * <p>The server responds with an
 * {@link it.polimi.ingsw.network.messages.server.AvailableLobbiesMessage}.
 */
public class AskLobbiesMessage implements ClientMessage {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }
}