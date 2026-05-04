package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.List;

public class GameEndingUpdateMessage implements ServerMessage {
    private final List<PlayerStatsDTO> stats;
    private final int rankingPos;
    public GameEndingUpdateMessage(List<PlayerStatsDTO> stats, int rankingPos) {
        this.stats = stats;
        this.rankingPos = rankingPos;
    }
    public List<PlayerStatsDTO> getStats() {
        return stats;
    }
    public int getRankingPos() {
        return rankingPos;
    }

    // TODO non è ancora stato fatto nemmeno per rmi, fare brainstorming su come

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
