package it.polimi.ingsw.network.messages.server;


import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class QuitAckMessage implements ServerMessage{
    private final String reason;
    public QuitAckMessage(String reason) {
        this.reason = reason;
    }
    public String getReason(){
        return this.reason;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
