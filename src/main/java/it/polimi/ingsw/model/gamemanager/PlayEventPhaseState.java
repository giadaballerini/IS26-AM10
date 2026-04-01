package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
//si giocano i due eventi finali
class PlayEventPhaseState implements GamePhaseState{
    @Override
    public GamePhaseState nextPhase(GameManager context){
        return new EndGamePhaseState();
    }
    public void onEntry(GameManager context){
        context.playEvent();
    }
    @Override
    public GamePhaseEnum getPhase(){return GamePhaseEnum.PLAY_EVENT;}
}
