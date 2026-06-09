package it.polimi.ingsw.network.client;

/**
 * Defines the set of actions a client can send to the server.
 *
 * <p>Implemented by {@link it.polimi.ingsw.client.Client} and its subclasses.
 * Each method corresponds to a distinct game or session operation; transport
 * details (RMI vs. socket) are handled by the concrete implementation.
 */
public interface ClientToServerActions {

    /**
     * Attempts to register the given nickname with the server for the current
     * session.
     *
     * @param nickname the desired player nickname
     * @return {@code true} if the login was accepted; {@code false} otherwise
     */
    boolean login(String nickname);

    /**
     * Requests the creation of a new game lobby with the specified number of
     * players.
     *
     * @param nickname   the nickname of the player creating the game
     * @param numPlayers the total number of players expected in the match
     */
    void createGame(String nickname, int numPlayers);

    /**
     * Requests to join an existing lobby identified by the given match ID.
     *
     * @param nickname the nickname of the player joining
     * @param id       the ID of the lobby to join
     */
    void joinGame(String nickname, int id);

    /**
     * Sends a tile-placement move to the server.
     *
     * @param tileId the ID of the tile the player wants to place
     */
    void move(int tileId);

    /**
     * Requests to skip the current turn.
     */
    void skip();

    /**
     * Requests to draw a card.
     *
     * @param card the index identifying the card to draw
     */
    void draw(int card);

    /**
     * Requests the end-of-game ranking from the server.
     */
    void requestRanking();

    /**
     * Requests the list of currently available lobbies from the server.
     */
    void requestJoin();

    /**
     * Voluntarily leaves the current match without closing the session.
     */
    void quit();

    /**
     * Terminates the session and disconnects from the server.
     */
    void exit();
}