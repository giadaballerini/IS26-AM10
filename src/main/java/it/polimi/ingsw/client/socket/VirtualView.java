package it.polimi.ingsw.client.socket;

import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.util.List;
import java.util.Map;

/**
 * Represents the server-side view of a client connected via Socket.
 * Implementations handle both incoming client messages and outgoing server
 * notifications for a single Socket connection, acting as the communication
 * endpoint between the server logic and the remote client.
 * Extends {@link ModelObserver} to receive game state updates from the model.
 */
public interface VirtualView extends ModelObserver {

    /**
     * Called when a client successfully establishes a connection and sends their nickname.
     * Registers the nickname on the server side and starts the health check for this connection.
     *
     * @param nickname the nickname chosen by the connecting client
     */
    void onLogin(String nickname);

    /**
     * Notifies the client that their login attempt was successful.
     *
     * @param nickname the nickname that was accepted by the server
     */
    void onLoginSuccess(String nickname);

    /**
     * Notifies the client that their login attempt failed.
     *
     * @param error a message describing the reason for the failure
     */
    void onLoginFailed(String error);

    /**
     * Delivers the list of currently available lobbies to the client,
     * grouped by lobby capacity.
     *
     * @param lobbies a map from lobby capacity to the list of lobbies with that capacity
     */
    void onLobbiesRequested(Map<Integer, List<LobbyDTO>> lobbies);

    /**
     * Handles the disconnection of a client, notifying the server to clean up
     * the associated session and match state if necessary.
     *
     * @param nickname the nickname of the disconnected client
     */
    void handleDisconnection(String nickname);

    /**
     * Notifies the client that the game they requested to create has been successfully created.
     *
     * @param gameId the unique identifier of the newly created game
     */
    void onGameCreated(int gameId);

    /**
     * Dispatches an incoming client message to the appropriate handler via the visitor pattern.
     *
     * @param message the message received from the client
     */
    void onClientMessage(ClientMessage message);

    /**
     * Sets the visitor used to process incoming client messages.
     * The visitor determines how each type of {@link ClientMessage} is handled.
     *
     * @param visitor the visitor to use for dispatching client messages
     */
    void setVisitor(ClientMessageVisitor visitor);

    /**
     * Notifies the client that they have successfully joined the specified game.
     *
     * @param gameid the unique identifier of the game that was joined
     */
    void onJoinGame(int gameid);

    /**
     * Delivers the global ranking response to the client following a ranking request.
     *
     * @param ranking a map from each player's nickname to their cumulative Prestige Points
     *                in the global leaderboard
     */
    void onRankingResponse(Map<String, Integer> ranking);


    /**
     * Notifies the observer that an error has occurred.
     *
     * @param errorMsg human-readable description of the error; never {@code null}
     */
    void onErrorMessage(String errorMsg);
}