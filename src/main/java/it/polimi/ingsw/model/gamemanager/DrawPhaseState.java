package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
//giocatori che sono su una tile della board con effetti interattivi eseguono le drawCard, gli altri ricevono effetti istant
class DrawPhaseState implements GamePhaseState{
    public GamePhaseState nextPhase(GameManager context){
        if(context.getToDoActions().isEmpty()) {
            context.setSkippableDraw(false);
            return new EndTurnPhaseState();
        }else {
            return this;
        }
    }

    @Override
    public GamePhaseEnum getPhase(){
        return GamePhaseEnum.DRAW_PHASE;
    }

    public void onEntry(GameManager context){
        context.nextPlayer();
        context.checkBoardTileEffects();
        context.checkCanDraw();
    }
}
