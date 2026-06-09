package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server when a player's statistics change.
 */
public class StatsUpdateMessage implements ServerMessage {

    /** Updated statistics for the affected player. */
    private final PlayerStatsDTO stats;

    /** ID of the card whose effect triggered the statistics change. */
    private final int cardId;

    /**
     * Creates a {@code StatsUpdateMessage} for the given stats and card.
     *
     * @param stats  updated player statistics
     * @param cardId ID of the triggering card
     */
    public StatsUpdateMessage(PlayerStatsDTO stats, int cardId) {
        this.stats = stats;
        this.cardId = cardId;
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
     * Returns the ID of the card whose effect triggered this update.
     *
     * @return triggering card ID
     */
    public int getCardId() {
        return cardId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}