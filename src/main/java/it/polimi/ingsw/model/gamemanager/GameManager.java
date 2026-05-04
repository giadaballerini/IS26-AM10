package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.GameInitializer;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.card.types.building.Building;
import it.polimi.ingsw.model.entities.card.types.event.Event;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import it.polimi.ingsw.visitors.DrawCardVisitor;
import it.polimi.ingsw.visitors.PlayEventVisitor;


import java.util.*;
import java.util.stream.Stream;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.*;

public class GameManager implements ApplicableActions{
    protected List<Card> deck;
    protected List<Card> buildings;
    protected List <Tile> queue;
    protected ArrayList<Tile> board;
    protected List<Card> upperList;
    protected List<Card> lowerList;
    private final int numPlayers;
    protected GamePhaseState currPhaseState;
    public int currAge;
    protected List<Player> players;
    private Player currPlayer;
    protected List<Action> toDoActions;
    List<ModelObserver> listeners;
    boolean skippableDraw;
    int currTurn;

    public GameManager(List<ModelObserver> listeners, List<Player> players, int numPlayers) {
        this.listeners = listeners;
        this.toDoActions = new ArrayList<Action>();
        this.currPlayer = null;
        this.players = players;
        this.currAge = 1;
        this.numPlayers = numPlayers;
        this.currPhaseState  = new SetupPhaseState();
        this.skippableDraw = false;

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
        if(players != null && !players.isEmpty()) {
            currPlayer = players.getFirst();
            initFood();
        }
        System.out.println("Game avviato correttamente!");
        System.out.println("Partita da: " + numPlayers + " giocatori\n");
        showBoard();
        nextPhase();
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
        //notifyPhaseUpdate();
        if(!oldPhase.equals(currPhaseState)){
            notifyPhaseUpdate();
            currPhaseState.onEntry(this);//per eseguire azioni di ingresso alla fase
        }
    }

    public void changeAge() {
        if(currAge < 3)
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

        notifyChangeAge();
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
        // COME NOTIFY BASTA LO SHOW BOARD CHE FACCIAMO GIA' ALLA FINE DEL ROUND
    }

