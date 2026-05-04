package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.gamemanager.ApplicableActions;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.network.messages.client.*;

import it.polimi.ingsw.network.messages.service.PongMessage;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.util.Objects;


public class Controller implements ClientMessageVisitor {

    private final ApplicableActions gameManager;
    private final int numPlayers;
    /*
    public void onGameStartRequested(){
        gameManager.initGame();
    }
    */

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

    @Override
    public void visit(CreateGameMessage createGameMessage) {

    }

    @Override
    public void visit(JoinGameMessage joinGameMessage) {

    }

    @Override
    public void visit(LoginMessage loginMessage) {

    }

    @Override
    public void visit(AskLobbiesMessage askLobbiesMessage) {

    }

    @Override
    public void visit(PongMessage pongMessage) {

    }

    @Override
    public void visit(QuitMessage quitMessage) {

    }

    @Override
    public void visit(ExitMessage exitMessage) {

    }

    public int getNumPlayers(){
        return numPlayers;
    }

}
