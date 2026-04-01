package it.polimi.ingsw.visitors;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.entities.card.types.character.Character;

public class DrawCardVisitor implements CardVisitor {
    private String errorMessage;
    final Player currPlayer;

    public DrawCardVisitor(Player currPlayer) {
        this.currPlayer = currPlayer;
        errorMessage = "";
    }
    public void visit(Building building) {
        if(currPlayer.getNFood() >= building.getFoodCost()){
            currPlayer.addFood(-building.getFoodCost());
            currPlayer.addBuilding(building);
        }
        else
            errorMessage = "Non disponi del cibo necessario per acquistare l'edificio scelto!";
    }
    public void visit(Character character) {
        currPlayer.addCard(character);
    }

    public void visit(Event event) {
        errorMessage = "Non puoi selezionare carte evento!";
    }
    public boolean hasErrorMessage(){
        return !errorMessage.isEmpty();
    }
    public String getErrorMessage(){
        return errorMessage;
    }
}
