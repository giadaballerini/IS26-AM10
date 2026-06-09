package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client when the player chooses to skip their remaining actions
 * for the current turn.
 *
 * <p>The server broadcasts a
 * {@link it.polimi.ingsw.network.messages.server.NotifySkipMessage} to all
 * clients.
 */
public class SkipMessage implements ClientMessage {

    /** Nickname of the player skipping their turn. */
    private final String nickname;

    /**
     * Creates a {@code SkipMessage} for the given player.
     *
     * @param nickname nickname of the skipping player
     */
    public SkipMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the nickname of the player skipping their turn.
     *
     * @return player nickname
     */
    public String getNickname() {
        return nickname;
    }
}