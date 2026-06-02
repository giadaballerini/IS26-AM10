package it.polimi.ingsw.model.interfaces;

import it.polimi.ingsw.model.entities.card.effects.instant.GainPP;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.player.Player;

public interface GainPPVisitor {
    default void visit(Crafter crafter, Player p, GainPP gainPP){}
    default void visit(Builder builder, Player p, GainPP gainPP){}
    default void visit(Painter painter, Player p, GainPP gainPP){}
    default void visit(Gatherer gatherer, Player p, GainPP gainPP){}
    default void visit(Shaman shaman, Player p, GainPP gainPP){}
    default void visit(Hunter hunter, Player p, GainPP gainPP){}
    default void visit(Building building, Player p, GainPP gainPP){}
    default void visit(Event event, Player p, GainPP gainPP){}
}
