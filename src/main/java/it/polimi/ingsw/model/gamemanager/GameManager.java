package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.interfaces.Snapshotable;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;
import it.polimi.ingsw.visitors.CanDrawVisitor;
import java.util.*;
import java.util.logging.Logger;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.SETUP_PHASE;
import static it.polimi.ingsw.enumerations.GamePhaseEnum.DRAW_PHASE;
import static it.polimi.ingsw.enumerations.GamePhaseEnum.OPTIONAL_DRAW_PHASE;


/**
 * Central controller for a single game session.
 *
 * <p>{@code GameManager} coordinates all game activity by implementing the
 * State pattern: it holds a reference to the active {@link GamePhaseState}
 * and delegates phase-specific logic to it. It also implements
 * {@link ApplicableActions} to expose the three player-driven entry points
 * (move, draw, skip) and {@link Snapshotable} to support game persistence.</p>
 *
 * <p>Observers registered via {@link GameNotifier} are notified of every
 * state change so that connected clients can keep their views up to date.</p>
 */
public class GameManager implements ApplicableActions, Snapshotable {
    protected static final Logger LOG = Logger.getLogger(GameManager.class.getName());

    protected GamePhaseState currPhaseState;
    private final Runnable onGameEndedCallback;
    private Runnable onGameStartedCallback;
    protected GameState state;
    protected GameNotifier notifier;
    protected   RankingCalculator rankingCalculator;


    public GameManager(List<ModelObserver> listeners, List<Player> players, int numPlayers, Runnable onGameEndedCallback) {
        state = new GameState(players, numPlayers);
        notifier = new GameNotifier(listeners);
        rankingCalculator = new RankingCalculator();
        currPhaseState = new SetupPhaseState();
        this.onGameEndedCallback = onGameEndedCallback;
    }

    public void initGame() {
        state.initialize();

        notifier.showBoard(state.toDTO(currPhaseState));
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());

        if (onGameStartedCallback != null)
            onGameStartedCallback.run();

