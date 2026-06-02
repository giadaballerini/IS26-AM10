package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.Map;

public class RequestLeaderboardUpdateMessage implements ServerMessage {
    private final Map<PlayerDTO, Integer> ranks;
    public RequestLeaderboardUpdateMessage(Map<PlayerDTO, Integer> ranks) {
        this.ranks = ranks;
    }
    public Map<PlayerDTO, Integer> getRanks() {
        return ranks;
    }


    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
