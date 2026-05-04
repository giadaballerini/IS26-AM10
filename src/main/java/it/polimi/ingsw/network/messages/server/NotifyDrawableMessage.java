package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class NotifyDrawableMessage implements ServerMessage {
    private final ActionsDTO actionsDTO;

    public NotifyDrawableMessage(ActionsDTO actionsDTO){
        this.actionsDTO = actionsDTO;
    }
    public ActionsDTO getActionsDTO() {
        return actionsDTO;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
