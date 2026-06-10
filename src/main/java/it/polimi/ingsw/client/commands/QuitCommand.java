package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;
/**
 * Command that terminates the client application.
 * When executed, it notifies the server that the player is leaving,
 * which causes the ongoing match — if any — to end for all participants,
 * and then shuts down the client.
 */
public class QuitCommand implements Command {
    /**
     * Executes the command by notifying the server and shutting down the client application.
     *
     * @param client the client instance representing the player who wants to quit
     * @throws RemoteException if a communication error occurs during execution
     */
    @Override
    public void execute(Client client) throws RemoteException {
        client.quit();
    }
}
