package it.polimi.ingsw.network.server;

import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.network.dto.LobbyDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>Currently implemented by {@link it.polimi.ingsw.network.server.socket.ClientHandlerSocket}
 * for the socket transport layer.
 * Each method corresponds to a distinct player action or session
 * request; all input validation and game-state management are delegated to
 * {@link MatchManager}.
 */
public interface VirtualServer {

    /**
     * Registers a new client session under the given nickname.
     *
     * @param nickname   the desired player nickname
     * @param clientStub the RMI callback stub used to push events to the client
     */
    void login(String nickname, VirtualViewRmi clientStub);

    /**
     * Creates a new game lobby with the specified player capacity.
     *
     * @param nickname   nickname of the player creating the lobby
     * @param numPlayers number of players for the new match
     * @return the unique match ID assigned to the new lobby
     */
    int createGame(String nickname, int numPlayers);

    /**
     * Adds the player to an existing lobby identified by the given match ID.
     *
     * @param nickname nickname of the joining player
     * @param id       ID of the target lobby
     */
    void joinGame(String nickname, int id);

    /**
     * Places the player's pawn on the specified board tile.
     *
     * @param nickname nickname of the moving player
     * @param tileId   ID of the target tile
     */
    void move(String nickname, int tileId);

    /**
     * Draws the card on behalf of the player.
     *
     * @param id       index of the card or deck to draw
     * @param nickname nickname of the drawing player
     */
    void draw(int id, String nickname);

    /**
     * Skips the player's remaining actions for the current turn.
     *
     * @param nickname nickname of the skipping player
     */
    void skip(String nickname);

    /**
     * Removes the player from their current match or lobby voluntarily.
     *
     * @param nickname nickname of the quitting player
     */
    void quit(String nickname);

    /**
     * Returns the currently available lobbies grouped by player capacity.
     *
     * @param nickname nickname of the requesting player
     * @return map from player capacity to the list of open lobbies with that capacity
     */
    Map<Integer, List<LobbyDTO>> getLobbies(String nickname);

    /**
     * Returns the global leaderboard: cumulative scores for all players who
     * have participated in matches with the same player count as the requester's
     * last match.
     *
     * @param nickname nickname of the requesting player
     * @return map from player nickname to cumulative score
     */
    Map<String, Integer> requestRanking(String nickname);

    /**
     * Handles an involuntary disconnection for the given player, cleaning up
     * their match or lobby state.
     *
     * @param nickname nickname of the disconnected player
     */
    void handleDisconnection(String nickname);
}