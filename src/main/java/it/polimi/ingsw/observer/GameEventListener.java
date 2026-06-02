package it.polimi.ingsw.observer;

import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;

public interface GameEventListener {

    void onCardDrawRequested(Card c, Player p);

    void onMovePawnRequested(Tile t, Player p);

    void onGameStartRequested();

}
