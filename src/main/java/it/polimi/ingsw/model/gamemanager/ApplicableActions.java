package it.polimi.ingsw.model.gamemanager;

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
     */
    void onMoveRequested(String nick, int tilePos);

    /**
     * Called when a player requests to draw a specific card.
     *
     * @param nick   the nickname of the player requesting the draw
     * @param cardId the identifier of the card the player wants to draw
     */
    void onDrawCardRequested(String nick, int cardId);

    /**
     * Called when a player requests to skip their current pending action.
     *
     * @param nick the nickname of the player requesting the skip
     */
    void onSkipRequested(String nick);
}