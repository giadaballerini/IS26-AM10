package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;

/**
 * Represents a command that can be executed by a client in the game.
 *  Implementations of this interface encapsulate a specific action that a player can perform during their interaction with the game.
 */
public interface Command {
    /**
     * Executes the command for the given client
     * @param client the player's client that wants to execute a command
     * @throws RemoteException if there's a communication error
     */
    void execute(Client client) throws RemoteException;

    /**
     * Indicates whether the log display should be cleared before executing this command.
     * Returns {@code true} by default; override this method to return {@code false}
     * if the command should preserve the existing logs.
     *
     * @return {@code true} if logs should be cleared, {@code false} otherwise
     */
    default boolean shouldClearLogs(){return true;}
}
