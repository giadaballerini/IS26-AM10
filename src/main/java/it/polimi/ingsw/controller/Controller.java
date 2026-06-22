package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.gamemanager.ApplicableActions;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.network.messages.client.DrawMessage;
import it.polimi.ingsw.network.messages.client.MoveMessage;
import it.polimi.ingsw.network.messages.client.SkipMessage;
import it.polimi.ingsw.visitors.GameMessageVisitor;

import java.util.Objects;

/**
 * Controller class that handles messages sent by clients during the game.
 *
 * <p>Implements the {@link GameMessageVisitor} interface using the Visitor pattern
 * to process different types of client messages and forward them
 * to the game model via {@link ApplicableActions}.</p>
 */
public class Controller implements GameMessageVisitor {

    /** The game manager used to apply actions to the game model. */
    private final ApplicableActions gameManager;

    /** The number of players in the game. */
    private final int numPlayers;

    /**
     * Creates a new {@code Controller} for the game session.
     *
     * @param gameManager the {@link GameManager} that handles game actions; cannot be {@code null}
     * @param numPlayers  the number of players in the game
     * @throws NullPointerException if {@code gameManager} is {@code null}
     */
    public Controller(GameManager gameManager, int numPlayers) {
        Objects.requireNonNull(gameManager, "GameManager non può essere null!");
        this.gameManager = gameManager;
        this.numPlayers = numPlayers;
    }

    /**
     * Handles a move request from a client.
     *
     * @param moveMessage the message containing the player and the tile position to move to
     * @throws InvalidMoveException   if the move is not valid
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if a move is not allowed in the current phase
     * @throws OccupiedTileException  if the target tile is already occupied
     */
    @Override
    public void visit(MoveMessage moveMessage)
            throws InvalidMoveException, InvalidPlayerException,
            InvalidPhaseException, OccupiedTileException {
        gameManager.onMoveRequested(moveMessage.getPlayer(), moveMessage.getTilePos());
    }

    /**
     * Handles a card draw request from a client.
     *
     * @param drawMessage the message containing the player's nickname and the card to draw
     * @throws InvalidDrawException   if the card cannot be drawn
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if drawing is not allowed in the current phase
     */
    @Override
    public void visit(DrawMessage drawMessage)
            throws InvalidDrawException, InvalidPlayerException, InvalidPhaseException {
        gameManager.onDrawCardRequested(drawMessage.getNickname(), drawMessage.getCardId());
    }

    /**
     * Handles a turn skip request from a client.
     *
     * @param skipMessage the message containing the nickname of the player skipping their turn
     * @throws InvalidSkipException   if skipping is not allowed
     * @throws InvalidPlayerException if it is not this player's turn
     * @throws InvalidPhaseException  if skipping is not allowed in the current phase
     */
    @Override
    public void visit(SkipMessage skipMessage)
            throws InvalidSkipException, InvalidPlayerException, InvalidPhaseException {
        gameManager.onSkipRequested(skipMessage.getNickname());
    }

    /**
     * Returns the number of players in the game.
     *
     * @return the number of players
     */
    public int getNumPlayers() {
        return numPlayers;
    }
}