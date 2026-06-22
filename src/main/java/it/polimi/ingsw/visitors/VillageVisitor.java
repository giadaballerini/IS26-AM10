package it.polimi.ingsw.visitors;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Event;

/**
 * Visitor for character cards present in a player's village.
 * <p>
 * Defines visit methods for each character type. All methods have a default
 * empty implementation, allowing implementing classes to override only the
 * character types they need to handle.
 * </p>
 */
@SuppressWarnings("EmptyMethod")
public interface VillageVisitor {

    /**
     * Visits a {@link Crafter} character card.
     *
     * @param crafter the crafter card to visit
     */
    default void visit(Crafter crafter) {}

    /**
     * Visits a {@link Builder} character card.
     *
     * @param builder the builder card to visit
     */
    default void visit(Builder builder) {}

    /**
     * Visits a {@link Painter} character card.
     *
     * @param painter the painter card to visit
     */
    default void visit(Painter painter) {}

    /**
     * Visits a {@link Hunter} character card.
     *
     * @param hunter the hunter card to visit
     */
    default void visit(Hunter hunter) {}

    /**
     * Visits a {@link Gatherer} character card.
     *
     * @param gatherer the gatherer card to visit
     */
    default void visit(Gatherer gatherer) {}

    /**
     * Visits a {@link Shaman} character card.
     *
     * @param shaman the shaman card to visit
     */
    default void visit(Shaman shaman) {}

    /**
     * Visits a {@link Building} card.
     *
     * @param building the building card to visit
     */
    default void visit(Building building){}

    /**
     * Visits an {@link Event} card.
     *
     * @param event the event card to visit
     */
    default void visit(Event event){}
}