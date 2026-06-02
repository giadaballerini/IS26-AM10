package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidDrawException;
import it.polimi.ingsw.model.GameInitializer;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.ActionsDTO;
import it.polimi.ingsw.network.dto.BoardDTO;
import it.polimi.ingsw.network.dto.ChangeAgeDTO;
import it.polimi.ingsw.network.dto.EventDTO;
import it.polimi.ingsw.persistency.GameSnapshot;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.SETUP_PHASE;

public class GameState {
    private List<Card> deck;
    private List<Card> buildings;
    private List <Tile> queue;
    private List<Tile> board;
    private List<Card> upperList;
    private List<Card> lowerList;
    private List<Player> players;
    private Player currPlayer;
    private int currAge;
    private int currTurn;
    private boolean skippableDraw;
    private List<Action> toDoActions;
    private final int numPlayers;




    public GameState(List<Player> players, int numPlayers){
        this.players = players;
        this.numPlayers = numPlayers;
        this.currAge = 1;
        this.currTurn = 1;
        this.currPlayer = null;
        this.skippableDraw = false;
        this.toDoActions = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.buildings = new ArrayList<>();
        this.lowerList = new ArrayList<>();
        this.upperList = new ArrayList<>();
        this.board = new ArrayList<>();
        this.queue = new LinkedList<>();
    }


    public Card getCardById(int id){
        return Stream.of(upperList, lowerList).flatMap(List::stream)
                .filter(c->c.getId()==id)
                .findFirst()
                .orElse(null);
    }

    public Tile getTileById(int id){
        if (id < 0 || id >= board.size()) {
            return null;
        }
        return board.get(id);
    }

    public boolean getSkippableDraw(){ return this.skippableDraw; }


    public List<Action> getToDoActions(){
        return new ArrayList<>(toDoActions);
    }

    int getQueueSize(){
        return Math.toIntExact(queue.stream().filter(Tile::isOccupied).count());
    }

    int getNumPlayers(){
        return this.numPlayers;
    }

    void incrementTurn(){
        currTurn++;
    }

    int getCurrTurn(){
        return currTurn;
    }

    void setSkippableDraw(boolean canSkip){
        this.skippableDraw = canSkip;
    }

