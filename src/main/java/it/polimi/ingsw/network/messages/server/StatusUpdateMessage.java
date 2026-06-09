package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatusDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server when a player's status flags change.
 */
public class StatusUpdateMessage implements ServerMessage {

    /** Updated status flags for the affected player. */
    private final PlayerStatusDTO status;

    /**
     * Creates a {@code StatusUpdateMessage} carrying the new status.
     *
     * @param status updated player status
     */
    public StatusUpdateMessage(PlayerStatusDTO status) {
        this.status = status;
    }

    /**
     * Returns the updated player status.
     *
     * @return player status DTO
     */
    public PlayerStatusDTO getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}