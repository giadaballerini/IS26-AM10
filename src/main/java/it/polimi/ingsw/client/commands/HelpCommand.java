package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class HelpCommand implements Command{
    public boolean shouldClearLogs(){
        return false;
    }
    public void execute(Client client){
        client.help();
    }
}
