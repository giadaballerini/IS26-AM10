package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.io.Serializable;

public interface ServerMessage extends Serializable {
    void accept(ServerMessageVisitor visitor);
}
