package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to a player who successfully joined an existing lobby,
 * confirming the match ID they have entered.
 */
public class GameJoinedMessage implements ServerMessage {

    /** The ID of the match the player has joined. */
    private final int id;

    /**
     * Creates a {@code GameJoinedMessage} for the given match ID.
     *
     * @param id the ID of the joined match
     */
    public GameJoinedMessage(int id) {
        this.id = id;
    }

    /**
     * Returns the ID of the match the player has joined.
     *
     * @return match ID
     */
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}