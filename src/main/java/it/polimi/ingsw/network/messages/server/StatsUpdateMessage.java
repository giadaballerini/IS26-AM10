package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class StatsUpdateMessage implements ServerMessage {
    private final PlayerStatsDTO stats;
    private final int cardId;
    public StatsUpdateMessage(PlayerStatsDTO stats, int cardId) {
        this.stats = stats;
        this.cardId = cardId;
    }
    public PlayerStatsDTO getStats() {
        return stats;
    }
    public int getCardId() {
        return cardId;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
