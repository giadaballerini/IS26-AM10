package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client to request joining an existing game lobby.
 *
 * <p>The server responds with a
 * {@link it.polimi.ingsw.network.messages.server.GameJoinedMessage} on
 * success.
 */
public class JoinGameMessage implements ClientMessage {

    /** ID of the lobby the player wants to join. */
    private final int id;

    /** Nickname of the player joining the lobby. */
    private final String nickname;

    /**
     * Creates a {@code JoinGameMessage} for the given lobby and player.
     *
     * @param id       ID of the target lobby
     * @param nickname nickname of the joining player
     */
    public JoinGameMessage(int id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the ID of the lobby the player wants to join.
     *
     * @return target lobby ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the nickname of the joining player.
     *
     * @return player nickname
     */
    public String getNickname() {
        return nickname;
    }
}