package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class CreateGameCommand implements Command {
    private final int matchId;
    public CreateGameCommand(int matchId) {
        this.matchId = matchId;
    }

    public void execute(Client client){
        client.createGame(client.getNickname(), matchId);
    }
}
