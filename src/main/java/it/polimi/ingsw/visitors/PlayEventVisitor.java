package it.polimi.ingsw.visitors;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;

import java.util.*;

public class PlayEventVisitor implements CardVisitor {
    Event feastEvent = null;
    private List<Player> players;
    private GamePhaseEnum currPhase;
    public PlayEventVisitor(List<Player> players, GamePhaseEnum currPhase) {
        this.players = players;
        this.currPhase = currPhase;
    }
    public void visit(Event e){
        if(e.getType() == CardTypeEnum.FEAST)
            feastEvent = e;
        else
            e.execEvent(players, currPhase);
    }

    public void feastIfPresent(){
        if(feastEvent != null){
            feastEvent.execEvent(players, currPhase);
        }
    }

}
