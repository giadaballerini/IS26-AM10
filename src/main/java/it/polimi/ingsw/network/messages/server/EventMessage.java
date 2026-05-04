package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class EventMessage implements ServerMessage{
    private final String event;
    public EventMessage(String event){
        this.event = event;
    }
    public String getEvent() {
        return event;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
