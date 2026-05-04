package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class QuitMessage implements ClientMessage{

    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }
}
