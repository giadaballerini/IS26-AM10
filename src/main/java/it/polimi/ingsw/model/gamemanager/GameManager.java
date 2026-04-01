package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.exceptions.OccupiedTileException;
import it.polimi.ingsw.model.GameInitializer;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.observer.GameEventListener;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;


import java.util.*;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.*;

public class GameManager {
    protected List<Card> deck;
    protected List<Card> buildings;
    protected Queue <Tile> queue;
    protected ArrayList<Tile> board;
    protected List<Card> upperList;
    protected List<Card> lowerList;
    private final int numPlayers;
    protected GamePhaseState currPhaseState;
    public int currAge;
    protected List<Player> players;
    private Player currPlayer;
    protected List<Action> toDoActions;
    List<GameEventListener> listeners;
    int currTurn;

    public GameManager(List<GameEventListener> listeners, List<Player> players, int numPlayers) {
        this.listeners = listeners;
        this.toDoActions = new ArrayList<Action>();
        this.currPlayer = null;
        this.players = players;
        this.currAge = 1;
        this.numPlayers = numPlayers;
        this.currPhaseState  = new SetupPhaseState();

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
        buildings = g.initBuildingDeck(numPlayers);
        lowerList = g.initLowerList(deck, upperList, numPlayers);
        upperList = g.initUpperList(deck, buildings,upperList, numPlayers);
        board = g.initBoard(numPlayers);
        queue = g.initQueue(numPlayers);

        Collections.shuffle(players);

        int i = 0;
        for(Tile t: queue){
            t.setPlayer(players.get(i));
            i++;
        }

        currTurn = 1;
        if(players != null && !players.isEmpty()){
                currPlayer = players.getFirst();
                initFood();
            }
    }

    private void initFood(){
        for(int i = 0; i < numPlayers; i++){
            Player p = players.get(i);
            int food = switch(i){
                case 0  -> 2;
                case 1,2 -> 3;
                case 3,4 -> 4;
                default -> 0;
            };
            p.addFood(food);
        }
    }

    public void nextPhase(){
        GamePhaseState oldPhase;
        oldPhase = currPhaseState;

        this.currPhaseState = currPhaseState.nextPhase(this);
        if(!oldPhase.equals(currPhaseState)){
            currPhaseState.onEntry(this);//per eseguire azioni di ingresso alla fase
        }
    }

