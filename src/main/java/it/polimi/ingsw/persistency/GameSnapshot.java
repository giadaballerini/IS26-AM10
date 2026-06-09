package it.polimi.ingsw.persistency;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * Immutable snapshot of the full game state at a given point in time.
 *
 * <p>Used for persistence and game restoration: the snapshot captures every
 * piece of information needed to reconstruct a running match exactly as it
 * was when the snapshot was taken. Instances are created by Jackson during
 * deserialization via the {@link com.fasterxml.jackson.annotation.JsonCreator}
 * constructor, and by the server when saving the current game state.</p>
 */
public class GameSnapshot {

    /** Unique identifier of the match this snapshot belongs to. */
    private final int matchId;

    /** Total number of players participating in the match. */
    private final int numPlayers;

    /** The draw pile of cards not yet dealt or placed on the board. */
    private final List<Card> deck;

    /** Cards currently in the buildings area. */
    private final List<Card> buildings;

    /** Cards occupying the upper display row. */
    private final List<Card> upperList;

    /** Cards occupying the lower display row. */
    private final List<Card> lowerList;

    /** Tiles currently placed on the main game board. */
    private final List<Tile> board;

    /** Tiles waiting in the queue to be placed on the board. */
    private final List<Tile> queue;

    /** All players and their individual state. */
    private final List<Player> players;

    /**
     * Actions that have been triggered but not yet resolved at the time the
     * snapshot was taken (e.g. pending card draws granted by an effect).
     */
    private final List<PendingAction> toDoActions;

    /** Nickname of the player whose turn it currently is. */
    private final String currentPlayerNickname;

    /** The phase of the game at the time of the snapshot. */
    private final GamePhaseEnum currentPhase;

    /** The age (era) of the game at the time of the snapshot (1-based). */
    private final int currAge;

    /** The turn number within the current age at the time of the snapshot. */
    private final int currTurn;

    /**
     * Whether the next draw action can be skipped by the active player.
     * Typically {@code true} when the draw is optional rather than mandatory.
     */
    private final boolean skippableDraw;

    /**
     * Constructs a fully initialised {@code GameSnapshot} from its constituent parts.
     *
     * <p>This constructor is used by Jackson for JSON deserialization; all
     * parameters map directly to their corresponding JSON properties.</p>
     *
     * @param matchId                unique identifier of the match
     * @param numPlayers             number of players in the match
     * @param deck                   remaining draw pile
     * @param buildings              cards in the buildings area
     * @param upperList              cards in the upper display row
     * @param lowerList              cards in the lower display row
     * @param board                  tiles on the game board
     * @param queue                  tiles that constitute the queue
     * @param players                list of all players with their state
     * @param currentPlayerNickname  nickname of the currently active player
     * @param currentPhase           current phase of the game
     * @param currAge                current age (era) of the game
     * @param currTurn               current turn within the age (era)
     * @param skippableDraw          {@code true} if the pending draw may be skipped
     * @param toDoActions            pending actions not yet resolved
     */
    @JsonCreator
    public GameSnapshot(
            @JsonProperty("matchId") int matchId,
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("deck") List<Card> deck,
            @JsonProperty("buildings") List<Card> buildings,
            @JsonProperty("upperList") List<Card> upperList,
            @JsonProperty("lowerList") List<Card> lowerList,
            @JsonProperty("board") List<Tile> board,
            @JsonProperty("queue") List<Tile> queue,
            @JsonProperty("players") List<Player> players,
            @JsonProperty("currentPlayerNickname") String currentPlayerNickname,
            @JsonProperty("currentPhase") GamePhaseEnum currentPhase,
            @JsonProperty("currAge") int currAge,
            @JsonProperty("currTurn") int currTurn,
            @JsonProperty("skippableDraw") boolean skippableDraw,
            @JsonProperty("toDoActions") List<PendingAction> toDoActions) {

        this.matchId = matchId;
        this.numPlayers = numPlayers;
        this.deck  = deck;
        this.buildings = buildings;
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.board = board;
        this.queue = queue;
        this.players = players;
        this.currentPlayerNickname = currentPlayerNickname;
        this.currentPhase = currentPhase;
        this.currAge = currAge;
        this.currTurn = currTurn;
        this.skippableDraw = skippableDraw;
        this.toDoActions = toDoActions;
    }

