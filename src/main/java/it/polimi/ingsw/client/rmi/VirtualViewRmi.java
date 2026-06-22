package it.polimi.ingsw.client.rmi;

import it.polimi.ingsw.network.dto.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface through which the server pushes game state updates to the client.
 * Each method corresponds to a specific game event or state change, and is invoked
 * by the server on the client's stub over RMI.
 */
@SuppressWarnings("EmptyMethod")
public interface VirtualViewRmi extends Remote {

    /**
     * Notifies the client that the current active player has changed.
     *
     * @param nickname the nickname of the player whose turn it now is
     * @throws RemoteException if a communication error occurs
     */
    void onCurrPlayerUpdate(String nickname) throws RemoteException;

    /**
     * Notifies the client that a player has placed their pawn on a board tile.
     *
     * @param tile       the updated state of the tile that was just occupied
     * @param nextPlayer the nickname of the player who moved their totem
     * @throws RemoteException if a communication error occurs
     */
    void onMoveUpdate(TileDTO tile, String nextPlayer) throws RemoteException;

    /**
     * Notifies the client that the current game phase has changed.
     *
     * @param phaseDTO the new game phase
     * @throws RemoteException if a communication error occurs
     */
    void onPhaseUpdate(PhaseDTO phaseDTO) throws RemoteException;

    /**
     * Notifies the client that the game has ended and delivers the final results.
     *
     * @param stats             the final stats of all players at the end of the match
     * @param rankingPos        the client's final position in the match ranking
     * @param globalRankingPos  the client's position in the global leaderboard after the match
     * @throws RemoteException if a communication error occurs
     */
    void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) throws RemoteException;

    /**
     * Notifies the client that a player has drawn a card.
     *
     * @param c        the card that was drawn
     * @param nickname the nickname of the player who drew the card
     * @throws RemoteException if a communication error occurs
     */
    void onDrawUpdate(CardDTO c, String nickname) throws RemoteException;

    /**
     * Delivers the current player's full status to the client,
     * including active bonuses, discounts, and special flags.
     *
     * @param status the updated status of the player
     * @throws RemoteException if a communication error occurs
     */
    void onStatusUpdate(PlayerStatusDTO status) throws RemoteException;

    /**
     * Notifies the client that a player's stats have changed after an effect.
     *
     * @param stats  the updated stats of the player
     * @throws RemoteException if a communication error occurs
     */
    void onStatsUpdate(PlayerStatsDTO stats) throws RemoteException;

    /**
     * Notifies the client that the game has advanced to a new age
     *
     * @param dto the age transition data, including the new upper and lower card lists
     *            and the updated deck size
     * @throws RemoteException if a communication error occurs
     */
    void onChangeAge(ChangeAgeDTO dto) throws RemoteException;

    /**
     * Notifies the client that a player has skipped their optional draw action.
     *
     * @param nickname the nickname of the player who skipped
     * @throws RemoteException if a communication error occurs
     */
    void notifySkip(String nickname) throws RemoteException;

    /**
     * Notifies the client that draw actions are available for the current turn,
     * along with the number of available up-draws and down-draws.
     *
     * @param actions the available draw actions for the current turn
     * @throws RemoteException if a communication error occurs
     */
    void notifyDrawable(ActionsDTO actions) throws RemoteException;

    /**
     * Notifies the client that a player's pawn has been returned to the queue tile
     * after leaving a board tile, and delivers the updated tile and player stats.
     *
     * @param tileDTO        the updated state of the board tile the player left
     * @param playerStatsDTO the updated stats of the player who returned to the queue
     * @throws RemoteException if a communication error occurs
     */
    void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) throws RemoteException;

    /**
     * Delivers the full board state to the client, typically upon connection or reconnection.
     *
     * @param boardDTO the complete current state of the board
     * @throws RemoteException if a communication error occurs
     */
    void showBoard(BoardDTO boardDTO) throws RemoteException;

    /**
     * Notifies the client that it has been disconnected from the server.
     *
     * @param reason a message describing the reason for the disconnection
     * @throws RemoteException if a communication error occurs
     */
    void onQuitServer(String reason) throws RemoteException;

    /**
     * Notifies the client that a game event has occurred, delivering the
     * triggered event cards and the resulting updated stats for all affected players.
     *
     * @param event the event data, including the triggered event cards and updated player stats
     * @throws RemoteException if a communication error occurs
     */
    void onEvent(EventDTO event) throws RemoteException;

    /**
     * Checks that the RMI connection to the client is still alive.
     * Called periodically by the server as a heartbeat mechanism.
     *
     * @throws RemoteException if the client is unreachable
     */
    void ping() throws RemoteException;

    /**
     * Notifies the client that it has been reconnected to an ongoing match,
     * and delivers the full board state to restore the client's view.
     *
     * @param matchId the ID of the match the client has reconnected to
     * @throws RemoteException if a communication error occurs
     */
    void reconnect(int matchId) throws RemoteException;
}