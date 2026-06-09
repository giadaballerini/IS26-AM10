package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server when a login request is rejected.
 *
 * <p>Upon receiving this message, the client completes its login future with
 * {@code false} so the player can choose a different nickname.
 */
public class LoginFailedMessage implements ServerMessage {

    /** Human-readable explanation of why the login was rejected. */
    private final String error;

    /**
     * Creates a {@code LoginFailedMessage} with the given error description.
     *
     * @param error reason for the login failure
     */
    public LoginFailedMessage(String error) {
        this.error = error;
    }

    /**
     * Returns the reason why the login was rejected.
     *
     * @return error description
     */
    public String getError() {
        return this.error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}