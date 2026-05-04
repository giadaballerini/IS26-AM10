package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

public class OptionalDrawPhaseState implements GamePhaseState{
    public GamePhaseState nextPhase(GameManager context){
        if(context.getToDoActions().isEmpty()) {
            assert context.queue.getFirst() != null; //serve davvero?
            context.setCurrPlayer(context.queue.getFirst().getPlayer());
            return new EndRoundPhaseState();
        }
        else
            return this;
    }
    //qui rimango in attesa andando in EndRound dal momento che la lista di azioni si svuota
    @Override
    public GamePhaseEnum getPhase(){
        return GamePhaseEnum.DRAW_PHASE;
    }
}
