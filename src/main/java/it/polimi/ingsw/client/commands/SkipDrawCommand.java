package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class SkipDrawCommand implements Command {

    public void execute( Client client){
        client.skip();
    }
}
