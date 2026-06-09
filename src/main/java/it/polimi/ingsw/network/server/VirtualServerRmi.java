package it.polimi.ingsw.network.server;

import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.exceptions.InvalidTimingException;
import it.polimi.ingsw.network.dto.LobbyDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * RMI-remote version of {@link VirtualServer}.
 *
 * <p>Extends {@link Remote} so that its methods can be called by clients over
 * the RMI registry. Every method declares {@link RemoteException} as required
 * by the RMI specification. Methods that enforce session-timing rules also
 * declare {@link InvalidTimingException} so that the client receives a
 * meaningful error when an action is performed out of a sequence.
 */
public interface VirtualServerRmi extends Remote {

    /**
     * Registers a new RMI client session under the given nickname.
     *
     * @param nickname   the desired player nickname
     * @param clientStub the client's RMI callback stub for server-to-client events
     * @throws RemoteException if the remote call fails
     */
    void login(String nickname, VirtualViewRmi clientStub) throws RemoteException;

    /**
     * Creates a new game lobby with the specified player capacity.
     *
     * @param nickname   nickname of the player creating the lobby
     * @param numPlayers number of players for the new match
     * @return the unique match ID assigned to the new lobby
     * @throws RemoteException if the remote call fails
     */
    int createGame(String nickname, int numPlayers) throws RemoteException;

    /**
     * Adds the player to an existing lobby identified by the given match ID.
     *
     * @param nickname nickname of the joining player
     * @param id       ID of the target lobby
     * @throws RemoteException if the remote call fails
     */
    void joinGame(String nickname, int id) throws RemoteException;

    /**
     * Places the player's pawn on the specified board tile.
     *
     * @param nickname nickname of the moving player
     * @param tileId   ID of the target tile
     * @throws RemoteException if the remote call fails
     */
    void move(String nickname, int tileId) throws RemoteException;

    /**
     * Draws the card on behalf of the player.
     *
     * @param id       index of the card to draw
     * @param nickname nickname of the drawing player
     * @throws RemoteException if the remote call fails
     */
    void draw(int id, String nickname) throws RemoteException;

    /**
     * Skips the player's remaining actions for the current turn.
     *
     * @param nickname nickname of the skipping player
     * @throws RemoteException if the remote call fails
     */
    void skip(String nickname) throws RemoteException;

    /**
     * Removes the player from their current match or lobby voluntarily.
     *
     * @param nickname nickname of the quitting player
     * @throws RemoteException       if the remote call fails
     * @throws InvalidTimingException if the player is not currently in a match or lobby
     */
    void quit(String nickname) throws RemoteException, InvalidTimingException;

    /**
     * Returns the currently available lobbies grouped by player capacity.
     *
     * @param nickname nickname of the requesting player
     * @return map from player capacity to the list of open lobbies with that capacity
     * @throws RemoteException if the remote call fails
     */
    Map<Integer, List<LobbyDTO>> getLobbies(String nickname) throws RemoteException;

    /**
     * Returns the global leaderboard: cumulative scores for all players who
     * have participated in matches with the same player count as the requester's
     * last match.
     *
     * @param nickname nickname of the requesting player
     * @return map from player nickname to cumulative score
     * @throws RemoteException if the remote call fails
     */
    Map<String, Integer> requestRanking(String nickname) throws RemoteException;

    /**
     * Handles an involuntary disconnection for the given player, cleaning up
     * their match or lobby state.
     *
     * @param nickname nickname of the disconnected player
     * @throws RemoteException if the remote call fails
     */
    void handleDisconnection(String nickname) throws RemoteException;

    /**
     * No-op method called by the server's health check thread to verify that
     * this client's RMI stub is still reachable.
     *
     * @throws RemoteException if the remote call fails
     */
    void ping() throws RemoteException;
}