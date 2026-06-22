package it.polimi.ingsw.observer;

import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.visitors.ClientMessageVisitor;
import it.polimi.ingsw.visitors.GameMessageVisitor;

import java.util.List;

/**
 * Observer interface through which the model (and server-side logic) notifies
 * a connected player of game events.
 *
 * <p>Each method corresponds to a specific type of update. Implementations are
 * responsible for forwarding the notification to the client over the network,
 * updating a local virtual model, or both.</p>
 */
public interface ModelObserver {

    /**
     * Returns the nickname of the player associated with this observer.
     *
     * @return the player's nickname; never {@code null}
     */
    String getNickname();

    /**
     * Delivers an inbound {@link ClientMessage} to this observer for processing.
     *
     * @param m the message received from the client; must not be {@code null}
     */
    void onClientMessage(ClientMessage m);

    /**
     * Sets the visitor used to dispatch incoming {@link ClientMessage} instances.
     *
     * @param visitor the visitor to use; must not be {@code null}
     */
    void setVisitor(ClientMessageVisitor visitor);

    /**
     * Notifies the observer that the active player has changed.
     *
     * @param nickname nickname of the player who is now taking their turn
     */
    void onCurrPlayerUpdate(String nickname);

    /**
     * Notifies the observer that a tile has been placed on the board.
     *
     * @param tile       the tile that was placed
     * @param nextPlayer nickname of the player who will act next
     */
    void onMoveUpdate(TileDTO tile, String nextPlayer);

    /**
     * Notifies the observer that the game phase has changed.
     *
     * @param phaseDTO descriptor of the new game phase
     */
    void onPhaseUpdate(PhaseDTO phaseDTO);

    /**
     * Notifies the observer that the game has ended, providing final
     * statistics and the player's ranking positions.
     *
     * @param stats             final statistics for all players
     * @param rankingPos        the observer's finishing position in this match
     * @param globalRankingPos  the observer's position in the global ranking
     */
    void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos);

    /**
     * Notifies the observer that a card has been drawn.
     *
     * @param c        the card that was drawn
     * @param nickname nickname of the player who drew the card
     */
    void onDrawUpdate(CardDTO c, String nickname);

    /**
     * Notifies the observer of an update to the player's status.
     *
     * @param status the updated player status
     */
    void onStatusUpdate(PlayerStatusDTO status);

    /**
     * Notifies the observer of an update to a player's statistics,
     * triggered by a specific card effect.
     *
     * @param stats  the updated statistics for the player
     */
    void onStatsUpdate(PlayerStatsDTO stats);

    /**
     * Notifies the observer that a player has skipped their action.
     *
     * @param nickname nickname of the player who skipped
     */
    void notifySkip(String nickname);

    /**
     * Notifies the observer of the draw actions currently available to the player.
     *
     * @param actions descriptor of the drawable actions
     */
    void notifyDrawable(ActionsDTO actions);

    /**
     * Notifies the observer that a player has been sent back to the queue,
     * providing the updated board tile and the player's updated statistics.
     *
     * @param tileDTO        the queue tile affected by the return to queue
     * @param playerStatsDTO updated statistics of the player who was sent back
     */
    void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO);

    /**
     * Sends the observer a full snapshot of the current board state.
     *
     * @param boardDTO the current board state
     */
    void showBoard(BoardDTO boardDTO);

    /**
     * Notifies the observer that the game has advanced to a new age.
     *
     * @param dto descriptor carrying the details of the age change
     */
    void onChangeAge(ChangeAgeDTO dto);

    /**
     * Notifies the observer that a game event has occurred
     *
     * @param event the event that occurred
     */
    void onEvent(EventDTO event);

    /**
     * Notifies the observer that another player in the lobby or match has quit.
     *
     * @param reason human-readable explanation of why the player left
     */
    void onQuitServer(String reason);

    /**
     * Notifies the observer that the player has successfully reconnected to
     * an ongoing match.
     *
     * @param matchId identifier of the match the player has rejoined
     */
    void onReconnection(int matchId);

    /**
     * Injects a {@link GameMessageVisitor} into this observer.
     *
     * <p>The default implementation is a no-op; observers that need to handle
     * game-specific messages should override this method.</p>
     *
     * @param visitor the visitor to inject
     */
    default void injectGameVisitor(GameMessageVisitor visitor) {}

    /**
     * Resets the injected {@link GameMessageVisitor}, removing any game-specific
     * message handling from this observer.
     *
     * <p>The default implementation is a no-op.</p>
     */
    default void resetGameVisitor() {}
}