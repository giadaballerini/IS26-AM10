package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A snapshot of the complete game board state.
 *
 * <p>Sent by the server at game start and after a reconnection so that the
 * client can fully rebuild its local {@code VirtualModel}.
 */
public class BoardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Cards currently visible in the upper row. */
    private final List<CardDTO> upperList;

    /** Cards currently visible in the lower row. */
    private final List<CardDTO> lowerList;

    /** All players participating in the match. */
    private final List<PlayerDTO> players;

    /** Current statistics for every player. */
    private final List<PlayerStatsDTO> playerStats;

    /** Actions still available to the active player this turn. */
    private final ActionsDTO toDoActions;

    /** Tiles placed on the board. */
    private final List<TileDTO> boardTiles;

    /** Tiles of the queue. */
    private final List<TileDTO> queueTiles;

    /** Nickname of the player whose turn it currently is. */
    private final String currentPlayerNickname;

    /** Current phase of the game. */
    private final GamePhaseEnum currentPhase;

    /** Index of the current turn. */
    private final int currTurn;

    /** Total number of players in the match. */
    private final int numPlayers;

    /** Number of cards remaining in the main deck. */
    private final int deckSize;

    /**
     * Creates a full board snapshot.
     *
     * @param upperList             cards in the upper row
     * @param lowerList             cards in the lower row
     * @param players               all players in the match
     * @param boardTiles            tiles on the board
     * @param queueTiles            tiles of the queue
     * @param playerStats           current statistics for every player
     * @param currentPlayerNickname nickname of the active player
     * @param toDoActions           actions still available this turn
     * @param currentPhase          current game phase
     * @param currTurn              index of the current turn
     * @param numPlayers            total number of players
     * @param deckSize              number of cards left in the deck
     */
    public BoardDTO(List<CardDTO> upperList, List<CardDTO> lowerList, List<PlayerDTO> players,
                    List<TileDTO> boardTiles, List<TileDTO> queueTiles, List<PlayerStatsDTO> playerStats,
                    String currentPlayerNickname, ActionsDTO toDoActions, GamePhaseEnum currentPhase,
                    int currTurn, int numPlayers, int deckSize) {
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.players = players;
        this.boardTiles = boardTiles;
        this.queueTiles = queueTiles;
        this.playerStats = playerStats;
        this.currentPlayerNickname = currentPlayerNickname;
        this.toDoActions = toDoActions;
        this.currentPhase = currentPhase;
        this.currTurn = currTurn;
        this.numPlayers = numPlayers;
        this.deckSize = deckSize;
    }

    /**
     * Returns a copy of the upper card row.
     *
     * @return upper card list
     */
    public List<CardDTO> getUpperList() {
        return new ArrayList<>(upperList);
    }

    /**
     * Returns a copy of the lower card row.
     *
     * @return lower card list
     */
    public List<CardDTO> getLowerList() {
        return new ArrayList<>(lowerList);
    }

    /**
     * Returns a copy of the tiles on the board.
     *
     * @return board tiles
     */
    public List<TileDTO> getboardTiles() {
        return new ArrayList<>(boardTiles);
    }

    /**
     * Returns a copy of the tiles of the queue.
     *
     * @return queue tiles
     */
    public List<TileDTO> getqueueTiles() {
        return new ArrayList<>(queueTiles);
    }

    /**
     * Returns a copy of the player list.
     *
     * @return all players in the match
     */
    public List<PlayerDTO> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Returns the nickname of the player whose turn it currently is.
     *
     * @return active player's nickname
     */
    public String getCurrentPlayerNickname() {
        return currentPlayerNickname;
    }

    /**
     * Returns a copy of the statistics for every player.
     *
     * @return player statistics list
     */
    public List<PlayerStatsDTO> getPlayerStats() {
        return new ArrayList<>(playerStats);
    }

    /**
     * Returns the current game phase.
     *
     * @return current {@link GamePhaseEnum} value
     */
    public GamePhaseEnum getCurrentPhase() {
        return this.currentPhase;
    }

    /**
     * Returns the index of the current turn.
     *
     * @return current turn index
     */
    public int getCurrTurn() {
        return currTurn;
    }

    /**
     * Returns the total number of players in the match.
     *
     * @return number of players
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Returns the number of cards remaining in the main deck.
     *
     * @return deck size
     */
    public int getDeckSize() {
        return deckSize;
    }

    /**
     * Returns a copy of the actions still available to the active player.
     *
     * @return to-do actions for the current turn
     */
    public ActionsDTO getTodoActions() {
        return new ActionsDTO(toDoActions);
    }
}