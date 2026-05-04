package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class MoveCommand implements Command {
    private final int tilePos;
    public MoveCommand(int tilePos) {
        this.tilePos = tilePos;
    }

    public void execute(Client client) {
        client.move(tilePos - 1);
    }
}
