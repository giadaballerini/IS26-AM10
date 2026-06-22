package it.polimi.ingsw.client.command;

import it.polimi.ingsw.client.Client;
/**
 * Command that requests the client to join an existing game lobby.
 * When executed, it asks the server to add the player to the lobby
 * identified by the given match ID, associating the request with
 * the client's nickname.
 */
public class JoinGameCommand implements Command {
    /**
     * The unique identifier of the lobby the player wants to join.
     */
    int matchId;

    /**
     * Creates a command to join an existing game lobby.
     *
     * @param matchId the unique identifier of the lobby the player wants to join
     *
     */
    public JoinGameCommand(int matchId) {
        this.matchId = matchId;
    }
    /**
     * Executes the command by requesting the server to add the client to the specified lobby.
     * The request is associated with the client's current nickname and the given match ID.
     *
     * @param client the client instance representing the player who wants to join the match
     */
    public void execute(Client client){
        client.joinGame(client.getNickname(), matchId);
    }
}
