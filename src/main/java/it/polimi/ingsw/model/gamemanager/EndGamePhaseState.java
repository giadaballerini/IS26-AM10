package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
// vengono calcolati i punteggi e decretato il vincitore della partita
class EndGamePhaseState implements GamePhaseState{
    @Override
    public GamePhaseState nextPhase(GameManager context){return this;}

    public void onEntry(GameManager context){
        context.finalScoreCount();
        context.gameWinners();
    }

    @Override
    public GamePhaseEnum getPhase(){return GamePhaseEnum.END_GAME;}
}
