package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

import java.util.List;
import java.util.Map;

/**
 * Sent by the server in response to an
 * {@link it.polimi.ingsw.network.messages.client.AskLobbiesMessage}, carrying
 * the current list of open game lobbies.
 */
public class AvailableLobbiesMessage implements ServerMessage {

    /**
     * Map from player capacity to the list of open lobbies with that capacity.
     * The map is empty when no joinable lobbies exist at the time of the request.
     */
    private final Map<Integer, List<LobbyDTO>> lobbies;

    /**
     * Creates an {@code AvailableLobbiesMessage} with the given lobby map.
     *
     * @param lobbies map from player capacity to the corresponding lobby list
     */
    public AvailableLobbiesMessage(Map<Integer, List<LobbyDTO>> lobbies) {
        this.lobbies = lobbies;
    }

    /**
     * Returns the available lobbies grouped by player capacity.
     *
     * @return lobby map
     */
    public Map<Integer, List<LobbyDTO>> getLobbies() {
        return lobbies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}