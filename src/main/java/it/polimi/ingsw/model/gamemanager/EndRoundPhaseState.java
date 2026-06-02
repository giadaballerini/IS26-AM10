package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

class EndRoundPhaseState implements GamePhaseState{
    @Override
    public GamePhaseState nextPhase(GameManager context){
        if(context.hasAnySkippableDraws()) {
            return new OptionalDrawPhaseState();
        }
        if(context.getCurrTurn() == 10) {
            return new PlayEventPhaseState();
        }
        else{
            context.refillBoard();
            context.incrementTurn();
            return new SetupPhaseState();
        }
    }
    public void onEntry(GameManager context){
        context.playEvent();
    }
    @Override
    public GamePhaseEnum getPhase(){return GamePhaseEnum.END_ROUND;}
}
