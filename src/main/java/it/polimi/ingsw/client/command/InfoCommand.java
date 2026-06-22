package it.polimi.ingsw.client.command;

import it.polimi.ingsw.client.Client;

/**
 * The command that displays the info of the card selected
 */
public class InfoCommand implements Command{
    /**
     * The unique identifier of the card to be displayed, passed to {@link Client#info(int)}.
     */
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
