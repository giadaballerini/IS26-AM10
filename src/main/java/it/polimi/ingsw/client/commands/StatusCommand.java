package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class StatusCommand implements Command{
    @Override
    public void execute(Client client){
        client.showStatus();
    }

    @Override
    public boolean shouldClearLogs(){return false;}
}
