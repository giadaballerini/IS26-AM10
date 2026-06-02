package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;

public class RankingRequestCommand implements Command{
    @Override
    public void execute(Client client) throws RemoteException {
        client.requestRanking();
    }

}
