package it.polimi.ingsw.client.commands;

import it.polimi.ingsw.client.Client;
/**
 * Command that requests the drawing of a card from the game.
 * When executed, it asks the server to draw the card with the specified ID
 * on behalf of the client.
 */
public class DrawCardCommand implements Command {
    private final int cardId;

    /**
     * Creates a command to draw a specific card.
     *
     * @param cardId the unique identifier of the card to be drawn
     */
    public DrawCardCommand(int cardId) {
        this.cardId = cardId;
    }
    /**
     * Executes the command by requesting the server to draw the specified card.
     *
     * @param client the client instance representing the player who wants to draw the card
     */
    @Override
    public void execute(Client client){
        client.draw(cardId);
    }
}
