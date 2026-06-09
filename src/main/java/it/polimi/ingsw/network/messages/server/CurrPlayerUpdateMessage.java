package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Broadcast by the server to all clients when the active player changes.
 */
public class CurrPlayerUpdateMessage implements ServerMessage {

    /** Nickname of the player whose turn it now is. */
    private final String nickname;

    /**
     * Creates a {@code CurrPlayerUpdateMessage} for the new active player.
     *
     * @param nickname nickname of the new current player
     */
    public CurrPlayerUpdateMessage(String nickname) {
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the nickname of the player whose turn it now is.
     *
     * @return active player's nickname
     */
    public String getNickname() {
        return nickname;
    }
}