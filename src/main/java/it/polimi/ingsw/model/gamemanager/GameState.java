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

/**
 * Holds and manages all mutable game data for a single match.
 *
 * <p>This class is the central data store accessed by {@link GameManager} and
 * the phase state machine. It owns the card lists (deck, buildings, upper and
 * lower display rows), the board and queue tiles, the player list, the current
 * player, turn/age counters, and the pending action queue. It also exposes the
 * operations that mutate this data in response to player actions and phase
 * transitions.</p>
 */
public class GameState {

    /**
     * The minimum number of players supported by a match.
     *
     * <p>This lower bound is enforced by {@link #GameState(List, int)} and
     * mirrors the range of player counts that {@link GameInitializer} is able
     * to set up (decks, boards, queues, and display rows are only defined for
     * {@value #MIN_PLAYERS}–{@value #MAX_PLAYERS} players).</p>
     */
    public static final int MIN_PLAYERS = 2;

    /**
     * The maximum number of players supported by a match.
     */
    public static final int MAX_PLAYERS = 5;

    /** The main draw pile of tribe cards. */
    private List<Card> deck;

    /** The building card supply not yet added to the display rows. */
    private List<Card> buildings;

    /** Tiles constituting the queue, where players wait before moving onto the board. */
    private List<Tile> queue;

    /** Tiles constituting the main game board. */
    private List<Tile> board;

    /** The upper card display row, from which players may draw. */
    private List<Card> upperList;

    /** The lower card display row, from which players may draw. */
    private List<Card> lowerList;

    /** All players participating in this match. */
    private final List<Player> players;

    /** The player currently taking their turn. */
    private Player currPlayer;

    /** The current age (era) of the match (1–3). */
    private int currAge;

    /** The current turn number within the age (1–10). */
    private int currTurn;

    /**
     * Whether the pending draw actions in {@link #toDoActions} may be skipped
     * by the active player.
     */
    private boolean skippableDraw;

    /** Draw actions that have been triggered but not yet resolved. */
    private final List<Action> toDoActions;

    /** The total number of players in this match. */
    private final int numPlayers;

