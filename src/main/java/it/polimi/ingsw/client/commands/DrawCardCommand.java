package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class DrawCardCommand implements Command {
    private final int cardId;
    public DrawCardCommand(int cardId) {
        this.cardId = cardId;
    }

    @Override
    public void execute(Client client){
        client.draw(cardId);
    }
}
