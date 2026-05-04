package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.io.Serializable;

public interface ClientMessage extends Serializable {
    void accept(ClientMessageVisitor visitor);
}
