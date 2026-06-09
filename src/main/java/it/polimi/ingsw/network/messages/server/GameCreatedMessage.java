package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to the player who created a lobby, confirming the
 * creation and providing the assigned match ID.
 */
public class GameCreatedMessage implements ServerMessage {

    /** The unique identifier assigned to the newly created match. */
    private final int gameId;

    /**
     * Creates a {@code GameCreatedMessage} with the given match ID.
     *
     * @param gameId the ID assigned to the new match
     */
    public GameCreatedMessage(int gameId) {
        this.gameId = gameId;
    }

    /**
     * Returns the unique identifier of the newly created match.
     *
     * @return match ID
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}