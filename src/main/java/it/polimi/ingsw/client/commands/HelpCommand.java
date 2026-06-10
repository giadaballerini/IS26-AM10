package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

/**
 * Command that displays the list of available commands to the client.
 */
public class HelpCommand implements Command{
    /**
     * Indicates that the log display should not be cleared when this command is executed.
     *
     * @return {@code false}, so that existing logs are preserved
     */
    public boolean shouldClearLogs(){
        return false;
    }
    /**
     * Executes the command by displaying the list of available commands to the client.
     *
     * @param client the client associated to the player that executed the command
     */
    public void execute(Client client){
        client.help();
    }
}
