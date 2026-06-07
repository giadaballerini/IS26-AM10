package it.polimi.ingsw.visitors;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;

/**
 * Visitor that determines whether the current player can or must draw a card.
 * <p>
 * Implements the Visitor pattern on {@link CardVisitor}: visits a card and
 * updates the {@code mustDraw} and {@code mayDraw} flags based on the card
 * type and the player's available resources.
 * </p>
 */
public class CanDrawVisitor implements CardVisitor {

    private Player currPlayer;

    /**
     * {@code true} if the player must mandatorily draw the visited card
     * (the visited card is a {@link Character}).
     */
    private boolean mustDraw;

    /**
     * {@code true} if the player may optionally draw the visited card
     * (the visited card is a {@link Building} the player can afford).
     */
    private boolean mayDraw;

    /**
     * Constructs a new {@code CanDrawVisitor} for the given player.
     *
     * @param currPlayer the player whose draw eligibility is to be evaluated
     */
    public CanDrawVisitor(Player currPlayer) {
        this.mustDraw = false;
        this.mayDraw = false;
        this.currPlayer = currPlayer;
    }

    /**
     * Visits a {@link Character} card: sets {@code mustDraw} to {@code true},
     * since character cards must always be drawn.
     *
     * @param character the character card being visited
     */
    public void visit(Character character) {
        this.mustDraw = true;
    }

    /**
     * Visits a {@link Building} card: sets {@code mayDraw} to {@code true}
     * if the player has enough food to afford it, accounting for any
     * accumulated building discounts.
     *
     * @param building the building card being visited
     */
    public void visit(Building building) {
        int actualCost = building.getFoodCost() - currPlayer.getTotBuildDisc();
        if (actualCost < 0)
            actualCost = 0;
        if (this.currPlayer.getNFood() >= actualCost) {
            this.mayDraw = true;
        }
    }

    /**
     * Visits an {@link Event} card: no action is taken, as event cards
     * cannot be drawn by the player.
     *
     * @param event the event card being visited
     */
    public void visit(Event event) {}

    /**
     * Returns whether the player must mandatorily draw the visited card.
     *
     * @return {@code true} if drawing is mandatory, {@code false} otherwise
     */
    public boolean getMustDraw() {
        return this.mustDraw;
    }

    /**
     * Returns whether the player may optionally draw the visited card.
     *
     * @return {@code true} if drawing is possible, {@code false} otherwise
     */
    public boolean getMayDraw() {
        return this.mayDraw;
    }
}