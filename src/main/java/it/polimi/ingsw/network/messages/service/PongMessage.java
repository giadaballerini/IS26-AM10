package it.polimi.ingsw.network.messages.service;

import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.visitors.ClientMessageVisitor;


public class PongMessage implements ClientMessage {
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }
}
