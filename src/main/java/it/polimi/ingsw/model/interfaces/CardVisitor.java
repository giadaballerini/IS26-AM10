package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;

public interface CardVisitor {
    default void visit(Building b){}
    default void visit(Character c){}
    default void visit(Event e){}
}
