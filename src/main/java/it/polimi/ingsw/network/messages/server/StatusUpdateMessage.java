package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatusDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class StatusUpdateMessage implements ServerMessage {
    private final PlayerStatusDTO status;

    public StatusUpdateMessage(PlayerStatusDTO status){
        this.status = status;
    }
    public PlayerStatusDTO getStatus() {
        return status;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

}
