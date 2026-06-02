package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.gamemanager.ApplicableActions;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.visitors.GameMessageVisitor;
import java.util.Objects;


public class Controller implements GameMessageVisitor {

    private final ApplicableActions gameManager;
    private final int numPlayers;

    public Controller(GameManager gameManager, int numPlayers) {
        Objects.requireNonNull(gameManager, "GameManager non può essere null!");
        this.gameManager = gameManager;
        this.numPlayers = numPlayers;
    }

    public void visit(MoveMessage moveMessage) {
        gameManager.onMoveRequested(moveMessage.getPlayer(), moveMessage.getTilePos());
    }
    public void visit(DrawMessage drawMessage){
        gameManager.onDrawCardRequested(drawMessage.getNickname(), drawMessage.getCardId());
    }

    public void visit(SkipMessage skipMessage){
        gameManager.onSkipRequested(skipMessage.getNickname());
    }


    public int getNumPlayers(){
        return numPlayers;
    }

}
