package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to acknowledge a
 * {@link it.polimi.ingsw.network.messages.client.QuitMessage}, confirming
 * that the player has been removed from the match and providing a reason or
 * summary message.
 */
public class QuitAckMessage implements ServerMessage {

    /** Human-readable message explaining or confirming the quit. */
    private final String reason;

    /**
     * Creates a {@code QuitAckMessage} with the given reason.
     *
     * @param reason explanation or confirmation of the quit
     */
    public QuitAckMessage(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the reason or confirmation message for the quit.
     *
     * @return quit reason
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}