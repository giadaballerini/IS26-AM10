package it.polimi.ingsw.model.gamemanager;

import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.interfaces.Snapshotable;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.EventDTO;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.dto.PlayerStatusDTO;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.persistency.GameSnapshot;
import it.polimi.ingsw.visitors.CanDrawVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static it.polimi.ingsw.enumerations.GamePhaseEnum.*;


/**
 * Core model class for a single game session.
 *
 * <p>{@code GameManager} coordinates all game activity by implementing the
 * State pattern: it holds a reference to the active {@link GamePhaseState}
 * and delegates phase-specific logic to it. It also exposes the three
 * player-driven entry points ({@code onMoveRequested}, {@code onDrawCardRequested},
 * {@code onSkipRequested}) through the {@link ApplicableActions} interface,
 * which the {@code Controller} invokes after receiving and dispatching
 * incoming network messages.</p>
 *
 * <p>The raw game state (board, deck, players, current turn) is encapsulated
 * in {@link GameState}; {@code GameManager} accesses it to apply transitions
 * and delegate calculations, keeping phase logic separate from data
 * structures.</p>
 *
 * <p>Observers registered via {@link GameNotifier} are notified of every
 * relevant state change so that connected clients can keep their views
 * up to date.</p>
 *
 * <p>Implements {@link Snapshotable} to support game persistence.</p>
 */
public class GameManager implements ApplicableActions, Snapshotable {
    /**
     * The logger used for logging events in the game.
     */
    protected static final Logger LOG = Logger.getLogger(GameManager.class.getName());
    /**
     * The current game phase.
     */
    private GamePhaseState currPhaseState;
    /**
     * Callback invoked when the game reaches {@link EndGamePhaseState}.
     */
    private final Runnable onGameEndedCallback;
    /**
     * Callback invoked when {@link #initGame()} completes.
     */
    private Runnable onGameStartedCallback;
    /**
     * The game state, encapsulating the board, deck, players, and current turn.
     */
    private GameState state;
    /**
     * Handles game-wide notifications and updates.
     */
    private final GameNotifier notifier;
    /**
     * Handles ranking calculations and updates.
     */
    private final RankingCalculator rankingCalculator;


    /**
     * Constructs a new {@code GameManager} and initializes all collaborators.
     *
     * @param listeners          list of {@link ModelObserver} instances that will receive
     *                           game-event notifications
     * @param players            the list of participating players
     * @param numPlayers         the expected number of players in the match
     * @param onGameEndedCallback callback invoked when the game reaches
     *                           {@link EndGamePhaseState}; may be {@code null}
     */
    public GameManager(List<ModelObserver> listeners, List<Player> players, int numPlayers, Runnable onGameEndedCallback) {
        state = new GameState(players, numPlayers);
        notifier = new GameNotifier(listeners);
        rankingCalculator = new RankingCalculator();
        currPhaseState = new SetupPhaseState();
        this.onGameEndedCallback = onGameEndedCallback;
    }

    /**
     * Initializes the game state, broadcasts the initial board to all observers,
     * and fires the game-started callback if one has been registered.
     */
    public void initGame() {
        state.initialize();
        notifier.showBoard(state.toDTO(currPhaseState));
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());

        if (onGameStartedCallback != null)
            onGameStartedCallback.run();

