package it.polimi.ingsw.visitors;

import it.polimi.ingsw.network.messages.client.DrawMessage;
import it.polimi.ingsw.network.messages.client.MoveMessage;
import it.polimi.ingsw.network.messages.client.SkipMessage;

/**
 * Visitor for in-game action messages sent by the client.
 * <p>
 * Defines the visit methods for the three actions a player can perform
 * during their turn: moving a pawn, drawing a card, and skipping the draw phase.
 * </p>
 */
public interface GameMessageVisitor {

    /**
     * Visits a move message, handling the player's pawn placement on the board.
     *
     * @param moveMessage the message containing the target tile index
     */
    void visit(MoveMessage moveMessage);

    /**
     * Visits a draw message, handling the player's card draw action.
     *
     * @param drawMessage the message containing the ID of the card to draw
     */
    void visit(DrawMessage drawMessage);

    /**
     * Visits a skip message, handling the player's decision to skip
     * the optional draw phase.
     *
     * @param skipMessage the skip message sent by the client
     */
    void visit(SkipMessage skipMessage);
}