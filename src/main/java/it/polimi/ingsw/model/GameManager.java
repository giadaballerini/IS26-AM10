package it.polimi.ingsw.model;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.OccupiedTileException;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.GameEventListener;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.*;

public class GameManager {
    private List<Card> deck;
    private List<Card> buildings;
    private Queue<Tile> queue;
    private ArrayList<Tile> board;
    private List<Card> upperList;
    private List<Card> lowerList;
    private final int numPlayers;
    private GamePhaseEnum currPhase;
    private int currAge;
    private final List<Player> players;
    private Player currPlayer;
    private List<Action> toDoActions;
    private List<GameEventListener> listeners;
    private int currTurn;

    public GameManager(List<GameEventListener> listeners, List<Player> players, int numPlayers) {
        this.listeners = listeners;
        this.toDoActions = new ArrayList<Action>();
        this.currPlayer = null;
        this.players = players;
        this.currAge = 1;
        this.currPhase = SETUP_PHASE;
        this.numPlayers = numPlayers;

        this.deck = new ArrayList<Card>();
        this.buildings = new ArrayList<Card>();
        this.lowerList = new ArrayList<Card>();
        this.upperList = new ArrayList<Card>();
        this.board = new ArrayList<Tile>();
        this.queue = new LinkedList<Tile>();
        this.currTurn = 1;
    }

    public void initGame() {
        GameInitializer g = new GameInitializer();

        deck = g.initDeck(numPlayers);
        buildings = g.initBuildingDeck();
        lowerList = g.initLowerList(deck, upperList, numPlayers);
        upperList = g.initUpperList(deck, buildings, numPlayers);
        board = g.initBoard(numPlayers);
        queue = g.initQueue(numPlayers);
    }

    public void changeAge() {

        if (currAge < 3)
            currAge++;
        System.out.println("Current age: " + currAge);

        if (currAge == 3) {
            for (Card c : lowerList) {
                if (c.getType().equals(CardTypeEnum.BUILDING))
                    lowerList.remove(c);
            }
        }

        for (Card c : upperList) {
            if (c.getType().equals(CardTypeEnum.BUILDING)) {
                lowerList.add(c);
                upperList.remove(c);
            }
        }
        upperList.addAll(buildings.stream()
                .filter(b -> b.getAge() == currAge)
                .toList());
    }

    public void nextPhase() {
        currPhase = switch (currPhase) {
            case SETUP_PHASE -> {
                if (queue.isEmpty())
                    yield DRAW_PHASE;
                else
                    yield SETUP_PHASE;
            }
            case DRAW_PHASE -> {
                if (toDoActions.isEmpty())
                    yield END_TURN;
                else
                    yield DRAW_PHASE;
            }
            case END_TURN -> {
                if (queue.size() < numPlayers)
                    yield DRAW_PHASE;
                else {
                    currTurn++;
                    yield END_ROUND;
                }
            }
            case END_ROUND -> {
                if (currTurn == 10 && queue.size() == numPlayers)
                    yield PLAY_EVENT;
                else
                    yield END_ROUND;
            }
            case PLAY_EVENT -> {
                if(lowerList.isEmpty())
                    yield END_GAME;
                else
                    yield PLAY_EVENT;
            }

            default -> null;
        };
    }

    public void RefillBoard() {
        
        for(Card c : lowerList) {
            if (c.getType().isCharacter() || c.getType().isEvent()){
                lowerList.remove(c);
            }
        }

        for(Card c : upperList) {
            if (c.getType().isCharacter()) {
                lowerList.add(c);
                upperList.remove(c);
            }
        }

        for(int i = 0; i < numPlayers + 4; i++) {
            upperList.add(deck.removeFirst());
        }
    }

    public void move(Tile t) throws OccupiedTileException{
        if(t.isOccupied())
            throw new OccupiedTileException("La posizione in cui si sta provando a sposare la pedina è occupata!");
        Tile removed = queue.remove();
        queue.add(new Tile(removed));
        t.setPawn(currPlayer.getPawn());
    }

    private void checkEffects(){
        currPlayer.checkBuildsEffects(currPhase);
    }

    //DA FARE drawCard()

    public void finalScoreCount(){
        for(Player p : players){
            int ppsToAdd = 0;
            ppsToAdd += p.getBuilderPoints();
            int numCrafters = p.getNumType(CardTypeEnum.CRAFTER);
            int numSymbols = p.getTotSymbolsForCrafter();
            ppsToAdd += numCrafters * numSymbols;

            ppsToAdd += 10 * p.getNumType(CardTypeEnum.PAINTER) / 2;

            for(Building b : p.getBuildings()){
                ppsToAdd += b.getPpValue();
            }
            currPlayer = p;
            this.checkEffects();
            p.addPP(ppsToAdd);
        }
    }

    public List<Player> winner(){
        List<Player> winners = new ArrayList<>();

        return winners;
    }
}

