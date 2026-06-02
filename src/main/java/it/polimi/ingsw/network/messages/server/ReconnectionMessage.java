package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class ReconnectionMessage implements ServerMessage{
    private final int matchId;
    public ReconnectionMessage(int matchId) {
        this.matchId = matchId;
    }
    public int getMatchId() {
        return matchId;
    }
    public void accept(ServerMessageVisitor visitor){
        visitor.visit(this);
    }
}