    public void changeAge() {
        if (currAge < 3)
            currAge++;
        System.out.println("\nCurrent age: " + currAge);

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


    public void refillBoard() {

        lowerList.removeIf(c -> c.getType().isEvent() || c.getType().isCharacter());

        Iterator<Card> it = upperList.iterator();
        while (it.hasNext()) {
            Card c = it.next();
            if (c.getType().isCharacter() || c.getType().isEvent()) {
                lowerList.addFirst(c);
                it.remove();
            }
        }
        Card c;
        for(int i = 0; i < numPlayers + 4; i++) {
            if(!deck.isEmpty()){
                c = deck.removeFirst();
                if (c.getAge() > currAge) {
                    changeAge();
                }
                upperList.addFirst(c);
            }

        }
    }

    public void move(Tile t) throws OccupiedTileException{
        if(t.isOccupied())
            throw new OccupiedTileException("La posizione in cui si sta provando a spostare la pedina è occupata!");
        Tile removed = queue.poll();
        assert removed != null;
        removed.removePlayer();
        queue.add(removed);
        t.setPlayer(currPlayer);
        nextPhase();//also calls nextPlayer()
    }

    void checkEffects(){
        List<Action> list = currPlayer.checkBuildsEffects(currPhaseState.getPhase());
        if(!list.isEmpty())
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
        if(players.isEmpty())
            return new ArrayList<>();

        final int maxPP = players.stream()
                .mapToInt(Player::getPP)
                .max()
                .orElse(0);

        List<Player> winners = new ArrayList<>(players.stream()
                .filter(p -> p.getPP() == maxPP)
                .toList());


        if(winners.size() > 1) {
            final int maxFood = winners.stream()
                    .mapToInt(Player::getNFood)
                    .max()
                    .orElse(0);

            winners.removeIf(p -> p.getNFood() < maxFood);
        }

        nextPhase();
        return winners;
    }

    public Player getCurrentPlayer(){return currPlayer;}

    void nextPlayer(){
        if(board.stream().anyMatch(Tile::isOccupied) && (currPhaseState.getPhase() != GamePhaseEnum.SETUP_PHASE))
            currPlayer = board.stream()
                    .filter(Tile::isOccupied)
                    .map(Tile::getPlayer)
                    .findFirst()
                    .orElse(null);
        else
            currPlayer = queue.peek().getPlayer();
    }

    public void addListener(GameEventListener listener){
        listeners.add(listener);
    }

    public List<Action> getToDoActions(){
        return new ArrayList<>(toDoActions);
    }

    void playEvent() {

        List<Card> targetList = new ArrayList<>();
        if (currPhaseState.getPhase() == END_ROUND)
            targetList = lowerList;
        else
            targetList = upperList;

        PlayEventVisitor visitor = new PlayEventVisitor(this.players, currPhaseState.getPhase());

        for (Card c : targetList) {
            c.accept(visitor);
        }
        visitor.feastIfPresent();
        nextPhase(); // per passare a setup/endgame
    }

    private void checkTileEffects(Collection<Tile> tiles){
        for(Tile t : tiles){
            if(t.isOccupied() && t.getPlayer().equals(currPlayer)){
                t.execInstantEffect(currPhaseState.getPhase());
                List<Action> actions = t.execInteractiveEffect();
                toDoActions.addAll(actions);
            }
        }
    }

    void checkBoardTileEffects(){
        checkTileEffects(board);
        nextPhase(); // in caso di tile con soli effetti instant questa mi permette di passare alla endTurn
    }

    private void checkQueueTileEffects(){
        checkTileEffects(queue);
    }

    private void resolveAction(Action a){
        toDoActions.remove(a);
    }

    public void drawCard(Card card) throws InvalidDrawException {
        boolean isInUpper = false;
        DrawCardVisitor visitor = new DrawCardVisitor(currPlayer);
        for(Card c: upperList) {
            if (c.equals(card)) {
                isInUpper = true;
                for(Action a : toDoActions) {
                    if (a.getType().equals(DrawCardEnum.UP_DRAW)){
                        c.accept(visitor);
                        if (visitor.hasErrorMessage()) throw new InvalidDrawException(visitor.getErrorMessage());
                    }
                    else throw(new InvalidDrawException("Fila non valida"));
                    resolveAction(a);
                    upperList.remove(c);
                    break;
                }
                break;
            }
        }

        if(!isInUpper){
            for(Card c : lowerList){
                if(c.equals(card)) {
                    for(Action a : toDoActions){
                        if (a.getType().equals(DrawCardEnum.DOWN_DRAW)){
                            c.accept(visitor);
                            if (visitor.hasErrorMessage()) throw new InvalidDrawException(visitor.getErrorMessage());
                        }
                        else throw(new InvalidDrawException("Fila non valida"));
                        resolveAction(a);
                        lowerList.remove(c);
                        break;
                    }
                    break;
                }
            }
        }

        card.execInstantEffect(currPlayer, currPhaseState.getPhase());
        card.execInteractiveEffect(currPlayer);
        //checkEndTurn();
        nextPhase(); // questa mi permette di passare alla end turn dopo aver terminato il pescaggio delle carte
    }

    boolean isQueueEmpty(){
        return !(Optional.ofNullable(queue.peek())
                .map(Tile::isOccupied)
                .orElse(false));
    }

    int getQueueSize(){
        return Math.toIntExact(queue.stream().filter(Tile::isOccupied).count());
    }

    int getNumPlayers(){
        return players.size();
    }

    void incrementTurn(){
        currTurn++;
    }

    int getCurrTurn(){
        return currTurn;
    }


    void execEndTurn(){
        removeFromBoard();
        assert queue.peek() != null;

        Objects.requireNonNull(queue.stream()
                .filter(tile -> !tile.isOccupied())
                .findFirst()
                .orElse(null)).setPlayer(currPlayer);


        checkQueueTileEffects();
        nextPlayer();
        nextPhase();

    }



    private void removeFromBoard(){
        Tile occupiedTile;
        occupiedTile = board.stream().filter(tile -> tile.isOccupied() && tile.getPlayer().equals(currPlayer)).findFirst().orElse(null);
        assert occupiedTile != null;
        occupiedTile.removePlayer();
    }

    public boolean checkCorrectPhase(GamePhaseEnum gamePhaseEnum) {
        return gamePhaseEnum == currPhaseState.getPhase();
    }

    public boolean checkCorrectPlayer(Player p) {
        return currPlayer.equals(p);
    }
}

