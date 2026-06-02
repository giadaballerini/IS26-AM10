package it.polimi.ingsw.visitors;

import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;

public class CanDrawVisitor implements CardVisitor {
    private Player currPlayer;
    private boolean mustDraw;    //quando devi prendere un character
    private boolean mayDraw;   //quando PUOI prendere un building dalla lista
    public CanDrawVisitor(Player currPlayer) {
        this.mustDraw = false;
        this.mayDraw = false;
        this.currPlayer = currPlayer;
    }

    public void visit(Character character){
        this.mustDraw = true;
    }
    public void visit(Building building){
        int actualCost = building.getFoodCost() - currPlayer.getTotBuildDisc();
        if(actualCost < 0)
            actualCost = 0;
        if(this.currPlayer.getNFood() >= actualCost){
            this.mayDraw = true;
        }
    }

    public void visit(Event event){}

    public boolean getMustDraw() {
        return this.mustDraw;
    }

    public boolean getMayDraw() {
        return this.mayDraw;
    }
}
