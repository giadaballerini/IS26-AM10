package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.EventDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class EventMessage implements ServerMessage{
    private final EventDTO event;
    public EventMessage(EventDTO event){
        this.event = event;
    }
    public EventDTO getEvent() {
        return event;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
