package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to relay a generic error message to the client.
 */
public class ErrorMessage implements ServerMessage {

    /** Human-readable description of the error. */
    private final String message;

    /**
     * Creates an {@code ErrorMessage} with the given description.
     *
     * @param message error description
     */
    public ErrorMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the error description.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}