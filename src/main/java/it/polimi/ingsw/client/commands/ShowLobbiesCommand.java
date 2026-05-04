package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class ShowLobbiesCommand implements Command {
    @Override
    public void execute(Client client){
        client.requestJoin();
    }
}
