package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
//il giocatore corrente ritorna in coda, si attivano effetti della queue

class EndTurnPhaseState implements GamePhaseState{
    @Override
    public GamePhaseState nextPhase(GameManager context){
        if(context.getQueueSize() < context.getNumPlayers()){
            return new DrawPhaseState();
        }
        else{
            return new EndRoundPhaseState();
        }
    }
    public void onEntry(GameManager context){
        context.execEndTurn();
    }
    @Override
    public GamePhaseEnum getPhase(){return GamePhaseEnum.END_TURN;}
}
