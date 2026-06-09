package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.player.Player;

/**
 * Visitor interface used by {@link GainFood} effects that need to inspect the
 * card that triggered them before applying food to a player.
 *
 * <p>Implemented by {@link it.polimi.ingsw.enumerations.GainFoodEnum} values
 * whose food gain rule depends on the type of the source card.
 * All overloads default to no-ops so that implementors only
 * override the card types they care about.</p>
 */
public interface GainFoodVisitor {

    /**
     * Called when the source card is a {@link Crafter}.
     * The default implementation is a no-op.
     *
     * @param crafter  the crafter card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Crafter crafter, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is a {@link Builder}.
     * The default implementation is a no-op.
     *
     * @param builder  the builder card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Builder builder, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is a {@link Painter}.
     * The default implementation is a no-op.
     *
     * @param painter  the painter card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Painter painter, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is a {@link Hunter}.
     * The default implementation is a no-op.
     *
     * @param hunter   the hunter card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Hunter hunter, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is a {@link Gatherer}.
     * The default implementation is a no-op.
     *
     * @param gatherer the gatherer card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Gatherer gatherer, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is a {@link Shaman}.
     * The default implementation is a no-op.
     *
     * @param shaman   the shaman card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Shaman shaman, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is a {@link Building}.
     * The default implementation is a no-op.
     *
     * @param building the building card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Building building, Player p, GainFood gainFood) {}

    /**
     * Called when the source card is an {@link Event}.
     * The default implementation is a no-op.
     *
     * @param event    the event card that triggered the effect
     * @param p        the player receiving the food
     * @param gainFood the food gain effect being applied
     */
    default void visit(Event event, Player p, GainFood gainFood) {}
}