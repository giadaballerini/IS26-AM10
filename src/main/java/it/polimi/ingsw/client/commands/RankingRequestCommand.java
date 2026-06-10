package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

import java.rmi.RemoteException;
/**
 * Command that requests the global leaderboard from the server.
 * The ranking reflects cumulative points earned across all completed matches
 * and is only available once the current game has ended.
 */
public class RankingRequestCommand implements Command{
    /**
     * Executes the command by requesting the global leaderboard from the server.
     *
     * @param client the client instance representing the player who wants to view the ranking
     * @throws RemoteException if a communication error occurs during execution
     */
    @Override
    public void execute(Client client) throws RemoteException {
        client.requestRanking();
    }

}
