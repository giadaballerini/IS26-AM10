package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;
/**
 * Command that requests the list of currently available lobbies from the server.
 * When executed, it fetches the open lobbies and displays them to the player,
 * allowing them to choose which one to join.
 */
public class ShowLobbiesCommand implements Command {
    /**
     * Executes the command by requesting the available lobbies from the server
     * and displaying them to the client.
     *
     * @param client the client instance representing the player who wants to view the available lobbies
     */
    @Override
    public void execute(Client client){
        client.requestJoin();
    }
}
