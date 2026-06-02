package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class GameJoinedMessage implements ServerMessage{
    private final int id;
    public  GameJoinedMessage(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    @Override
    public void accept(ServerMessageVisitor visitor){
        visitor.visit(this);
    }
}
