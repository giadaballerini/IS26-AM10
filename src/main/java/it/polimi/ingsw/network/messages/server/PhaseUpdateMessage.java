package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PhaseDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class PhaseUpdateMessage implements ServerMessage {

    private final PhaseDTO phase;

    public PhaseUpdateMessage(PhaseDTO phase) {
        this.phase = phase;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    public PhaseDTO getPhase(){
        return this.phase;
    }
}