        LOG.info("Game avviato correttamente!");
    }


    public void nextPhase(){
        GamePhaseState oldPhase;
        oldPhase = currPhaseState;

        this.currPhaseState = currPhaseState.nextPhase(this);
        if(!oldPhase.equals(currPhaseState)){
            notifier.notifyPhaseUpdate(currPhaseState);
            currPhaseState.onEntry(this);
        }
    }

    public void changeAge() {

        state.advanceAge();
        notifier.notifyChangeAge(state.genChangeAgeDTO());
    }


    public void loadSkippableDraws(){
        if(!state.loadSkippableDraws())
            return;
        String currPlayer = state.getCurrPlayer().getNickname();
        notifier.notifyCurrPlayerUpdate(currPlayer);
        notifier.notifyDrawable(state.toActionsDTO(), currPlayer);
    }

    public void refillBoard() {
        if(state.refillBoard())
            changeAge();
    }

    void checkEffects(){
        Player currPlayer = state.getCurrPlayer();
        List<Action> effects = currPlayer.checkBuildsEffects(currPhaseState.getPhase());
        if(!effects.isEmpty()) {
            currPlayer.addSkippableDraws(effects);
        }
    }

    public void finalScoreCount(){
        state.applyFinalScores(currPhaseState.getPhase());
    }

    void nextPlayer(){
        state.applyNextPlayer(currPhaseState.getPhase());
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());
    }

    void playEvent() {
        EventDTO events = state.applyEvents(currPhaseState.getPhase());
        notifier.notifyEventUpdate(events);
        nextPhase();
    }

    private void checkQueueTileEffects(){
        state.applyQueueTileEffects();
    }



    public void onMoveRequested(String nick, int tilePos) throws OccupiedTileException, InvalidPhaseException, InvalidPlayerException, InvalidMoveException{

        if (!checkCorrectPhase(SETUP_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        if (!(state.checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }

        Tile t = state.getTileById(tilePos);
        if(t == null)
            throw new InvalidMoveException("SELEZIONE NON VALIDA");

        if(t.isOccupied())
            throw new OccupiedTileException("LA POSIZIONE A CUI SI STA PROVANDO AD ACCEDERE È OCCUPATA");
        move(t);
    }

    void move(Tile t){
        state.applyMove(t);
        notifier.notifyMoveUpdate(t.toDTO(), state.getCurrPlayer().getNickname());
        nextPhase();
    }

    public void onDrawCardRequested(String nick,int cardID) throws InvalidPhaseException, InvalidPlayerException, InvalidDrawException{
        if (!checkCorrectPhase(DRAW_PHASE) && !checkCorrectPhase(OPTIONAL_DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        if (!(state.checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }
        Card card = state.getCardById(cardID);

        if(card==null)
            throw new InvalidDrawException("CARTA NON ESISTENTE");

        drawCard(card);

    }

    void drawCard(Card card) throws InvalidDrawException,InvalidPlayerException,InvalidPhaseException{
        Player currPlayer = state.getCurrPlayer();
        state.applyDraw(card, currPhaseState.getPhase());
        checkEffects();
        notifier.notifyDrawUpdate(currPlayer,card);
        notifier.notifyStatsUpdate(currPlayer,card);
        notifier.notifyStatusUpdate(currPlayer);
        if(!state.getToDoActions().isEmpty())
            checkCanDraw();
        else
            nextPhase();
    }

    public void onSkipRequested(String nick) throws InvalidPhaseException, InvalidPlayerException, InvalidSkipException{
        if (!checkCorrectPhase(DRAW_PHASE) && !checkCorrectPhase(OPTIONAL_DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        else if (!(state.checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("GIOCATORE INVALIDO");
        }
        else if (!state.getSkippableDraw())
            throw new InvalidSkipException("NON È POSSIBILE SALTARE IL TURNO ADESSO");
        skipDraw();
    }

    void skipDraw(){
        state.applySkip();
        notifier.notifyDrawable(state.toActionsDTO(), state.getCurrPlayer().getNickname());
        notifier.notifySkip(state.getCurrPlayer());
        nextPhase();
    }

    void execEndTurn(){
        Tile t = state.applyEndTurn();
        checkEffects();
        notifier.notifyReturnToQueue(state.getCurrPlayer().toStatsDTO(),t.toDTO());
        nextPhase();
    }

    public GameSnapshot toSnapshot(int matchId){
        return state.toSnapshot(matchId, currPhaseState.getPhase());
    }

    public boolean checkCorrectPhase(GamePhaseEnum gamePhaseEnum) {
        return gamePhaseEnum == currPhaseState.getPhase();
    }

    public void checkCanDraw() {

        CanDrawVisitor cd = state.buildCanDrawVisitor();
        String nick = state.getCurrPlayer().getNickname();
        if(cd.getMustDraw()){
            notifier.notifyDrawable(state.toActionsDTO(), nick);
        } else if (cd.getMayDraw()){
            state.setSkippableDraw(true);
            notifier.notifyDrawable(state.toActionsDTO(), nick);
        } else
            skipDraw();
    }

    public Map<Player, Integer> calculateRankingPoints() {
        return rankingCalculator.calculateRankingPoints(state.getPlayers(), state.getNumPlayers());
    }

    public void notifyGameEnding(Map<String, Integer> globalPositions) {
        Map<Integer, List<Player>> finalRanking = rankingCalculator.calculateFinalRanking(state.getPlayers());
        List<PlayerStatsDTO> statsList = state.getPlayers().stream()
                .map(Player::toStatsDTO)
                .toList();
        Map<String, Integer> rankingPositions = new HashMap<>();
        finalRanking.forEach((rank, players) ->
                players.forEach(p -> rankingPositions.put(p.getNickname(), rank)));
        notifier.notifyGameEnding(statsList, globalPositions, rankingPositions);
    }

    public Runnable getOnGameEndedCallback() {
        return onGameEndedCallback;
    }
    public void setOnGameStartedCallback(Runnable onGameStartedCallback) {
        this.onGameStartedCallback = onGameStartedCallback;
    }

    public List<Action> getToDoActions() {
        return state.getToDoActions();
    }

    public void setSkippableDraw(boolean value) {
        state.setSkippableDraw(value);
    }

    public int getQueueSize() {
        return state.getQueueSize();
    }

    public int getNumPlayers() {
        return state.getNumPlayers();
    }

    public int getCurrTurn() {
        return state.getCurrTurn();
    }

    public void incrementTurn() {
        state.incrementTurn();
    }

    public boolean isQueueEmpty() {
        return state.isQueueEmpty();
    }

    public boolean hasAnySkippableDraws() {
        return state.hasAnySkippableDraws();
    }

    public void checkBoardTileEffects() {
        state.checkBoardTileEffects();
    }

    public void showBoard() {
        notifier.showBoard(state.toDTO(currPhaseState));
    }
}