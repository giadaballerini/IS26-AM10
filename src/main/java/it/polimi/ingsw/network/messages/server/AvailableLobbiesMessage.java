package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.List;
import java.util.Map;

public class AvailableLobbiesMessage implements ServerMessage{
    private final Map<Integer, List<LobbyDTO>> lobbies;
    public AvailableLobbiesMessage(Map<Integer, List<LobbyDTO>> lobbies){
        this.lobbies = lobbies;
    }
    public Map<Integer, List<LobbyDTO>> getLobbies() {
        return lobbies;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
