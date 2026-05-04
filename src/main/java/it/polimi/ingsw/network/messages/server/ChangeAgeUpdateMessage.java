package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.ChangeAgeDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class ChangeAgeUpdateMessage implements ServerMessage {

    private final ChangeAgeDTO changeAgeDTO;

    public ChangeAgeUpdateMessage(ChangeAgeDTO changeAgeDTO){
        this.changeAgeDTO = changeAgeDTO;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    public ChangeAgeDTO getAgeDTO(){
        return changeAgeDTO;
    }
}
