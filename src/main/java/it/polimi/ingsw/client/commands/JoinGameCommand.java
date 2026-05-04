package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class JoinGameCommand implements Command {
    int matchId;

    public JoinGameCommand(int matchId) {
        this.matchId = matchId;
    }

    public void execute(Client client){
        client.joinGame(client.getNickname(), matchId);
    }
}
