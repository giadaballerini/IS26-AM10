package it.polimi.ingsw.model.interfaces;
import it.polimi.ingsw.model.entities.card.effects.instant.GainFood;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.*;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.player.Player;

public interface GainFoodVisitor {
    default void visit(Crafter crafter, Player p, GainFood gainFood){}
    default void visit(Builder builder, Player p, GainFood gainFood){}
    default void visit(Painter painter, Player p, GainFood gainfood){}
    default void visit(Hunter hunter, Player p, GainFood gainFood){}
    default void visit(Gatherer gatherer, Player p, GainFood gainFood){}
    default void visit(Shaman shaman, Player p, GainFood gainFood){}
    default void visit(Building building, Player p, GainFood gainFood){}
    default void visit(Event event, Player p, GainFood gainFood){}
}
