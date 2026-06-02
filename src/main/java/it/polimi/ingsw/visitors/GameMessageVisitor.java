package it.polimi.ingsw.visitors;

import it.polimi.ingsw.network.messages.client.DrawMessage;
import it.polimi.ingsw.network.messages.client.MoveMessage;
import it.polimi.ingsw.network.messages.client.SkipMessage;

public interface GameMessageVisitor {
    void visit(MoveMessage moveMessage);
    void visit(DrawMessage drawMessage);
    void visit(SkipMessage skipMessage);
}
