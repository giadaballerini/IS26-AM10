package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.Map;

/**
 * Sent by the server in response to a
 * {@link it.polimi.ingsw.network.messages.client.RankingRequestMessage},
 * carrying the global leaderboard: the cumulative score each player has
 * accumulated across all matches played with the same number of players.
 */
public class RankingResponseMessage implements ServerMessage {

    private static final long serialVersionUID = 1L;

    /** Map from player nickname to their total cumulative score in the global leaderboard. */
    private final Map<String, Integer> ranking;

    /**
     * Creates a {@code RankingResponseMessage} with the given global leaderboard.
     *
     * @param ranking map from nickname to cumulative score
     */
    public RankingResponseMessage(Map<String, Integer> ranking) {
        this.ranking = ranking;
    }

    /**
     * Returns the global leaderboard as a map from nickname to cumulative score.
     *
     * @return global ranking map
     */
    public Map<String, Integer> getRanking() {
        return ranking;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}