    public void onMoveRequested(String nick, int tilePos) throws OccupiedTileException, InvalidPhaseException, InvalidPlayerException, InvalidMoveException{

        System.out.println("onMoveRequest called");
        if (!checkCorrectPhase(GamePhaseEnum.SETUP_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        else if (!(checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }

        Tile t = getTileById(tilePos);
        if(t == null)
            throw new InvalidMoveException("SELEZIONE NON VALIDA");

        if(t.isOccupied())
            throw new OccupiedTileException("LA POSIZIONE A CUI SI STA PROVANDO AD ACCEDERE È OCCUPATA");

        move(t);
    }

    void move(Tile t){
        Tile removed = queue.removeFirst();
        assert removed != null;
        removed.removePlayer();
        queue.add(removed);
        t.setPlayer(currPlayer);
        notifyMoveUpdate(t);
        nextPhase();
    }

    void checkEffects(){
        List<Action> list = currPlayer.checkBuildsEffects(currPhaseState.getPhase());
        if(!list.isEmpty()) {
            toDoActions.addAll(list);
            notifyDrawable();
        }
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
        return winners;
    }

    private Map<Integer, List<Player>> calculateFinalRanking(){
        if(this.players == null || this.players.isEmpty())
           return new TreeMap<>();
        List<Player> sortedPlayers = this.players.stream()
                .sorted(Comparator.comparing(Player::getPP, Comparator.reverseOrder())
                        .thenComparing(Player::getNFood, Comparator.reverseOrder()))
                .toList();
        Map<Integer, List<Player>> finalRanking = new TreeMap<>();

        int currentRank = 1;

        for(int i = 0; i < sortedPlayers.size(); i++){
            Player cPlayer = sortedPlayers.get(i);
            if(i > 0){
                Player prevPlayer = sortedPlayers.get(i-1);
                boolean identicalStats = cPlayer.getPP() == prevPlayer.getPP() && cPlayer.getNFood() == prevPlayer.getNFood();
                if(!identicalStats){
                    currentRank = i + 1;
                }
            }
            finalRanking.computeIfAbsent(currentRank, k -> new ArrayList<>()).add(cPlayer);
        }

        return finalRanking;
    }

    public Map<Player, Integer> calculateRankingPoints(){
        Map<Integer, List<Player>> finalRanking = calculateFinalRanking();
        Map<Player, Integer> rankingPoints = new HashMap<>();

        for(Map.Entry<Integer, List<Player>> entry : finalRanking.entrySet()){
            List<Player> players = entry.getValue();
            int rank = entry.getKey();
            int pointsToAssign = switch (this.numPlayers){
                case 2 -> (rank == 1) ? 1 : 0;
                case 3 -> switch(rank){
                    case 1 -> 1;
                    case 2 -> 0;
                    default -> -1;
                };
                case 4-> switch(rank){
                    case 1 -> 2;
                    case 2 -> 1;
                    case 3 -> 0;
                    default -> -1;
                };
                case 5 -> switch(rank){
                    case 1 -> 2;
                    case 2 -> 1;
                    case 3 -> 0;
                    case 4 -> -1;
                    default -> -2;
                };
                default -> 0;
            };
            for (Player player : players) {
                rankingPoints.put(player, pointsToAssign);
            }
        }
        return rankingPoints;
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
            currPlayer = queue.getFirst().getPlayer();
        notifyCurrPlayerUpdate();
    }

    public void addListener(ModelObserver listener){
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
            String type = visitor.getEventType();
            if(!type.isEmpty()) {
                notifyEventUpdate(type);
                visitor.resetEvent();
            }
        }
        visitor.feastIfPresent();
        if(!visitor.getEventType().isEmpty()) {
            notifyEventUpdate(visitor.getEventType());
        }
        notifyDrawable();
        nextPhase(); // per passare a setup/endgame
    }

    private void checkTileEffects(Collection<Tile> tiles){
        for(Tile t : tiles){
            if(t.isOccupied() && t.getPlayer().equals(currPlayer)){
                t.execInstantEffect();
                List<Action> actions = t.execInteractiveEffect();
                toDoActions.addAll(actions);
                notifyDrawable();
            }
        }
    }

    void checkBoardTileEffects(){
        checkTileEffects(board);
        nextPhase(); // in caso di tile con soli effetti instant questa mi permette di passare alla endTurn
    }

    private void checkQueueTileEffects(){
        for(Tile t : queue){
            if(t.isOccupied() && t.getPlayer().equals(currPlayer)){
                if(t.getPlayer().hasExtraFlag()){
                    if(t.hasGainFoodEffect())
                        t.getPlayer().addFood(1);
                }
                t.execInstantEffect();
                List<Action> actions = t.execInteractiveEffect();
                toDoActions.addAll(actions);
                notifyDrawable();
            }
        }
    }

    private void resolveAction(Action a){
        toDoActions.remove(a);
        notifyDrawable();
    }

    public void onDrawCardRequested(String nick,int cardID) throws InvalidPhaseException, InvalidPlayerException, InvalidDrawException{
        if (!checkCorrectPhase(GamePhaseEnum.DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        else if (!(checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }
        Card card = getCardById(cardID);

        if(card==null)
            throw new InvalidDrawException("CARTA NON ESISTENTE");

        drawCard(card);

    }
    void drawCard(Card card) throws InvalidDrawException,InvalidPlayerException,InvalidPhaseException{
        DrawCardVisitor visitor = new DrawCardVisitor(currPlayer);
        boolean isInUpper = upperList.stream()
                .anyMatch(c -> c.equals(card));

       DrawCardEnum requiredDraw = isInUpper ? DrawCardEnum.UP_DRAW : DrawCardEnum.DOWN_DRAW;

       Action toDoAction = toDoActions.stream()
                       .filter(a -> a.getType().equals(requiredDraw))
                               .findFirst()
                                       .orElseThrow(() -> new InvalidDrawException("Non hai pescate disponibili dalla fila " + (isInUpper ? "superiore" : "inferiore")));

       card.accept(visitor);
       if(visitor.hasErrorMessage())
           throw new InvalidDrawException(visitor.getErrorMessage());
       resolveAction(toDoAction);
       if(isInUpper)
           upperList.remove(card);
       else
           lowerList.remove(card);

        card.execInstantEffect(currPlayer, currPhaseState.getPhase());
        card.execInteractiveEffect(currPlayer);
        checkEffects();
        notifyDrawUpdate(card);
        notifyStatsUpdate(card);
        notifyStatusUpdate();

        nextPhase(); // questa mi permette di passare alla end turn dopo aver terminato il pescaggio delle carte
    }
    public void onSkipRequested(String nick) throws InvalidPhaseException, InvalidPlayerException, InvalidSkipException{
        if (!checkCorrectPhase(GamePhaseEnum.DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        else if (!(checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }
        else if (!getSkippableDraw())
            throw new InvalidSkipException("NON È POSSIBILE SALTARE IL TURNO ADESSO");

        skipDraw();
    }


    void skipDraw(){
        toDoActions.clear();
        notifySkip();
        nextPhase();
    }

    boolean isQueueEmpty(){
        return !(Optional.ofNullable(queue.getFirst())
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
        assert queue.getFirst() != null;

        Tile t = Objects.requireNonNull(queue.stream()
                .filter(tile -> !tile.isOccupied())
                .findFirst()
                .orElse(null));
        t.setPlayer(currPlayer);


        checkQueueTileEffects();
        notifyReturnToQueue(t);
        nextPlayer();
        nextPhase();
    }


    private void removeFromBoard() {
        Tile occupiedTile;
        occupiedTile = board.stream().filter(tile -> tile.isOccupied() && tile.getPlayer().equals(currPlayer)).findFirst().orElse(null);
        assert occupiedTile != null;
        occupiedTile.removePlayer();
    }

    public boolean checkCorrectPhase(GamePhaseEnum gamePhaseEnum) {
        return gamePhaseEnum == currPhaseState.getPhase();
    }

    public boolean checkCorrectPlayer(String p) {
        return currPlayer.getNickname().equals(p);
    }
    public void setCurrPlayer(Player p) {
        currPlayer = p;
    }

    public void checkCanDraw() {
        CanDrawVisitor cd = new CanDrawVisitor(currPlayer);
        if(toDoActions.stream().anyMatch(a -> a.getType() == DrawCardEnum.UP_DRAW)){
           for(int i = 0; i < upperList.size(); i++){
               upperList.get(i).accept(cd);
               if(cd.getMustDraw())
                   break;
           }
        }
        if(!cd.getMustDraw() && toDoActions.stream().filter(a -> a.getType()==DrawCardEnum.DOWN_DRAW).count() > 0){
            for(int i = 0; i < lowerList.size(); i++){
                lowerList.get(i).accept(cd);
                if(cd.getMustDraw())
                    break;
            }
        }

        if(!cd.getMustDraw()){
            if(!cd.getMayDraw()) {
                skipDraw();
                // avvenuto skip
            }
            else {
                System.out.println("Puoi pescare o decidere di finire il turno");
                skippableDraw = true;
                //notify view di possibilità di skip
            }
        }
        else
            System.out.println("Devi pescare");
            //notify view di obbligatorietà di draw
    }
    void setSkippableDraw(boolean canSkip){
        this.skippableDraw = canSkip;
    }

    public boolean getSkippableDraw(){ return this.skippableDraw; }

    public ChangeAgeDTO genChangeAgeDTO(){
        return new ChangeAgeDTO(upperList.stream().map(Card::toDTO).toList(),
                lowerList.stream().map(Card::toDTO).toList(), currAge);
    }
    public BoardDTO toDTO(){
        return new BoardDTO(
                upperList.stream().map(Card::toDTO).toList(),
                lowerList.stream().map(Card::toDTO).toList(),
                players.stream().map(Player::toDTO).toList(),
                board.stream().map(Tile::toDTO).toList(),
                queue.stream().map(Tile::toDTO).toList(),
                players.stream().map(Player::toStatsDTO).toList(),
                currPlayer.getNickname(),
                currPhaseState.getPhase(),
                currTurn,
                numPlayers
        );
    }

    public Tile getTileById(int id){
        if (id < 0 || id >= board.size()) {
            return null;
        }

        return board.get(id);
    }

    public Card getCardById(int id){
        return Stream.of(upperList, lowerList).flatMap(List::stream)
                .filter(c->c.getId()==id)
                .findFirst()
                .orElse(null);
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

    public ModelObserver getCurrentListener(){
        return listeners.stream().filter(l->l.getNickname().equals(currPlayer.getNickname())).findFirst().orElse(null);
    }

    // Metodi di notifica

    public void notifyCurrPlayerUpdate(){
        for(ModelObserver c: listeners){
            c.onCurrPlayerUpdate(currPlayer.getNickname());
        }
    }


    public void notifyMoveUpdate(Tile tile){
        TileDTO tdto = tile.toDTO();
        for(ModelObserver c: listeners){
            c.onMoveUpdate(tdto, currPlayer.getNickname());
        }
    }

    public void notifyPhaseUpdate(){
        PhaseDTO phaseDTO = new PhaseDTO(currPhaseState.getPhase());
        for (ModelObserver c: listeners){
            c.onPhaseUpdate(phaseDTO);
        }
    }


    public void notifyGameEnding(){
        Map<Integer, List<Player>> finalRanking = calculateFinalRanking();

        List<PlayerStatsDTO> statsList = players.stream()
                .map(Player::toStatsDTO)
                .toList();

        for(ModelObserver listener: listeners){
            int rankingPos = -1;
            for(Map.Entry<Integer, List<Player>> entry: finalRanking.entrySet()){
                if(entry.getValue().stream().anyMatch(p -> p.getNickname().equals(listener.getNickname()))){
                    rankingPos = entry.getKey();
                    break;
                }
            }
            listener.onGameEnding(statsList, rankingPos);
        }
    }

    public void notifyDrawUpdate(Card card){
        CardDTO cardDTO = card.toDTO();
        for (ModelObserver c : listeners){
            c.onDrawUpdate(cardDTO, currPlayer.getNickname());
        }
    }

    public void notifyStatusUpdate(){
        //se aggiungiamo queue visive nella gui per quando si attivano le flag si fa per ogni player, altrimenti solo per currPlayer
        PlayerStatusDTO status = currPlayer.toStatusDTO();
        for (ModelObserver c : listeners){
            c.onStatusUpdate(status);
        }
    }

    public void notifyStatsUpdate(Card card){
        PlayerStatsDTO statsDto = currPlayer.toStatsDTO();
        for(ModelObserver c: listeners){
            c.onStatsUpdate(statsDto,card.getId());
        }
    }

    public void notifyEventUpdate(String event){
        for(ModelObserver c: listeners) {
            c.onEvent(event);
        }
    }

    //List<PlayerDTO> listPlayers, BoardDTO board
    public void refresh(){
        List<PlayerDTO> playersdto = new ArrayList<>();
        for(Player p: players){
            playersdto.add(p.toDTO());
        }
        BoardDTO b = toDTO();
        for(ModelObserver c: listeners){
            c.refresh(playersdto,b);
        }
    }

    void showBoard(){
        BoardDTO b = toDTO();
        for(ModelObserver c: listeners){
            c.showBoard(b);
        }
    }

    public void notifySkip(){
        for(ModelObserver c: listeners){
            c.notifySkip(currPlayer.getNickname());
        }
    }

    public void notifyDrawable(){
        ActionsDTO dto = toActionsDTO();
        ModelObserver c = getCurrentListener();
        c.notifyDrawable(dto);
    }

    public void notifyReturnToQueue(Tile t){
        TileDTO tdto = t.toDTO();
        PlayerStatsDTO statsdto = currPlayer.toStatsDTO();
        for(ModelObserver c: listeners){
            c.onReturnToQueue (tdto, statsdto);
        }
    }

    public void notifyChangeAge(){
        ChangeAgeDTO dto = genChangeAgeDTO();
        for(ModelObserver c: listeners){
            c.onChangeAge(dto);
        }
    }

}

