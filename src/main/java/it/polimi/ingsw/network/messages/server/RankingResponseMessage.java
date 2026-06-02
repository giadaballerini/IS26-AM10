package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.Map;

public class RankingResponseMessage implements ServerMessage{
    private static final long serialVersionUID = 1L;
    private final Map<String, Integer> ranking;

    public RankingResponseMessage(Map<String, Integer> ranking) {
        this.ranking = ranking;
    }

    public Map<String, Integer> getRanking() { return ranking; }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
