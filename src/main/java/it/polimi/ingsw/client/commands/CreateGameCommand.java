package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

/**
 * Command that requests the creation of a new game match.
 * When executed, it asks the server to create a match with the given ID,
 * associating it with the client's nickname.
 */
public class CreateGameCommand implements Command {
    private final int matchId;

    /**
     * Command that requests the creation of a new game match.
     * When executed, it asks the server to create a match with the given ID,
     * associating it with the client's nickname.
     * @param matchId the unique identifier for the match to be created
     */
    public CreateGameCommand(int matchId) {
        this.matchId = matchId;
    }
    /**
     * Executes the command by requesting the server to create a new match.
     * The match is associated with the client's current nickname and the specified match ID.
     *
     * @param client the client instance representing the player who wants to create the match
     */
    public void execute(Client client){
        client.createGame(client.getNickname(), matchId);
    }
}
