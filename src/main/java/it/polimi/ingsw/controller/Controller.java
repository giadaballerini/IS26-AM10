package it.polimi.ingsw.controller;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.exceptions.InvalidPhaseException;
import it.polimi.ingsw.exceptions.InvalidPlayerException;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.GameEventListener;

import java.util.Objects;


public class Controller implements GameEventListener{

    private final GameManager gameManager;

    public Controller(GameManager gameManager) {

        Objects.requireNonNull(gameManager, "GameManager non può essere null!");
        this.gameManager = gameManager;
        subscribeListener(gameManager);
    }

    public void onCardDrawRequested(Card c, Player p) throws InvalidPhaseException, InvalidPlayerException {
        if (!gameManager.checkCorrectPhase(GamePhaseEnum.DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        else if (!(gameManager.checkCorrectPlayer(p))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }
        try {
            gameManager.drawCard(c);
        } catch (InvalidDrawException e) {
            throw new RuntimeException(e);
        }
    }
    public void subscribeListener(GameManager gm){
        if(gm != null)
            gm.addListener(this);
    }

    public void onMovePawnRequested(Tile t, Player p) throws InvalidPhaseException, InvalidPlayerException {
        if (!gameManager.checkCorrectPhase(GamePhaseEnum.SETUP_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        else if (!(gameManager.checkCorrectPlayer(p))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }
        try {
            gameManager.move(t);
        } catch (InvalidDrawException e) {
            throw new RuntimeException(e);
        }
    }

    public void onGameStartRequested(){
        gameManager.initGame();
    }
}
