package it.polimi.ingsw.network.messages.service;

import it.polimi.ingsw.network.messages.server.ServerMessage;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class PingMessage implements ServerMessage {
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
