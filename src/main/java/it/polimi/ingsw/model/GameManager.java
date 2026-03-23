package it.polimi.ingsw.model;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.exceptions.OccupiedTileException;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.character.Character;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.GameEventListener;


import java.util.*;

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
            lowerList.removeIf(c -> c.getType().equals(CardTypeEnum.BUILDING));
        }

        Iterator<Card> it = upperList.iterator();
        while (it.hasNext()) {
            Card c = it.next();
            if (c.getType().equals(CardTypeEnum.BUILDING)) {
                lowerList.add(c);
                it.remove();
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
                if (queue.size() < numPlayers) {
                    nextPlayer();
                    yield DRAW_PHASE;
                }
                else {
                    currTurn++;
                    yield END_ROUND;
                }
            }
            case END_ROUND -> {
                if (currTurn == 10 && queue.size() == numPlayers)
                    yield PLAY_EVENT;
                else {
                    playEvent();
                    refillBoard();
                    yield END_ROUND;
                }
            }
            case PLAY_EVENT -> {
                playEvent();
                yield END_GAME;
            }

            default -> null;
        };
    }

    private void refillBoard() {

        lowerList.removeIf(c -> c.getType().isEvent() || c.getType().isCharacter());

        Iterator<Card> it = upperList.iterator();
        while (it.hasNext()) {
            Card c = it.next();
            if (c.getAge() > currAge) {
                changeAge();
            }
            if (c.getType().isCharacter()) {
                lowerList.addFirst(c);
                it.remove();
            }
        }

        for(int i = 0; i < numPlayers + 4; i++) {
            if(!deck.isEmpty())
                upperList.addFirst(deck.removeFirst());
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
        List<Action> list = currPlayer.checkBuildsEffects(currPhase);
        if(list != null)
            toDoActions.addAll(list);
    }


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

    public List<Player> gameWinners(){
        List<Player> winners = new ArrayList<>();

        int maxPP = 0;

        for(Player p : players){
            if(p.getPP() > maxPP){
                maxPP = p.getPP();
            }
        }
        for(Player p : players){
            if(p.getPP() == maxPP){
                winners.add(p);
            }
        }

        if(winners.size() > 1){
            int maxFood = 0;
            for(Player p : winners){
                if(p.getNFood() > maxFood){
                    maxFood = p.getNFood();
                }
            }

            for(Player p : winners){
                if(p.getNFood() < maxFood){
                    winners.remove(p);
                }
            }
        }

        return winners;
    }

    public Player getCurrentPlayer(){return currPlayer;}

    private void nextPlayer(){
        assert queue.peek() != null;
        currPlayer = queue.peek().getPawn().getP();
    }

    public void addListener(GameEventListener listener){
        listeners.add(listener);
    }

    public List<Action> getToDoActions(){
        return new ArrayList<>(toDoActions);
    }

    private void insertAction(Action action){
        toDoActions.add(action);
    }

    private void playEvent(){

        Event feastEvent = null;

        if(currPhase == END_ROUND) {
            for (Card c : lowerList) {
                if (c.getType().isEvent()) {
                    Event e = (Event) c;
                    if (e.getType() == CardTypeEnum.FEAST)
                        feastEvent = e;
                    else
                        e.execEvent(players, currPhase);
                }
            }
        }
        else if(currPhase == PLAY_EVENT){
            for(Card c : upperList){
                if(c.getType().isEvent()){
                    Event e = (Event)c;
                    if(e.getType() == CardTypeEnum.FEAST)
                        feastEvent = e;
                    else
                        e.execEvent(players, currPhase);
                }
            }
        }
        if(feastEvent != null)
            feastEvent.execEvent(players, currPhase);

    }

    private void checkTileEffects(Collection<Tile> tiles){
        for(Tile t : tiles){
            if(t.isOccupied() && t.getPawn() == currPlayer.getPawn()){
                t.execInstantEffect();
                List<Action> actions = t.execInteractiveEffect();
                if(actions != null)
                    toDoActions.addAll(actions);
            }
        }
    }

    public void checkBoardTileEffects(){
        checkTileEffects(board);
    }

    public void checkQueueTileEffects(){
        checkTileEffects(queue);
    }

    private void resolveAction(Action a){
        toDoActions.remove(a);
    }

    public void drawCard(Card card) throws InvalidDrawException {
        boolean isInUpper = false;
        for(Card c: upperList) {
            if (c.equals(card)) {
                isInUpper = true;
                for(Action a : toDoActions){
                    if(a.getType().equals(DrawCardEnum.UP_DRAW)){
                        if(c.getType().isEvent()){
                            throw new InvalidDrawException("Non puoi selezionare carte evento!");
                        }
                        else if(c.getType().equals(CardTypeEnum.BUILDING)){
                            Building building = (Building)c;
                            if(currPlayer.getNFood() >= building.getFoodCost()){
                                currPlayer.addFood(-building.getFoodCost());
                                currPlayer.addBuilding(building);
                            }
                            else throw(new InvalidDrawException("Non disponi del cibo necessario per acquistare l'edificio scelto!"));
                        }
                        else if(c.getType().isCharacter()){
                            currPlayer.addCard((Character)c);
                        }
                    }
                    else throw(new InvalidDrawException("Fila non valida"));
                    resolveAction(a);
                    break;
                }
                break;
            }
        }

        if(!isInUpper){
            for(Card c : lowerList){
                if(c.equals(card)) {
                    for(Action a : toDoActions){
                        if(a.getType().equals(DrawCardEnum.DOWN_DRAW)){
                            if(c.getType().isEvent()){
                                throw new InvalidDrawException("Non puoi selezionare carte evento!");
                            }
                            else if(c.getType().equals(CardTypeEnum.BUILDING)){
                                Building building = (Building)c;
                                if(currPlayer.getNFood() >= building.getFoodCost()){
                                    currPlayer.addFood(-building.getFoodCost());
                                    currPlayer.addBuilding(building);
                                }
                                else throw(new InvalidDrawException("Non disponi del cibo necessario per acquistare l'edificio scelto!"));
                            }
                            else if(c.getType().isCharacter()){
                                currPlayer.addCard((Character)c);
                            }
                        }
                        else throw(new InvalidDrawException("Fila non valida"));
                        resolveAction(a);
                        break;
                    }
                    break;
                }
            }
        }

    }
}

