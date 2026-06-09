package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client to register a nickname with the server and start a
 * session.
 *
 * <p>The server responds with either a
 * {@link it.polimi.ingsw.network.messages.server.LoginSuccessMessage} or a
 * {@link it.polimi.ingsw.network.messages.server.LoginFailedMessage}.
 */
public class LoginMessage implements ClientMessage {

    /** The nickname the player wants to register. */
    private final String nickname;

    /**
     * Creates a {@code LoginMessage} for the given nickname.
     *
     * @param nickname the desired player nickname
     */
    public LoginMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the nickname the player wants to register.
     *
     * @return desired nickname
     */
    public String getNickname() {
        return nickname;
    }
}