        LOG.info("Game avviato correttamente!");
    }

    /**
     * Advances the game to the next phase as determined by the current
     * {@link GamePhaseState}. If the phase actually changes, observers are
     * notified and the new phase's {@code onEntry} hook is invoked.
     */
    public void nextPhase(){
        GamePhaseState oldPhase;
        oldPhase = currPhaseState;

        this.currPhaseState = currPhaseState.nextPhase(this);
        if(!oldPhase.equals(currPhaseState)){
            notifier.notifyPhaseUpdate(currPhaseState);
            currPhaseState.onEntry(this);
        }
    }


    /**
     * Increments the current age, updates card lists accordingly, and
     * notifies all observers of the age change.
     */
    public void changeAge() {

        state.advanceAge();
        notifier.notifyChangeAge(state.genChangeAgeDTO());
    }


    /**
     * Loads skippable draw actions for the first player that has pending ones.
     * If such a player is found, sets that player as current and notifies
     * observers about the available draw options.
     */
    public void loadSkippableDraws(){
        if(!state.loadSkippableDraws())
            return;
        String currPlayer = state.getCurrPlayer().getNickname();
        notifier.notifyCurrPlayerUpdate(currPlayer);
        notifier.notifyDrawable(state.toActionsDTO(), currPlayer);
    }

    /**
     * Refills the board with new cards from the deck. If cards belonging to a
     * newer age are drawn during the refill, {@link #changeAge()} is called.
     */
    public void refillBoard() {
        if(state.refillBoard())
            changeAge();
    }

    /**
     * Checks whether the current player's building cards trigger any effects for
     * the current phase, and if so, queues them as skippable draw actions.
     */
    void checkEffects(){
        Player currPlayer = state.getCurrPlayer();
        List<Action> effects = currPlayer.checkBuildsEffects(currPhaseState.getPhase());
        if(!effects.isEmpty()) {
            currPlayer.addSkippableDraws(effects);
        }
    }

    /**
     * Applies end-game score bonuses (builder points, crafter multipliers,
     * painter pairs, building values) to all players.
     */
    public void finalScoreCount(){
        state.applyFinalScores(currPhaseState.getPhase());
    }

    /**
     * Advances the current player reference and notifies observers of the change.
     */
    void nextPlayer(){
        state.applyNextPlayer(currPhaseState.getPhase());
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());
    }

    /**
     * Resolves and broadcasts the event cards for the current phase,
     * then advances to the next phase.
     */
    void playEvent() {
        EventDTO events = state.applyEvents(currPhaseState.getPhase());
        notifier.notifyEventUpdate(events);
        nextPhase();
    }


    /**
     * Handles a player's request to move their token to a board tile.
     *
     * @param nick    the nickname of the requesting player
     * @param tilePos the index of the target tile on the board
     * @throws InvalidPhaseException   if the game is not in {@code SETUP_PHASE}
     * @throws InvalidPlayerException  if {@code nick} is not the current player
     * @throws InvalidMoveException    if {@code tilePos} does not correspond to a valid tile
     * @throws OccupiedTileException   if the target tile is already occupied
     */
    public void onMoveRequested(String nick, int tilePos) throws OccupiedTileException, InvalidPhaseException, InvalidPlayerException, InvalidMoveException{

        if (!checkCorrectPhase(SETUP_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        if (!(state.checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("NON È IL TUO TURNO");
        }

        Tile t = state.getTileById(tilePos);
        if(t == null)
            throw new InvalidMoveException("SELEZIONE NON VALIDA");

        if(t.isOccupied())
            throw new OccupiedTileException("LA POSIZIONE A CUI SI STA PROVANDO AD ACCEDERE È OCCUPATA");
        move(t);
    }

    /**
     * Applies the move to the game state, notifies observers, and advances the phase.
     *
     * @param t the tile the current player is moving to
     */
    void move(Tile t){
        state.applyMove(t);
        notifier.notifyMoveUpdate(t.toDTO(), state.getCurrPlayer().getNickname());
        nextPhase();
    }

    /**
     * Handles a player's request to draw a card.
     *
     * @param nick   the nickname of the requesting player
     * @param cardID the unique identifier of the card to draw
     * @throws InvalidPhaseException  if the game is not in {@code DRAW_PHASE}
     *                                or {@code OPTIONAL_DRAW_PHASE}
     * @throws InvalidPlayerException if {@code nick} is not the current player
     * @throws InvalidDrawException   if the card does not exist or cannot be drawn
     */
    public void onDrawCardRequested(String nick,int cardID) throws InvalidPhaseException, InvalidPlayerException, InvalidDrawException{
        if (!(state.checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("NON È IL TUO TURNO");
        }
        if (!checkCorrectPhase(DRAW_PHASE) && !checkCorrectPhase(OPTIONAL_DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        Card card = state.getCardById(cardID);

        if(card==null)
            throw new InvalidDrawException("CARTA NON ESISTENTE");

        drawCard(card);

    }

    /**
     * Applies the draw to the game state, triggers card effects, updates observers,
     * and either initiates the next draw check or advances the phase.
     *
     * @param card the card to draw
     * @throws InvalidDrawException   if the draw is not valid for the current state
     * @throws InvalidPlayerException if the current player cannot perform the draw
     * @throws InvalidPhaseException  if the phase does not allow drawing
     */
    void drawCard(Card card) throws InvalidDrawException,InvalidPlayerException,InvalidPhaseException{
        Player currPlayer = state.getCurrPlayer();
        PlayerStatusDTO before = currPlayer.toStatusDTO();
        state.applyDraw(card, currPhaseState.getPhase());
        checkEffects();
        notifier.notifyDrawUpdate(currPlayer,card);
        notifier.notifyStatsUpdate(currPlayer);
        if(!before.equals(currPlayer.toStatusDTO()))
            notifier.notifyStatusUpdate(currPlayer);
        if(!state.getToDoActions().isEmpty())
            checkCanDraw();
        else
            nextPhase();
    }

    /**
     * Handles a player's request to skip their current draw action.
     *
     * @param nick the nickname of the requesting player
     * @throws InvalidPhaseException  if the game is not in a draw phase
     * @throws InvalidPlayerException if {@code nick} is not the current player
     * @throws InvalidSkipException   if the current draw action cannot be skipped
     */
    public void onSkipRequested(String nick) throws InvalidPhaseException, InvalidPlayerException, InvalidSkipException{
        if (!(state.checkCorrectPlayer(nick))) {
            throw new InvalidPlayerException("NON È IL TUO TURNO");
        }
        if (!checkCorrectPhase(DRAW_PHASE) && !checkCorrectPhase(OPTIONAL_DRAW_PHASE))
            throw new InvalidPhaseException("FASE INVALIDA");
        if (!state.getSkippableDraw())
            throw new InvalidSkipException("NON È POSSIBILE SALTARE IL TURNO ADESSO");
        skipDraw();
    }

    /**
     * Clears all pending draw actions, notifies observers of the skip, and
     * advances to the next phase.
     */
    void skipDraw(){
        state.applySkip();
        notifier.notifyDrawable(state.toActionsDTO(), state.getCurrPlayer().getNickname());
        notifier.notifySkip(state.getCurrPlayer());
        nextPhase();
    }

    /**
     * Executes the end-of-turn sequence: moves the current player back to the
     * queue, checks for triggered effects, and notifies observers.
     */
    void execEndTurn(){
        Tile t = state.applyEndTurn();
        checkEffects();
        notifier.notifyReturnToQueue(state.getCurrPlayer().toStatsDTO(),t.toDTO());
        nextPhase();
    }

    /**
     * Creates a {@link GameSnapshot} capturing the full current game state
     * so it can be persisted and later restored.
     *
     * @param matchId the unique identifier of the match
     * @return a {@link GameSnapshot} representing the current state
     */
    public GameSnapshot toSnapshot(int matchId){
        return state.toSnapshot(matchId, currPhaseState.getPhase());
    }

    /**
     * Checks whether the game is currently in the specified phase.
     *
     * @param gamePhaseEnum the phase to check against
     * @return {@code true} if the current phase matches {@code gamePhaseEnum}
     */
    public boolean checkCorrectPhase(GamePhaseEnum gamePhaseEnum) {
        return currPhaseState.getPhase() == gamePhaseEnum;
    }

    /**
     * Evaluates the current player's draw eligibility using a
     * {@link CanDrawVisitor} and either notifies the player of drawable cards,
     * marks the draw as skippable, or automatically skips if no draw is possible.
     */
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

    /**
     * Computes the ranking points earned by each player in this match.
     *
     * @return a map from each {@link Player} to the ranking points they earned
     */
    public Map<Player, Integer> calculateRankingPoints() {
        return rankingCalculator.calculateRankingPoints(state.getPlayers(), state.getNumPlayers());
    }

    /**
     * Notifies all observers that the game has ended, including per-player stats,
     * local ranking positions, and global ranking positions.
     *
     * @param globalPositions a map from player nickname to their global ranking position
     */
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


    /**
     * Returns the callback that is invoked when the game reaches
     * {@link EndGamePhaseState}.
     *
     * @return the game-ended {@link Runnable} callback
     */
    public Runnable getOnGameEndedCallback() {
        return onGameEndedCallback;
    }

    /**
     * Registers a callback to be invoked when {@link #initGame()} completes.
     *
     * @param onGameStartedCallback the callback to invoke on game start
     */
    public void setOnGameStartedCallback(Runnable onGameStartedCallback) {
        this.onGameStartedCallback = onGameStartedCallback;
    }

    /**
     * Returns the list of draw actions still pending for the current player.
     *
     * @return a copy of the pending {@link Action} list
     */
    public List<Action> getToDoActions() {
        return state.getToDoActions();
    }

    /**
     * Sets whether the current draw action may be skipped.
     *
     * @param value {@code true} to allow skipping, {@code false} to disallow it
     */
    public void setSkippableDraw(boolean value) {
        state.setSkippableDraw(value);
    }

    /**
     * The number of occupied tile slots in the player queue.
     * @return the number of occupied slots in the player queue */
    public int getQueueSize() {
        return state.getQueueSize();
    }

    /**
     * The number of players in the match.
     * @return the total number of players in the match */
    public int getNumPlayers() {
        return state.getNumPlayers();
    }

    /**
     * The current number turn number of the match
     * @return the current turn number (1-based) */
    public int getCurrTurn() {
        return state.getCurrTurn();
    }

    /**Increments the turn counter by one. */
    public void incrementTurn() {
        state.incrementTurn();
    }

    /**
     * Returns whether the queue's first slot is unoccupied, which indicates
     * that all players have moved onto the board.
     *
     * @return {@code true} if the queue is empty (no player in the first slot)
     */
    public boolean isQueueEmpty() {
        return state.isQueueEmpty();
    }

    /**
     * Returns whether any player currently has pending skippable draw actions.
     *
     * @return {@code true} if at least one player has skippable draws queued
     */
    public boolean hasAnySkippableDraws() {
        return state.hasAnySkippableDraws();
    }

    /**
     * Triggers instant and interactive effects for board tiles occupied by
     * the current player.
     */
    public void checkBoardTileEffects() {
        state.checkBoardTileEffects();
    }

    /**
     * Broadcasts the complete current board state to all registered observers.
     */
    public void showBoard() {
        notifier.showBoard(state.toDTO(currPhaseState));
    }

    /**
     * Sets the game state to the specified value.
     * @param state the new {@link GameState} value
     */
    protected void setState(GameState state) {
        this.state = state;
    }

    /**
     * Sets the current game phase state to the specified value.
     * @param currPhaseState the new {@link GamePhaseState} value
     */
    protected void setCurrPhaseState(GamePhaseState currPhaseState) {
        this.currPhaseState = currPhaseState;
    }

    /**
     * Returns the current game phase enum value.
     *
     * @return the phase of the active {@link GamePhaseState}
     */
    protected GamePhaseEnum getCurrPhase() {
        return currPhaseState.getPhase();
    }

    /**
     * Broadcasts the current phase to all registered observers.
     */
    protected void notifyPhaseUpdate() {
        notifier.notifyPhaseUpdate(currPhaseState);
    }

    /**
     * Broadcasts the current player's nickname to all registered observers.
     */
    protected void notifyCurrPlayerUpdate() {
        notifier.notifyCurrPlayerUpdate(state.getCurrPlayer().getNickname());
    }

    /**
     * Returns the list of tiles on the game board.
     *
     * @return the board tile list
     */
    protected List<Tile> getBoard() {
        return state.getBoard();
    }

    /**
     * Returns the list of tiles in the queue.
     *
     * @return the queue tile list
     */
    protected List<Tile> getQueue() {
        return state.getQueue();
    }
}