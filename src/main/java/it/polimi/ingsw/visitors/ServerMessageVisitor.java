package it.polimi.ingsw.visitors;

import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;

public interface ServerMessageVisitor {
    void visit(ChangeAgeUpdateMessage changeAgeUpdateMessage);
    void visit(CurrPlayerUpdateMessage currPlayerUpdateMessage);
    void visit(DrawUpdateMessage drawUpdateMessage);
    void visit(GameEndingUpdateMessage gameEndingUpdateMessage);
    void visit(MoveUpdateMessage moveUpdateMessage);
    void visit(NotifyDrawableMessage notifyDrawableMessage);
    void visit(NotifySkipMessage notifySkipMessage);
    void visit(PhaseUpdateMessage phaseUpdateMessage);
    void visit(RequestLeaderboardUpdateMessage requestLeaderboardUpdateMessage);
    void visit(ReturnToQueueUpdateMessage returnToQueueUpdateMessage);
    void visit(ShowBoardMessage showBoardMessage);
    void visit(StatusUpdateMessage statusUpdateMessage);
    void visit(StatsUpdateMessage statsUpdateMessage);
    void visit(PingMessage pingMessage);
    void visit(EventMessage eventMessage);
    void visit(AvailableLobbiesMessage avaiableLobbiesMessage);
    void visit(GameCreatedMessage gameCreatedMessage);
    void visit(ErrorMessage errorMessage);
}
