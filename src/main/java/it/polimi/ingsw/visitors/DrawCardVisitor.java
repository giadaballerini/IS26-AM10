package it.polimi.ingsw.visitors;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;

/**
 * Visitor that executes the draw action for a card on behalf of the current player.
 * <p>
 * Implements the Visitor pattern on {@link CardVisitor}: visits a card and applies
 * the appropriate effect depending on its type. If the action cannot be performed,
 * an error message is set and can be retrieved via {@link #getErrorMessage()}.
 * </p>
 */
public class DrawCardVisitor implements CardVisitor {

    /** Error message set when the draw action cannot be performed. Empty if no error occurred. */
    private String errorMessage;

    /** The player performing the draw action. */
    final Player currPlayer;

    /**
     * Constructs a new {@code DrawCardVisitor} for the given player.
     *
     * @param currPlayer the player performing the draw action
     */
    public DrawCardVisitor(Player currPlayer) {
        this.currPlayer = currPlayer;
        errorMessage = "";
    }

    /**
     * Visits a {@link Building} card: deducts the food cost (after discounts)
     * from the player's reserves and adds the building to their village.
     * Sets an error message if the player cannot afford the building.
     *
     * @param building the building card to draw
     */
    public void visit(Building building) {
        int actualCost = building.getFoodCost() - currPlayer.getTotBuildDisc();
        if (actualCost < 0)
            actualCost = 0;

        if (currPlayer.getNFood() >= actualCost) {
            currPlayer.addFood(-actualCost);
            currPlayer.addBuilding(building);
        } else {
            errorMessage = "Non disponi del cibo necessario per acquistare l'edificio scelto!";
        }
    }

    /**
     * Visits a {@link Character} card: adds the character directly to the player's hand.
     *
     * @param character the character card to draw
     */
    public void visit(Character character) {
        currPlayer.addCard(character);
    }

    /**
     * Visits an {@link Event} card: sets an error message since event cards
     * cannot be drawn by the player.
     *
     * @param event the event card being visited
     */
    public void visit(Event event) {
        errorMessage = "Non puoi selezionare carte evento!";
    }

    /**
     * Returns whether an error occurred during the last visit.
     *
     * @return {@code true} if an error message is set, {@code false} otherwise
     */
    public boolean hasErrorMessage() {
        return !errorMessage.isEmpty();
    }

    /**
     * Returns the error message set during the last visit, or an empty string if no error occurred.
     *
     * @return the error message, or an empty string if the action was successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}