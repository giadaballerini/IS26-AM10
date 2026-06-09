package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client to request the creation of a new game lobby.
 *
 * <p>The server responds with a
 * {@link it.polimi.ingsw.network.messages.server.GameCreatedMessage} carrying
 * the assigned match ID.
 */
public class CreateGameMessage implements ClientMessage {

    /** Number of players the lobby should accommodate. */
    private final int numPlayers;

    /** Nickname of the player creating the lobby. */
    private final String nickname;

    /**
     * Creates a {@code CreateGameMessage} for the given player and lobby size.
     *
     * @param numPlayers number of players for the new lobby
     * @param nickname   nickname of the player creating the lobby
     */
    public CreateGameMessage(int numPlayers, String nickname) {
        this.numPlayers = numPlayers;
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the number of players for the new lobby.
     *
     * @return player capacity
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Returns the nickname of the player creating the lobby.
     *
     * @return creator's nickname
     */
    public String getNickname() {
        return nickname;
    }
}