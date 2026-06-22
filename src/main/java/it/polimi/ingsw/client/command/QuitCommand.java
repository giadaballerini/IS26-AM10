package it.polimi.ingsw.client.command;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;
/**
 * Command that makes the player leave the current match, lobby, or
 * post-game screen while remaining connected to the server.
 * When executed, it notifies the server that the player is leaving — which
 * causes the ongoing match, if any, to end for all participants — and then
 * returns the client to the main menu. The application itself keeps running.
 */
public class QuitCommand implements Command {
    /**
     * Executes the command by notifying the server that the player is
     * abandoning their current match, lobby, or post-game screen.
     *
     * @param client the client instance representing the player who wants to quit
     * @throws RemoteException if a communication error occurs during execution
     */
    @Override
    public void execute(Client client) throws RemoteException {
        client.quit();
    }
    /**
     * Creates a new {@code QuitCommand} instance.
     */
    public QuitCommand() {
    }
}