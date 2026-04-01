package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
// tutti i giocatori eseguono la move, non si attivano effetti della board
class SetupPhaseState implements GamePhaseState{
    @Override
    public GamePhaseState nextPhase(GameManager context){

        if(context.isQueueEmpty()) {
            return new DrawPhaseState();
        }
        else{
            context.nextPlayer();
            return this;
        }
    }

    @Override
    public GamePhaseEnum getPhase(){return GamePhaseEnum.SETUP_PHASE;}
}

/*
Finchè non è finita la fase di spostamento dei player sulle tile della board,
per aggiornare il giocatore corrente a seguito della move()
all'interno della quale viene chiamato nextPhase(),
in cui chiamiamo nextPlayer() nel caso non siano finite le move() da fare.
Il primo giocatore a eseguire la drawPhase viene scelto con nextPlayer dentro
costruttore di DrawPhase.
*/