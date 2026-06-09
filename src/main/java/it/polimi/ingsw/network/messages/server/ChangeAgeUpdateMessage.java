package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.ChangeAgeDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Broadcast by the server to all clients when the game advances to a new age,
 * carrying the refreshed card rows and deck size.
 */
public class ChangeAgeUpdateMessage implements ServerMessage {

    /** Data describing the new age, including updated card rows and deck size. */
    private final ChangeAgeDTO changeAgeDTO;

    /**
     * Creates a {@code ChangeAgeUpdateMessage} carrying the given age data.
     *
     * @param changeAgeDTO data for the new age
     */
    public ChangeAgeUpdateMessage(ChangeAgeDTO changeAgeDTO) {
        this.changeAgeDTO = changeAgeDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the data describing the new age.
     *
     * @return change-age DTO
     */
    public ChangeAgeDTO getAgeDTO() {
        return changeAgeDTO;
    }
}