package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.List;

/**
 * Sent by the server to each client when the game ends, carrying the final
 * statistics and the recipient player's ranking positions.
 */
public class GameEndingUpdateMessage implements ServerMessage {

    /** Final statistics for all players in the match. */
    private final List<PlayerStatsDTO> stats;

    /** The recipient player's position in the match ranking (1-based). */
    private final int rankingPos;

    /** The recipient player's position in the global leaderboard (1-based). */
    private final int globalRankingPos;

    /**
     * Creates a {@code GameEndingUpdateMessage} for the given player.
     *
     * @param stats           final statistics for all players
     * @param rankingPos      recipient's position in the match ranking
     * @param globalRankingPos recipient's position in the global leaderboard
     */
    public GameEndingUpdateMessage(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        this.stats = stats;
        this.rankingPos = rankingPos;
        this.globalRankingPos = globalRankingPos;
    }

    /**
     * Returns the final statistics for all players.
     *
     * @return player statistics list
     */
    public List<PlayerStatsDTO> getStats() {
        return stats;
    }

    /**
     * Returns the recipient player's position in the match ranking.
     *
     * @return match ranking position (1-based)
     */
    public int getRankingPos() {
        return rankingPos;
    }

    /**
     * Returns the recipient player's position in the global leaderboard.
     *
     * @return global leaderboard position (1-based)
     */
    public int getGlobalRankingPos() {
        return globalRankingPos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}