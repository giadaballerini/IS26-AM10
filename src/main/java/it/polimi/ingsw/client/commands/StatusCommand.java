package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;
/**
 * Command that displays the current game status screen to the client.
 * The status screen shows an overview of all players' stats during an ongoing match.
 * Unlike most commands, this one does not clear the log display before executing
 */
public class StatusCommand implements Command{
    /**
     * Executes the command by displaying the current game status screen to the client.
     *
     * @param client the client instance representing the player who wants to view the game status
     */
    @Override
    public void execute(Client client){
        client.showStatus();
    }
    /**
     * Indicates that the log display should not be cleared when this command is executed.
     *
     * @return {@code false}, so that existing logs are preserved
     */
    @Override
    public boolean shouldClearLogs(){return false;}
}
