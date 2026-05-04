package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;

public interface Command {
    void execute(Client client) throws RemoteException;
    default boolean shouldClearLogs(){return true;}
}
