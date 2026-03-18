package it.polimi.ingsw.model;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.GameEventListener;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.SETUP_PHASE;

public class GameManager {
    private List<Card> deck;
    private Queue<Tile> queue;
    private ArrayList<Tile> board;
    private List<Card> upperList;
    private List<Card> lowerList;
    private final int numPlayers;
    private GamePhaseEnum currPhase;
    private int currAge;
    private final List<Player> players;
    private int currPlayer;
    private List<Action> toDoActions;
    private List<GameEventListener> listeners;

    public GameManager(List<GameEventListener> listeners, List<Player> players, int numPlayers, GameInitializer gi) {
        this.listeners = listeners;
        this.toDoActions = new ArrayList<Action>();
        this.currPlayer = 0;
        this.players = players;
        this.currAge = 1;
        this.currPhase = SETUP_PHASE;
        this.numPlayers = numPlayers;

        this.deck = new ArrayList<Card>();
        this.lowerList = new ArrayList<Card>();
        this.upperList = new ArrayList<Card>();
        this.board = new ArrayList<Tile>();
        this.queue = new LinkedList<Tile>();
    }

    public void initGame() {}
}
