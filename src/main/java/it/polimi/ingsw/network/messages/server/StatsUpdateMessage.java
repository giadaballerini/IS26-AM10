package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server when a player's statistics change.
 */
public class StatsUpdateMessage implements ServerMessage {

    /** Updated statistics for the affected player. */
    private final PlayerStatsDTO stats;


    /**
     * Creates a {@code StatsUpdateMessage} for the given stats and card.
     *
     * @param stats  updated player statistics
     */
    public StatsUpdateMessage(PlayerStatsDTO stats) {
        this.stats = stats;
    }

    /**
     * Returns the updated player statistics.
     *
     * @return player stats DTO
     */
    public PlayerStatsDTO getStats() {
        return stats;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}