    public void setCurrPlayer(Player p) {
        currPlayer = p;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    public List<Tile> getQueue() {
        return queue;
    }

    public void setQueue(List<Tile> queue) {
        this.queue = queue;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<Card> getBuildings() {
        return buildings;
    }

    public List<Tile> getBoard() {
        return board;
    }

    public void setBoard(ArrayList<Tile> board) {
        this.board = board;
    }

    public List<Card> getUpperList() {
        return upperList;
    }

    public void setUpperList(List<Card> upperList) {
        this.upperList = upperList;
    }

    public List<Card> getLowerList() {
        return lowerList;
    }

    public void setLowerList(List<Card> lowerList) {
        this.lowerList = lowerList;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getCurrPlayer() {
        return currPlayer;
    }

    public int getCurrAge() {
        return currAge;
    }

    public void setCurrAge(int currAge) {
        this.currAge = currAge;
    }

    public void setCurrTurn(int currTurn) {
        this.currTurn = currTurn;
    }

    public void setToDoActions(List<Action> toDoActions) {
        this.toDoActions = toDoActions;
    }

    public void setBuildings(List<Card> buildings) {
        this.buildings = buildings;
    }

    public void initialize() {
        GameInitializer g = new GameInitializer();

        List<Card> deck = g.initDeck(numPlayers);
        List<Card> buildings = g.initBuildingDeck(numPlayers);
        List<Card> upperList = new ArrayList<>();
        List<Card> lowerList = g.initLowerList(deck, upperList, numPlayers);
        g.initUpperList(deck, buildings, upperList, numPlayers);

        this.deck = deck;
        this.buildings = buildings;
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.board = g.initBoard(numPlayers);
        this.queue = g.initQueue(numPlayers);

        Collections.shuffle(players);

        int i = 0;
        for (Tile t : queue) {
            t.setPlayer(players.get(i));
            i++;
        }

        currTurn = 1;
        currPlayer = players.isEmpty() ? null : players.getFirst();

        assignInitialFood();

    }

    private void assignInitialFood() {
        for (int i = 0; i < players.size(); i++) {
            int food = switch (i) {
                case 0    -> 2;
                case 1, 2 -> 3;
                case 3, 4 -> 4;
                default   -> 0;
            };
            players.get(i).addFood(food);
        }
    }

    public void advanceAge() {
        if (currAge < 3)
            currAge++;

        if (currAge == 3)
            lowerList.removeIf(c -> c.getType().equals(CardTypeEnum.BUILDING));

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

    public boolean hasAnySkippableDraws(){
        return players.stream().anyMatch(Player::hasSkippableDraws);
    }

    public boolean loadSkippableDraws() {
        Player p = players.stream()
                .filter(Player::hasSkippableDraws)
                .findFirst()
                .orElse(null);

        if (p == null) return false;

        currPlayer = p;
        skippableDraw = true;
        toDoActions.addAll(p.resolveSkippableDraws());
        return true;
    }

    public boolean refillBoard() {
        boolean ageChanged = false;

        lowerList.removeIf(c -> c.getType().isEvent() || c.getType().isCharacter());

        Iterator<Card> it = upperList.iterator();
        while (it.hasNext()) {
            Card c = it.next();
            if (c.getType().isCharacter() || c.getType().isEvent()) {
                lowerList.addFirst(c);
                it.remove();
            }
        }

        for (int i = 0; i < numPlayers + 4; i++) {
            if (!deck.isEmpty()) {
                Card c = deck.removeFirst();
                if (c.getAge() > currAge) {
                    ageChanged = true;
                }
                upperList.addFirst(c);
            }
        }

        return ageChanged;
    }

    public void applyMove(Tile t) {
        Tile removed = queue.removeFirst();
        removed.removePlayer();
        queue.add(removed);
        t.setPlayer(currPlayer);
    }

    public void applyFinalScores(GamePhaseEnum phase) {
        for (Player p : players) {
            p.addPP(calculateFinalPlayerScore(p));

            List<Action> endGameActions = p.checkBuildsEffects(phase);
            if (!endGameActions.isEmpty()) {
                p.addSkippableDraws(endGameActions);
                toDoActions.addAll(endGameActions);
            }
        }
    }

    private int calculateFinalPlayerScore(Player p) {
        int score = 0;
        score += p.getBuilderPoints();
        score += p.getNumType(CardTypeEnum.CRAFTER) * p.getTotSymbolsForCrafter();
        score += 10 * (p.getNumType(CardTypeEnum.PAINTER) / 2);
        score += p.getBuildings().stream().mapToInt(Building::getPpValue).sum();
        return score;
    }

    public void applyNextPlayer(GamePhaseEnum currPhaseState) {
        if(board.stream().anyMatch(Tile::isOccupied) && (currPhaseState != SETUP_PHASE))
            currPlayer = board.stream()
                    .filter(Tile::isOccupied)
                    .map(Tile::getPlayer)
                    .findFirst()
                    .orElse(null);
        else
            currPlayer = queue.getFirst().getPlayer();
    }

    public EventDTO applyEvents(GamePhaseEnum phase) {
        EventDTO events = new EventDTO();

        List<Card> targetList = phase == GamePhaseEnum.END_ROUND ? lowerList : upperList;

        PlayEventVisitor visitor = new PlayEventVisitor(players, phase, events);
        for (Card c : targetList) {
            c.accept(visitor);
        }
        visitor.feastIfPresent();

        players.forEach(p -> events.addStats(p.toStatsDTO()));

        return events;
    }

    private void applyTileEffects(Collection<Tile> tiles) {
        for (Tile t : tiles) {
            if (t.isOccupied() && t.getPlayer().equals(currPlayer)) {
                t.execInstantEffect();
                toDoActions.addAll(t.execInteractiveEffect());
            }
        }
    }

    void checkBoardTileEffects(){
        applyTileEffects(board);
    }

    public void applyQueueTileEffects() {
        for (Tile t : queue) {
            if (t.isOccupied() && t.getPlayer().equals(currPlayer)) {
                t.applyQueueBonus(currPlayer);
                t.execInstantEffect();
                toDoActions.addAll(t.execInteractiveEffect());
            }
        }
    }

    public void applyDraw(Card card, GamePhaseEnum phase) throws InvalidDrawException {
        boolean isInUpper = upperList.stream().anyMatch(c -> c.equals(card));
        DrawCardEnum requiredDraw = isInUpper ? DrawCardEnum.UP_DRAW : DrawCardEnum.DOWN_DRAW;

        Action toDoAction = toDoActions.stream()
                .filter(a -> a.getType().equals(requiredDraw))
                .findFirst()
                .orElseThrow(() -> new InvalidDrawException(
                        "Non hai pescate disponibili dalla fila " + (isInUpper ? "superiore" : "inferiore")));

        DrawCardVisitor visitor = new DrawCardVisitor(currPlayer);
        card.accept(visitor);
        if (visitor.hasErrorMessage())
            throw new InvalidDrawException(visitor.getErrorMessage());

        resolveAction(toDoAction);

        if (isInUpper) upperList.remove(card);
        else lowerList.remove(card);

        card.execInstantEffect(currPlayer, phase);
        card.execInteractiveEffect(currPlayer);
    }

    private void resolveAction(Action action) {
        toDoActions.remove(action);
    }

    public void applySkip(){
        toDoActions.clear();
    }

    boolean isQueueEmpty(){
        return !queue.getFirst().isOccupied();
    }

    public Tile applyEndTurn() {
        removeFromBoard();

        Tile t = queue.stream()
                .filter(tile -> !tile.isOccupied())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nessuna tile libera in coda"));

        t.setPlayer(currPlayer);
        applyQueueTileEffects();
        return t;
    }

    private void removeFromBoard() {
        board.stream().filter(tile -> tile.isOccupied() && tile.getPlayer().equals(currPlayer))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(currPlayer.getNickname() + " non presente sulla board."))
                .removePlayer();
    }

    public boolean checkCorrectPlayer(String p) {
        return currPlayer.getNickname().equals(p);
    }

    public CanDrawVisitor buildCanDrawVisitor() {
        CanDrawVisitor cd = new CanDrawVisitor(currPlayer);

        if (toDoActions.stream().anyMatch(a -> a.getType() == DrawCardEnum.UP_DRAW)) {
            for (Card c : upperList) {
                c.accept(cd);
                if (cd.getMustDraw()) return cd;
            }
        }

        if (!cd.getMustDraw() &&
                toDoActions.stream().anyMatch(a -> a.getType() == DrawCardEnum.DOWN_DRAW)) {
            for (Card c : lowerList) {
                c.accept(cd);
                if (cd.getMustDraw()) return cd;
            }
        }

        return cd;
    }

    public GameSnapshot toSnapshot(int matchId, GamePhaseEnum phase) {
        List<GameSnapshot.PendingAction> pendingActions = toDoActions.stream()
                .map(a -> new GameSnapshot.PendingAction(
                        a.getOwner() != null ? a.getOwner().getNickname() : "SYSTEM",
                        a.getType()))
                .collect(Collectors.toList());

        return new GameSnapshot(
                matchId,
                numPlayers,
                new ArrayList<>(deck),
                new ArrayList<>(buildings),
                new ArrayList<>(upperList),
                new ArrayList<>(lowerList),
                new ArrayList<>(board),
                new ArrayList<>(queue),
                new ArrayList<>(players),
                currPlayer != null ? currPlayer.getNickname() : "",
                phase,
                currAge,
                currTurn,
                skippableDraw,
                pendingActions
        );
    }

    public ChangeAgeDTO genChangeAgeDTO(){
        return new ChangeAgeDTO(upperList.stream().map(Card::toDTO).toList(),
                lowerList.stream().map(Card::toDTO).toList(), currAge);
    }
    public BoardDTO toDTO(GamePhaseState currPhaseState){
        return new BoardDTO(
                upperList.stream().map(Card::toDTO).toList(),
                lowerList.stream().map(Card::toDTO).toList(),
                players.stream().map(Player::toDTO).toList(),
                board.stream().map(Tile::toDTO).toList(),
                queue.stream().map(Tile::toDTO).toList(),
                players.stream().map(Player::toStatsDTO).toList(),
                currPlayer.getNickname(),
                toActionsDTO(),
                currPhaseState.getPhase(),
                currTurn,
                numPlayers
        );
    }

    public ActionsDTO toActionsDTO(){
        int up = 0;
        int down = 0;
        for (Action a : toDoActions) {
            if (a.getType() == DrawCardEnum.UP_DRAW) up++;
            else if (a.getType() == DrawCardEnum.DOWN_DRAW) down++;
        }
        return new ActionsDTO(up, down, skippableDraw);
    }
}