    /**
     * Constructs a {@code GameState} with the given player list and player count,
     * initializing all collections to empty and counters to their starting values.
     * Call {@link #initialize()} to populate the game data before play begins.
     *
     * @param players    the list of players participating in the match
     * @param numPlayers the total number of players
     * @throws IllegalArgumentException if {@code numPlayers} is not between
     *         {@value #MIN_PLAYERS} and {@value #MAX_PLAYERS} (inclusive)
     */
    public GameState(List<Player> players, int numPlayers) {
        if (numPlayers < MIN_PLAYERS || numPlayers > MAX_PLAYERS)
            throw new IllegalArgumentException(
                    "Numero di giocatori non valido: " + numPlayers
                            + " (deve essere compreso tra " + MIN_PLAYERS + " e " + MAX_PLAYERS + ")");
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

    /**
     * Reconstructs a {@code GameState} from a previously saved
     * {@link GameSnapshot}, restoring every piece of mutable game data
     * (decks, display rows, board, queue, age/turn counters, pending actions,
     * and the current player) exactly as it was at the time of the snapshot.
     *
     * <p>Unlike {@link #GameState(List, int)}, {@code numPlayers} is not
     * re-validated against {@link #MIN_PLAYERS}/{@link #MAX_PLAYERS} here:
     * the snapshot is assumed to originate from a match that was already
     * validated when it was first created.</p>
     *
     * <p>Tile-to-player object references broken by JSON deserialization are
     * intentionally left unrestored here; callers are responsible for invoking
     * the appropriate reconnection logic after construction (see
     * {@link RestoredGameManager}).</p>
     *
     * @param snapshot the snapshot to restore the state from
     * @param players  the players participating in the restored match
     */
    public GameState(GameSnapshot snapshot, List<Player> players) {
        this.players = players;
        this.numPlayers = snapshot.getNumPlayers();
        this.deck = new ArrayList<>(snapshot.getDeck());
        this.buildings = new ArrayList<>(snapshot.getBuildings());
        this.upperList = new ArrayList<>(snapshot.getUpperList());
        this.lowerList = new ArrayList<>(snapshot.getLowerList());
        this.board = new ArrayList<>(snapshot.getBoard());
        this.queue = new ArrayList<>(snapshot.getQueue());
        this.currAge = snapshot.getCurrAge();
        this.currTurn = snapshot.getCurrTurn();
        this.skippableDraw = snapshot.isSkippableDraw();

        this.currPlayer = players.stream()
                .filter(p -> p.getNickname().equals(snapshot.getCurrentPlayerNickname()))
                .findFirst()
                .orElse(null);

        this.toDoActions = new ArrayList<>();
        if (snapshot.getToDoActions() != null) {
            for (GameSnapshot.PendingAction pa : snapshot.getToDoActions()) {
                players.stream()
                        .filter(p -> p.getNickname().equals(pa.getOwnerNickname()))
                        .findFirst()
                        .ifPresent(owner -> toDoActions.add(new Action(owner, pa.getType())));
            }
        }
    }

    /**
     * Returns the card with the given ID from the upper or lower display row.
     *
     * @param id the card ID to look up
     * @return the matching card, or {@code null} if not found in either row
     */
    public Card getCardById(int id) {
        return Stream.of(upperList, lowerList).flatMap(List::stream)
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the board tile at the given position.
     *
     * @param id the index of the tile in the board list
     * @return the tile at position {@code id}, or {@code null} if out of bounds
     */
    public Tile getTileById(int id) {
        if (id < 0 || id >= board.size()) {
            return null;
        }
        return board.get(id);
    }

    /**
     * Returns whether the pending draw actions may be skipped by the active player.
     *
     * @return {@code true} if the current draws are skippable
     */
    public boolean getSkippableDraw() { return this.skippableDraw; }

    /**
     * Returns a copy of the list of pending draw actions.
     *
     * @return pending actions; never {@code null}, may be empty
     */
    public List<Action> getToDoActions() {
        return new ArrayList<>(toDoActions);
    }

    /**
     * Returns the number of players currently occupying a queue tile.
     *
     * @return count of occupied queue tiles
     */
    int getQueueSize() {
        return Math.toIntExact(queue.stream().filter(Tile::isOccupied).count());
    }

    /**
     * Returns the total number of players in this match.
     *
     * @return number of players
     */
    int getNumPlayers() {
        return this.numPlayers;
    }

    /**
     * Increments the turn counter by one.
     */
    void incrementTurn() {
        currTurn++;
    }

    /**
     * Returns the current turn number within the age.
     *
     * @return current turn (1–10)
     */
    int getCurrTurn() {
        return currTurn;
    }

    /**
     * Sets whether the pending draw actions may be skipped.
     *
     * @param canSkip {@code true} to mark the current draws as skippable
     */
    void setSkippableDraw(boolean canSkip) {
        this.skippableDraw = canSkip;
    }

    /**
     * Sets the currently active player.
     *
     * @param p the player to set as current; may be {@code null}
     */
    public void setCurrPlayer(Player p) {
        currPlayer = p;
    }


    /**
     * Returns the tile queue.
     *
     * @return the queue; never {@code null}
     */
    public List<Tile> getQueue() {
        return queue;
    }

    /**
     * Returns the main draw pile.
     *
     * @return the deck; never {@code null}
     */
    public List<Card> getDeck() {
        return deck;
    }

    /**
     * Returns the building card supply.
     *
     * @return the building list; never {@code null}
     */
    public List<Card> getBuildings() {
        return buildings;
    }

    /**
     * Returns the board tiles.
     *
     * @return the board; never {@code null}
     */
    public List<Tile> getBoard() {
        return board;
    }

    /**
     * Replaces the board tiles with the given list.
     *
     * @param board the new board; must not be {@code null}
     */
    void restoreBoard(List<Tile> board) {
        this.board = board;
    }

    /**
     * Returns the upper card display row.
     *
     * @return the upper list; never {@code null}
     */
    public List<Card> getUpperList() {
        return upperList;
    }

    /**
     * Returns the lower card display row.
     *
     * @return the lower list; never {@code null}
     */
    public List<Card> getLowerList() {
        return lowerList;
    }

    /**
     * Returns the list of all players in the match.
     *
     * @return the player list; never {@code null}
     */
    public List<Player> getPlayers() {
        return players;
    }


    /**
     * Returns the player currently taking their turn.
     *
     * @return the current player; may be {@code null} before the match starts
     */
    public Player getCurrPlayer() {
        return currPlayer;
    }

    /**
     * Returns the current age.
     *
     * @return current age (1–3)
     */
    public int getCurrAge() {
        return currAge;
    }

    /**
     * Initializes all game data for a new match.
     *
     * <p>Loads and shuffles the deck, building deck, board, queue, and display
     * rows via {@link GameInitializer}. Players are shuffled and assigned to
     * queue tiles in random order. The first player in the shuffled order
     * becomes the starting current player. Initial food is distributed
     * according to turn order.</p>
     */
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

    /**
     * Assigns starting food to each player based on their position in the
     * turn order. Earlier players receive less food to compensate for the
     * first-mover advantage.
     */
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

    /**
     * Advances the match to the next age.
     *
     * <p>Increments the age counter (up to 3), removes building cards from
     * the lower row when entering age 3, moves any remaining building cards
     * from the upper row to the lower row, and adds the new age's building
     * cards to the upper row.</p>
     */
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

    /**
     * Returns whether any player has pending skippable draw actions.
     *
     * @return {@code true} if at least one player has skippable draws queued
     */
    public boolean hasAnySkippableDraws() {
        return players.stream().anyMatch(Player::hasSkippableDraws);
    }

    /**
     * Loads the skippable draw actions of the first player who has any,
     * setting them as the current player and populating the action queue.
     *
     * @return {@code true} if a player with skippable draws was found and
     *         loaded; {@code false} if no such player exists
     */
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

    /**
     * Refills the display rows at the start of a new round by removing played
     * character and event cards, shifting remaining cards, and drawing new
     * cards from the deck.
     *
     * @return {@code true} if at least one card drawn belongs to a newer age,
     *         indicating that an age change should be triggered
     */
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

    /**
     * Moves the current player from the front of the queue onto the given
     * board tile. The vacated queue tile is moved to the back of the queue.
     *
     * @param t the board tile the current player is moving to
     */
    public void applyMove(Tile t) {
        Tile removed = queue.removeFirst();
        removed.removePlayer();
        queue.add(removed);
        t.setPlayer(currPlayer);
    }

    /**
     * Computes and applies end-of-game scores for all players, including
     * builder points, crafter bonuses, painter pairs, and building PP values.
     * Any interactive end-game building effects are also triggered.
     *
     * @param phase the game phase to pass to building effect checks
     */
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

    /**
     * Calculates the end-of-game prestige point bonus for a single player,
     * summing contributions from builders, crafters, painters, and buildings.
     *
     * @param p the player to calculate the score for
     * @return the total end-of-game PP bonus for the player
     */
    private int calculateFinalPlayerScore(Player p) {
        int score = 0;
        score += p.getBuilderPoints();
        score += p.getNumType(CardTypeEnum.CRAFTER) * p.getTotSymbolsForCrafter();
        score += 10 * (p.getNumType(CardTypeEnum.PAINTER) / 2);
        score += p.getBuildings().stream().mapToInt(Building::getPpValue).sum();
        return score;
    }

    /**
     * Advances the current player to the next one in turn order.
     *
     * <p>During the setup phase, or when no player is on the board, the next
     * player is taken from the front of the queue. In all other phases, the
     * next player is the first one currently occupying a board tile.</p>
     *
     * @param currPhaseState the current game phase, used to determine the
     *                       selection strategy
     */
    public void applyNextPlayer(GamePhaseEnum currPhaseState) {
        if (board.stream().anyMatch(Tile::isOccupied) && (currPhaseState != SETUP_PHASE))
            currPlayer = board.stream()
                    .filter(Tile::isOccupied)
                    .map(Tile::getPlayer)
                    .findFirst()
                    .orElse(null);
        else
            currPlayer = queue.getFirst().getPlayer();
    }

    /**
     * Applies all event cards in the display row appropriate for the given
     * phase, collecting the results into an {@link EventDTO}.
     *
     * <p>During {@link GamePhaseEnum#END_ROUND}, event cards are read from the
     * lower row; during {@link GamePhaseEnum#PLAY_EVENT}, from the upper row.
     * After events are applied, updated player stats are added to the DTO.</p>
     *
     * @param phase the game phase determining which display row to process
     * @return a DTO summarising all event effects and updated player stats
     */
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

    /**
     * Applies instant and interactive tile effects for the current player
     * across the given collection of tiles, adding any resulting actions to
     * the pending queue.
     *
     * @param tiles the tiles to evaluate
     */
    private void applyTileEffects(Collection<Tile> tiles) {
        for (Tile t : tiles) {
            if (t.isOccupied() && t.getPlayer().equals(currPlayer)) {
                t.execInstantEffect();
                toDoActions.addAll(t.execInteractiveEffect());
            }
        }
    }

    /**
     * Evaluates board tile effects for the current player.
     */
    void checkBoardTileEffects() {
        applyTileEffects(board);
    }

    /**
     * Evaluates queue tile effects for the current player, including the
     * queue food bonus, instant effects, and interactive effects.
     */
    public void applyQueueTileEffects() {
        for (Tile t : queue) {
            if (t.isOccupied() && t.getPlayer().equals(currPlayer)) {
                t.applyQueueBonus(currPlayer);
                t.execInstantEffect();
                toDoActions.addAll(t.execInteractiveEffect());
            }
        }
    }

    /**
     * Resolves a draw action by the current player for the given card.
     *
     * <p>The draw type (upper or lower) is inferred from which display row the
     * card belongs to. A matching pending action must exist, and the card must
     * pass validation by {@link DrawCardVisitor}; otherwise an
     * {@link InvalidDrawException} is thrown. On success, the action is
     * removed from the queue, the card is removed from its row, and its
     * instant and interactive effects are applied.</p>
     *
     * @param card  the card the player wants to draw
     * @param phase the current game phase, passed to instant effect resolution
     * @throws InvalidDrawException if no matching pending draw action exists or
     *                              the card fails validation
     */
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

    /**
     * Removes the given action from the pending action queue, marking it as
     * resolved.
     *
     * @param action the action to resolve
     */
    private void resolveAction(Action action) {
        toDoActions.remove(action);
    }

    /**
     * Clears all pending draw actions, effectively skipping them.
     */
    public void applySkip() {
        toDoActions.clear();
    }

    /**
     * Returns whether the queue is empty, meaning that no player is occupying the
     * first queue tile.
     *
     * @return {@code true} if the first queue tile is unoccupied
     */
    boolean isQueueEmpty() {
        return !queue.getFirst().isOccupied();
    }

    /**
     * Moves the current player off the board and onto the first free queue
     * tile, then evaluates queue tile effects.
     *
     * @return the queue tile the player was placed on
     * @throws IllegalStateException if no free queue tile exists or the current
     *                               player is not found on the board
     */
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

    /**
     * Removes the current player's pawn from the board tile they occupy.
     *
     * @throws IllegalStateException if the current player is not found on any
     *                               board tile
     */
    private void removeFromBoard() {
        board.stream().filter(tile -> tile.isOccupied() && tile.getPlayer().equals(currPlayer))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(currPlayer.getNickname() + " non presente sulla board."))
                .removePlayer();
    }

    /**
     * Returns whether the given nickname matches the current player's nickname.
     *
     * @param p the nickname to check
     * @return {@code true} if {@code p} is the current player's nickname
     */
    public boolean checkCorrectPlayer(String p) {
        return currPlayer.getNickname().equals(p);
    }

    /**
     * Builds a {@link CanDrawVisitor} preloaded with the draw actions available
     * to the current player, checking upper and lower rows in order and stopping
     * early if a mandatory draw is found.
     *
     * @return a {@link CanDrawVisitor} reflecting which cards the current player
     *         may draw and whether any draw is mandatory
     */
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

    /**
     * Serializes the current game state into a {@link GameSnapshot} for
     * persistence.
     *
     * <p>All mutable collections are copied into new lists to avoid aliasing.
     * Pending actions are converted to {@link it.polimi.ingsw.persistency.GameSnapshot.PendingAction}
     * records; actions without an owner are recorded with the placeholder
     * nickname {@code "SYSTEM"}.</p>
     *
     * @param matchId the unique identifier of the match being snapshotted
     * @param phase   the current game phase at the time of the snapshot
     * @return a {@link GameSnapshot} representing the full current state
     */
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

    /**
     * Builds a {@link ChangeAgeDTO} reflecting the current display rows and
     * age counter, used to notify clients of an age transition.
     *
     * @return a DTO describing the new age state
     */
    public ChangeAgeDTO genChangeAgeDTO() {
        return new ChangeAgeDTO(upperList.stream().map(Card::toDTO).toList(),
                lowerList.stream().map(Card::toDTO).toList(), currAge, deck.size());
    }

    /**
     * Builds a {@link BoardDTO} representing the full current board state,
     * suitable for broadcasting to all clients.
     *
     * @param currPhaseState the current phase state, used to include the phase
     *                       enum in the DTO
     * @return a DTO of the current board state
     */
    public BoardDTO toDTO(GamePhaseState currPhaseState) {
        return new BoardDTO(
                upperList.stream().map(Card::toDTO).toList(),
                lowerList.stream().map(Card::toDTO).toList(),
                players.stream().map(Player::toDTO).toList(),
                board.stream().map(Tile::toDTO).toList(),
                queue.stream().map(Tile::toDTO).toList(),
                players.stream().map(Player::toStatsDTO).toList(),
                players.stream().map(Player::toStatusDTO).toList(),
                currPlayer.getNickname(),
                toActionsDTO(),
                currPhaseState.getPhase(),
                currTurn,
                numPlayers,
                deck.size()
        );
    }

    /**
     * Builds an {@link ActionsDTO} summarising the pending draw actions
     * available to the current player.
     *
     * @return a DTO with the count of upper and lower draws available and
     *         whether they are skippable
     */
    public ActionsDTO toActionsDTO() {
        int up = 0;
        int down = 0;
        for (Action a : toDoActions) {
            if (a.getType() == DrawCardEnum.UP_DRAW) up++;
            else if (a.getType() == DrawCardEnum.DOWN_DRAW) down++;
        }
        return new ActionsDTO(up, down, skippableDraw);
    }
}