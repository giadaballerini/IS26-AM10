package it.polimi.ingsw.observer;

import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.exceptions.InvalidPhaseException;
import it.polimi.ingsw.exceptions.InvalidPlayerException;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.gamemanager.GameManager;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.player.Player;

public interface GameEventListener {

    public void onCardDrawRequested(Card c, Player p);

    public void onMovePawnRequested(Tile t, Player p);

    public void onGameStartRequested();

}
