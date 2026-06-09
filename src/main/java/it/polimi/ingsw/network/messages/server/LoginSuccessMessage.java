package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to confirm that a login request was accepted.
 *
 * <p>Upon receiving this message, the client completes its login future and
 * activates the ping timeout on the socket.
 */
public class LoginSuccessMessage implements ServerMessage {

    /** The nickname that was successfully registered. */
    private final String nickname;

    /**
     * Creates a {@code LoginSuccessMessage} for the given nickname.
     *
     * @param nickname the nickname that was accepted
     */
    public LoginSuccessMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the nickname that was successfully registered.
     *
     * @return accepted nickname
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ServerMessageVisitor message) {
        message.visit(this);
    }
}