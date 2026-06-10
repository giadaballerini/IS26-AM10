package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;

public class InfoCommand implements Command{

    private final int cardId;

    /**
     * Creates a command to display the info of the card selected
     * @param cardId must not be null
     */
    public InfoCommand(int cardId) {
        this.cardId = cardId;
    }
    public void execute(Client client){
        client.info(cardId);
    }

    public boolean shouldClearLogs(){
        return false;
    }
}
