package it.polimi.ingsw.model.gamemanager;


import it.polimi.ingsw.enumerations.GamePhaseEnum;

interface GamePhaseState {
    GamePhaseState nextPhase(GameManager context);
    GamePhaseEnum getPhase();
    default void onEntry(GameManager context){};
}
