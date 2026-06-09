package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to the active player when they become eligible to draw
 * one or more cards, carrying the updated action budget.
 */
public class NotifyDrawableMessage implements ServerMessage {

    /** The updated action budget indicating how many draws are available. */
    private final ActionsDTO actionsDTO;

    /**
     * Creates a {@code NotifyDrawableMessage} with the given action budget.
     *
     * @param actionsDTO updated actions available for the current turn
     */
    public NotifyDrawableMessage(ActionsDTO actionsDTO) {
        this.actionsDTO = actionsDTO;
    }

    /**
     * Returns the updated action budget.
     *
     * @return actions DTO
     */
    public ActionsDTO getActionsDTO() {
        return actionsDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}