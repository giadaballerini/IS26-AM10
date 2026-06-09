package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to a client that has reconnected to an ongoing match,
 * providing the match ID so the client can restore its state.
 */
public class ReconnectionMessage implements ServerMessage {

    /** ID of the match the client is being reconnected to. */
    private final int matchId;

    /**
     * Creates a {@code ReconnectionMessage} for the given match.
     *
     * @param matchId ID of the match to reconnect to
     */
    public ReconnectionMessage(int matchId) {
        this.matchId = matchId;
    }

    /**
     * Returns the ID of the match the client is being reconnected to.
     *
     * @return match ID
     */
    public int getMatchId() {
        return matchId;
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}