package it.polimi.ingsw.visitors;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.interfaces.CardVisitor;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.EventDTO;

import java.util.*;

public class PlayEventVisitor implements CardVisitor {
    Event feastEvent = null;
    private List<Player> players;
    private GamePhaseEnum currPhase;
    private EventDTO events;
    public PlayEventVisitor(List<Player> players, GamePhaseEnum currPhase, EventDTO events) {
        this.players = players;
        this.currPhase = currPhase;
        this.events = events;
    }
    public void visit(Event e){
        if(e.getType() == CardTypeEnum.FEAST)
            feastEvent = e;
        else {
            e.execEvent(players, currPhase);
            events.addEvents(e.toDTO());
        }
    }

    public void feastIfPresent(){
        if(feastEvent != null){
            feastEvent.execEvent(players, currPhase);
            events.addEvents(feastEvent.toDTO());
        }
    }

}