    /**
     * Returns the unique identifier of the match this snapshot belongs to.
     *
     * @return the match ID
     */
    public int getMatchId(){ return matchId; }

    /**
     * Returns the total number of players in the match.
     *
     * @return number of players
     */
    public int getNumPlayers(){ return numPlayers; }

    /**
     * Returns the draw pile at the time of the snapshot.
     *
     * @return the deck; never {@code null}
     */
    public List<Card> getDeck(){ return deck; }

    /**
     * Returns the cards in the buildings area at the time of the snapshot.
     *
     * @return the buildings list; never {@code null}
     */
    public List<Card> getBuildings(){ return buildings; }

    /**
     * Returns the cards occupying the upper display row.
     *
     * @return the upper list; never {@code null}
     */
    public List<Card> getUpperList(){ return upperList; }

    /**
     * Returns the cards occupying the lower display row.
     *
     * @return the lower list; never {@code null}
     */
    public List<Card> getLowerList(){ return lowerList; }

    /**
     * Returns the tiles placed on the main game board.
     *
     * @return the board tiles; never {@code null}
     */
    public List<Tile> getBoard(){ return board; }

    /**
     * Returns the tiles that constitute the queue.
     *
     * @return the tile queue; never {@code null}
     */
    public List<Tile> getQueue(){ return queue; }

    /**
     * Returns all players and their individual state as captured in the snapshot.
     *
     * @return the player list; never {@code null}
     */
    public List<Player> getPlayers(){ return players; }

    /**
     * Returns the nickname of the player who was active when the snapshot was taken.
     *
     * @return current player's nickname; never {@code null}
     */
    public String getCurrentPlayerNickname(){ return currentPlayerNickname; }

    /**
     * Returns the game phase at the time of the snapshot.
     *
     * @return current {@link GamePhaseEnum} value; never {@code null}
     */
    public GamePhaseEnum getCurrentPhase(){ return currentPhase; }

    /**
     * Returns the age (era) number at the time of the snapshot.
     *
     * @return current age, 1-based
     */
    public int getCurrAge(){ return currAge; }

    /**
     * Returns the turn number within the current age at the time of the snapshot.
     *
     * @return current turn, 1-based
     */
    public int getCurrTurn(){ return currTurn; }

    /**
     * Returns whether the next draw action may be skipped by the active player.
     *
     * @return {@code true} if the draw is optional; {@code false} if mandatory
     */
    public boolean isSkippableDraw(){ return skippableDraw; }

    /**
     * Returns the list of actions that were triggered but not yet resolved
     * at the time the snapshot was taken.
     *
     * @return pending actions; never {@code null}, may be empty
     */
    public List<PendingAction> getToDoActions(){ return toDoActions; }


    /**
     * Represents a game action that has been triggered but not yet completed.
     *
     * <p>A {@code PendingAction} records which player must resolve the action
     * and what kind of card draw (or other deferred effect) is involved.
     * Instances are persisted as part of {@link GameSnapshot} so that
     * in-flight actions survive a server restart.</p>
     */
    public static class PendingAction {

        /** Nickname of the player who must resolve this action. */
        private final String ownerNickname;

        /** The type of card draw (or deferred effect) to be resolved. */
        private final DrawCardEnum type;

        /**
         * Constructs a {@code PendingAction}.
         *
         * @param ownerNickname nickname of the player responsible for resolving
         *                      this action; must not be {@code null}
         * @param type          the kind of draw or effect pending;
         *                      must not be {@code null}
         */
        @JsonCreator
        public PendingAction(
                @JsonProperty("ownerNickname") String ownerNickname,
                @JsonProperty("type") DrawCardEnum type) {
            this.ownerNickname = ownerNickname;
            this.type= type;
        }

        /**
         * Returns the nickname of the player who must resolve this action.
         *
         * @return owner's nickname; never {@code null}
         */
        public String getOwnerNickname() { return ownerNickname; }

        /**
         * Returns the type of draw or deferred effect to be resolved.
         *
         * @return the {@link DrawCardEnum} value; never {@code null}
         */
        public DrawCardEnum getType() { return type; }
    }
}