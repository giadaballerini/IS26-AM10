package it.polimi.ingsw.visitors;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.messages.client.DrawMessage;
import it.polimi.ingsw.network.messages.client.MoveMessage;
import it.polimi.ingsw.network.messages.client.SkipMessage;

/**
 * Visitor for in-game action messages sent by the client.
 *
 * <p>Defines the visit methods for the three actions a player can perform
 * during their turn: moving a pawn, drawing a card, and skipping the draw phase.
 */
public interface GameMessageVisitor {

    /**
     * Visits a move message, handling the player's pawn placement on the board.
     *
     * @param moveMessage the message containing the target tile index
     * @throws InvalidMoveException   if the move is not valid
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if a move is not allowed in the current phase
     * @throws OccupiedTileException  if the target tile is already occupied
     */
    void visit(MoveMessage moveMessage)
            throws InvalidMoveException, InvalidPlayerException,
            InvalidPhaseException, OccupiedTileException;

    /**
     * Visits a draw message, handling the player's card draw action.
     *
     * @param drawMessage the message containing the ID of the card to draw
     * @throws InvalidDrawException   if the card cannot be drawn
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if drawing is not allowed in the current phase
     */
    void visit(DrawMessage drawMessage)
            throws InvalidDrawException, InvalidPlayerException, InvalidPhaseException;

    /**
     * Visits a skip message, handling the player's decision to skip
     * the optional draw phase.
     *
     * @param skipMessage the skip message sent by the client
     * @throws InvalidSkipException   if skipping is not allowed
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if skipping is not allowed in the current phase
     */
    void visit(SkipMessage skipMessage)
            throws InvalidSkipException, InvalidPlayerException, InvalidPhaseException;
}