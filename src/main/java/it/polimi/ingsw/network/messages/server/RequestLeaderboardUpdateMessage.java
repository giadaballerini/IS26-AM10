package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.Map;

/**
 * Sent by the server after the game ends with the global leaderboard, mapping
 * each player to their overall position across all matches.
 */
public class RequestLeaderboardUpdateMessage implements ServerMessage {

    /** Map from player DTO to their global leaderboard position (1-based). */
    private final Map<PlayerDTO, Integer> ranks;

    /**
     * Creates a {@code RequestLeaderboardUpdateMessage} with the given ranks.
     *
     * @param ranks map from player DTO to global leaderboard position
     */
    public RequestLeaderboardUpdateMessage(Map<PlayerDTO, Integer> ranks) {
        this.ranks = ranks;
    }

    /**
     * Returns the global leaderboard as a map from player to position.
     *
     * @return leaderboard map
     */
    public Map<PlayerDTO, Integer> getRanks() {
        return ranks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}