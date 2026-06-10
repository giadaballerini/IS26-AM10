package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;
/**
 * Command that requests to skip the current optional draw action.
 * During the draw phase, a player may be offered the chance to draw one or more cards;
 * this command allows them to decline and pass to the next phase instead.
 */
public class SkipDrawCommand implements Command {
    /**
     * Executes the command by requesting the server to skip the current draw action
     * on behalf of the client.
     *
     * @param client the client instance representing the player who wants to skip the draw
     */
    public void execute( Client client){
        client.skip();
    }
}
