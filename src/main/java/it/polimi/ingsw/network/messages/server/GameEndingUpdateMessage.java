package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.List;

public class GameEndingUpdateMessage implements ServerMessage {
    private final List<PlayerStatsDTO> stats;
    private final int rankingPos;
    private final int globalRankingPos;
    public GameEndingUpdateMessage(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        this.stats = stats;
        this.rankingPos = rankingPos;
        this.globalRankingPos = globalRankingPos;
    }
    public List<PlayerStatsDTO> getStats() {
        return stats;
    }
    public int getRankingPos() {
        return rankingPos;
    }
    public int getGlobalRankingPos() {
        return globalRankingPos;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
