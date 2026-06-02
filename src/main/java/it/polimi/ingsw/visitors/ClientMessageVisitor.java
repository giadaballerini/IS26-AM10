package it.polimi.ingsw.visitors;

import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.service.PongMessage;

public interface ClientMessageVisitor extends GameMessageVisitor{
    void visit(CreateGameMessage createGameMessage);
    void visit(JoinGameMessage joinGameMessage);
    void visit(LoginMessage loginMessage);
    void visit(AskLobbiesMessage askLobbiesMessage);
    void visit(PongMessage pongMessage);
    void visit(QuitMessage quitMessage);
    void visit(ExitMessage exitMessage);
    void visit(RankingRequestMessage  rankingRequestMessage);
    void setGameVisitor(GameMessageVisitor visitor);
}
