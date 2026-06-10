package it.polimi.ingsw.client.ui;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.network.dto.*;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract for the client-side user interface, abstracting over the
 * TUI and GUI implementations. Methods in this interface are called by the
 * {@link it.polimi.ingsw.client.Client} in response to server notifications or
 * local events, and are responsible for updating the display accordingly.
 */
public interface UserInterface {

    /**
     * Renders the full board state, typically called upon connection or reconnection
     * to restore the player's view.
     */
    void showBoard();

    /**
     * Updates the display to reflect that a player has placed their pawn on a board tile.
     *
     * @param tile       the updated state of the tile that was just occupied
     * @param currPlayer the nickname of the player who placed their pawn
     */
    void onMoveUpdate(TileDTO tile, String currPlayer);

    /**
     * Updates the display to reflect that the current active player has changed.
     *
     * @param nickname the nickname of the player whose turn it now is
     */
    void onCurrPlayerUpdate(String nickname);

    /**
     * Updates the display to reflect that the current game phase has changed.
     *
     * @param phaseDTO the new game phase
     */
    void onPhaseUpdate(PhaseDTO phaseDTO);

    /**
     * Updates the display to reflect that a player has drawn a card.
     *
     * @param c        the card that was drawn
     * @param nickname the nickname of the player who drew the card
     */
    void onDrawUpdate(CardDTO c, String nickname);

    /**
     * Updates the display to reflect that a player's pawn has been returned to the
     * queue tile, delivering the updated tile state and player stats.
     *
     * @param tileDTO        the updated state of the board tile the player left
     * @param playerStatsDTO the updated stats of the player who returned to the queue
     */
    void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO);

    /**
     * Updates the display to reflect that the game has advanced to a new age.
     *
     * @param age the new age number
     */
    void onChangeAge(int age);

    /**
     * Updates the display to reflect changes to a player's stats.
     *
     * @param playerStatsDTO the updated stats of the player
     */
    void onStatsUpdate(PlayerStatsDTO playerStatsDTO);

    /**
     * Updates the display to reflect changes to the local player's status flags
     * (such as active bonuses, discounts, and special effects).
     *
     * @param playerStatusDTO the updated status of the local player
     */
    void onStatusUpdate(PlayerStatusDTO playerStatusDTO);

    /**
     * Displays the list of currently available lobbies, grouped by capacity.
     *
     * @param lobbies a map from maximum player count to the list of lobbies with that capacity
     */
    void displayLobbies(Map<Integer, List<LobbyDTO>> lobbies);

    /**
     * Displays an error message derived from the given exception.
     *
     * @param e the exception whose message is to be displayed
     */
    void printError(Exception e);

    /**
     * Called when the local player successfully logs in, triggering the transition
     * to the lobby selection or creation screen.
     *
     * @param nickname the nickname that was accepted by the server
     */
    void onLogin(String nickname);

    /**
     * Called when the server confirms that the player's requested game was created.
     *
     * @param id the unique identifier of the newly created game
     */
    void onCreate(int id);

    /**
     * Called when the server confirms that the player has successfully joined a lobby.
     *
     * @param id the unique identifier of the lobby that was joined
     */
    void onJoin(int id);

    /**
     * Displays the global leaderboard following a ranking request.
     *
     * @param ranks a map from each player to their cumulative Prestige Points
     *              in the global leaderboard
     */
    void showLeaderboard(Map<PlayerDTO, Integer> ranks);

    /**
     * Displays the end game screen with the final standings and the local player's
     * match and global ranking positions.
     *
     * @param stats            the final stats of all players at the end of the match
     * @param rankingPos       the local player's final position in the match ranking
     * @param globalRankingPos the local player's position in the global leaderboard
     *                         after the match
     */
    void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos);

    /**
     * Notifies the UI that draw actions are available for the current turn,
     * prompting the player to draw a card.
     */
    void showDrawable();

    /**
     * Notifies the UI that the current draw phase has been completed,
     * allowing the display to update accordingly.
     */
    void showCompletedDraw();

    /**
     * Starts the UI event loop. For the TUI, this begins reading input from the terminal;
     * for the GUI, this launches the JavaFX application.
     */
    void start();

    /**
     * Notifies the UI that a player has skipped their optional draw action.
     *
     * @param nickname the nickname of the player who skipped
     */
    void notifySkip(String nickname);

    /**
     * Displays the event screen showing the cards that triggered at the end of the round
     * and their effects on each player's stats.
     *
     * @param events the event data for the round, including triggered cards and updated stats
     * @param stats  the stats of all players before the events were applied,
     *               used to compute before/after deltas
     */
    void onEvent(EventDTO events, List<PlayerStatsDTO> stats);

    /**
     * Handles the player quitting the application. Cleans up the UI state and
     * returns the current {@link VirtualModel} for any final processing.
     *
     * @return the current {@link VirtualModel} at the time of quitting
     */
    VirtualModel quit();

    /**
     * Handles the player exiting the current match, returning them to the lobby screen
     * without terminating the application.
     */
    void exit();

    /**
     * Displays the list of available commands to the player.
     */
    void displayHelpMessage();

    /**
     * Displays detailed information about the card with the given ID.
     *
     * @param cardId the unique identifier of the card to display information for
     */
    void info(int cardId);

    /**
     * Called when the server notifies the client that it has been disconnected,
     * showing the given reason and returning the player to the main menu.
     *
     * @param reason a message describing the reason for the disconnection
     */
    void onQuit(String reason);

    /**
     * Called when the server crashes or becomes unreachable, showing an appropriate
     * error message and returning the player to the main menu.
     */
    void onServerCrash();

    /**
     * Injects the {@link Client} instance into the UI, allowing it to dispatch
     * commands in response to player input.
     *
     * @param client the client to associate with this UI
     */
    void setClient(Client client);

    /**
     * Called when the client successfully reconnects to an ongoing match,
     * restoring the board view to the current game state.
     *
     * @param matchId the ID of the match the client has reconnected to
     */
    void reconnect(int matchId);

    /**
     * Displays the global leaderboard filtered by the number of players in the
     * most recently played match.
     *
     * @param stringIntegerMap a map from each player's nickname to their cumulative
     *                         Prestige Points in the global leaderboard
     */
    void showRanking(Map<String, Integer> stringIntegerMap);

    /**
     * Displays the status screen showing the active flags of all players
     * in the current match.
     */
    void showStatusScreen();
}