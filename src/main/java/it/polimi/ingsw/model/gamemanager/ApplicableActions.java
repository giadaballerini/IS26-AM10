package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.exceptions.*;

/**
 * Exposes the three player-driven actions that the {@link GameManager} can
 * receive during a match.
 *
 * <p>Implemented by {@link GameManager} and used by the
 * {@link it.polimi.ingsw.controller.Controller} as its sole entry point into
 * the game logic, keeping the controller decoupled from the full
 * {@code GameManager} logic.</p>
 */
public interface ApplicableActions {

    /**
     * Called when a player requests to place their pawn on a board tile.
     *
     * @param nick    the nickname of the player making the move
     * @param tilePos the position of the tile the player wants to move to
     * @throws OccupiedTileException   if the target tile is already occupied
     * @throws InvalidPhaseException   if the game is not in the correct phase
     * @throws InvalidPlayerException  if the player making the move is not the current player
     * @throws InvalidMoveException    if the move is not valid for the current state
     */
    void onMoveRequested(String nick, int tilePos) throws OccupiedTileException, InvalidPhaseException, InvalidPlayerException, InvalidMoveException;

    /**
     * Called when a player requests to draw a specific card.
     *
     * @param nick   the nickname of the player requesting the draw
     * @param cardId the identifier of the card the player wants to draw
     * @throws InvalidPhaseException  if the game is not in the correct phase
     * @throws InvalidPlayerException if the player making the draw is not the current player
     * @throws InvalidDrawException   if the card cannot be drawn
     */
    void onDrawCardRequested(String nick, int cardId) throws InvalidPhaseException, InvalidPlayerException, InvalidDrawException;

    /**
     * Called when a player requests to skip their current pending action.
     *
     * @param nick the nickname of the player requesting the skip
     * @throws InvalidPhaseException  if the game is not in the correct phase
     * @throws InvalidPlayerException if the player making the skip is not the current player
     * @throws InvalidSkipException   if the current action cannot be skipped
     */
    void onSkipRequested(String nick) throws InvalidPhaseException, InvalidPlayerException, InvalidSkipException;
}