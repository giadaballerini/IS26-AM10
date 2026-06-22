package it.polimi.ingsw.client.command;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;

/**
 * Command that terminates the client application.
 * When executed, it notifies the server of the disconnection, then shuts
 * down the network connection and exits the program entirely.
 */
public class ExitCommand implements Command{

    /**
     * Executes the command by notifying the server of the disconnection and
     * shutting down the client application.
     *
     * @param client the client instance representing the player who wants to exit
     * @throws RemoteException if a communication error occurs during execution
     */
    @Override
    public void execute(Client client) throws RemoteException {
        client.exit();
    }

    /**
     * Creates a new {@code ExitCommand} instance.
     */
    public ExitCommand() {
    }
}