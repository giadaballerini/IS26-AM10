package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

public class OptionalDrawPhaseState implements GamePhaseState{
    public GamePhaseState nextPhase(GameManager context){
        if(context.getToDoActions().isEmpty()) {
            context.setSkippableDraw(false);
            context.nextPlayer();
            return new EndRoundPhaseState();
        }
        return this;
    }

    @Override
    public void onEntry(GameManager context){
        context.loadSkippableDraws();
    }
    @Override
    public GamePhaseEnum getPhase(){
        return GamePhaseEnum.OPTIONAL_DRAW_PHASE;
    }
}
