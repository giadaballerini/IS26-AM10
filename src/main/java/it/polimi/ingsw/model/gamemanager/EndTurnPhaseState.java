package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
//il giocatore corrente ritorna in coda, si attivano effetti della queue

class EndTurnPhaseState implements GamePhaseState{
    @Override
    public GamePhaseState nextPhase(GameManager context) {
        if (context.getQueueSize() < context.getNumPlayers()) {
            return new DrawPhaseState();
        } else {
            // controllo quali player hanno il flag
            // se presente genero la lista (allora l'effetto del building deve avere trigger in EndTurn) e da qua vado in OptionalDraw (assumendo che un unico giocatore possa avere questa pesca opzionale)
            if (context.currAge == 3) {
                for (int i = 0; i < context.getNumPlayers(); i++) {
                    context.setCurrPlayer(context.players.get(i));
                    context.checkEffects();
                    if (!context.getToDoActions().isEmpty()) {
                        context.setSkippableDraw(true);
                        return new OptionalDrawPhaseState();
                    }
                }
            }
            return new EndRoundPhaseState();
        }
    }
    public void onEntry(GameManager context){
        context.execEndTurn();
    }
    @Override
    public GamePhaseEnum getPhase(){return GamePhaseEnum.END_TURN;}
}
