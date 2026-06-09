package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Broadcast by the server to all clients when a player skips their remaining
 * actions for the current turn.
 */
public class NotifySkipMessage implements ServerMessage {

    /** Nickname of the player who skipped. */
    private final String nickname;

    /**
     * Creates a {@code NotifySkipMessage} for the given player.
     *
     * @param nickname nickname of the player who skipped
     */
    public NotifySkipMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the nickname of the player who skipped their turn.
     *
     * @return skipping player's nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}