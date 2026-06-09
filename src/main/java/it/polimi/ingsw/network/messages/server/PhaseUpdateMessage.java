package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PhaseDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Broadcast by the server to all clients when the game transitions to a new
 * phase.
 */
public class PhaseUpdateMessage implements ServerMessage {

    /** The new game phase. */
    private final PhaseDTO phase;

    /**
     * Creates a {@code PhaseUpdateMessage} carrying the new phase.
     *
     * @param phase the new game phase
     */
    public PhaseUpdateMessage(PhaseDTO phase) {
        this.phase = phase;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the new game phase.
     *
     * @return phase DTO
     */
    public PhaseDTO getPhase() {
        return this.phase;
    }
}