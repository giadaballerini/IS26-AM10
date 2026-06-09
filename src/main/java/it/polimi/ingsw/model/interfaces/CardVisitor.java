package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;

/**
 * Visitor interface for the three main card types in the game.
 *
 * <p>Implementations override only the {@code visit} overloads relevant to
 * them; the remaining overloads are no-ops by default, so implementors are
 * not forced to handle card types they do not care about.</p>
 */
public interface CardVisitor {

    /**
     * Called when visiting a {@link Building} card.
     * The default implementation is a no-op.
     *
     * @param b the building card being visited
     */
    default void visit(Building b) {}

    /**
     * Called when visiting a {@link Character} card.
     * The default implementation is a no-op.
     *
     * @param c the character card being visited
     */
    default void visit(Character c) {}

    /**
     * Called when visiting an {@link Event} card.
     * The default implementation is a no-op.
     *
     * @param e the event card being visited
     */
    default void visit(Event e) {}
}