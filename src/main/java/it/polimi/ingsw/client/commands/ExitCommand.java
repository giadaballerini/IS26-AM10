package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;

/**
 * Command that requests the client to exit the current match.
 * When executed, it notifies the server that the player is leaving,
 * terminating the client's participation in the ongoing game.
 */
public class ExitCommand implements Command{

    /**
     * Executes the command by requesting the server to remove the client from the current match.
     *
     * @param client the client instance representing the player who wants to exit the match
     * @throws RemoteException if a communication error occurs during execution
     */
    @Override
    public void execute(Client client) throws RemoteException {
        client.exit();
    }
}
