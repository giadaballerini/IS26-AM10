package it.polimi.ingsw.client;

import it.polimi.ingsw.client.data.CardRegistry;
import it.polimi.ingsw.enumerations.CardTypeEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.exceptions.InvalidCardException;
import it.polimi.ingsw.network.dto.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side model that mirrors the authoritative game state held by the server.
 * Updated incrementally by the {@link it.polimi.ingsw.client.Client} in response
 * to server notifications, and read by the UI to render the current board state.
 * All mutating methods are {@code synchronized} to support concurrent access
 * from the network thread and the UI thread.
 */
public class VirtualModel {

    /** The queue tiles, each holding the pawn of a player waiting to move to the board. */
    private List<TileDTO> queue;

    /** The board tiles, each potentially occupied by a player's pawn. */
    private List<TileDTO> board;

    /** The cards currently visible in the upper draw row. */
    private List<CardDTO> upperList;

    /** The cards currently visible in the lower draw row. */
    private List<CardDTO> lowerList;

    /** The number of players in the current match. */
    private int numPlayers;

    /** The current game phase. */
    private GamePhaseEnum currPhaseState;

    /** The current game age (1–3). */
    public int currAge;

    /** The list of all players in the match, including their cards. */
    private List<PlayerDTO> players;

    /** The nickname of the player whose turn it currently is. */
    private String currPlayer;

    /** The draw actions still available to the current player in this turn. */
    private ActionsDTO toDoActions;

    /** The current turn number. */
    private int currTurn;

    /** The local player's nickname. */
    private String nickname;

    /** The current status flags of every player in the match. */
    private List<PlayerStatusDTO> playerStatuses;

    /** The current stats (food, Prestige Points, stars, discounts) of every player. */
    private List<PlayerStatsDTO> playerStats;

    /** The number of cards remaining in the deck. */
    private int deckSize;

    /**
     * Creates a new virtual model without a preset nickname.
     * The nickname must be set later via {@link #setNickname(String)}.
     */
    public VirtualModel() {
        this.queue = new ArrayList<>();
        this.board = new ArrayList<>();
        this.upperList = new ArrayList<>();
        this.lowerList = new ArrayList<>();
        this.numPlayers = 0;
        this.currPhaseState = null;
        this.currAge = 1;
        this.players = new ArrayList<>();
        this.currPlayer = "";
        this.currTurn = 0;
        this.playerStatuses = new ArrayList<>();
        this.playerStats = new ArrayList<>();
        this.toDoActions = new ActionsDTO(0, 0, false);
    }

    /**
     * Returns a defensive copy of the queue tiles.
     *  @return a defensive copy of the queue tiles */
    public List<TileDTO> getQueue() {
        return new ArrayList<>(queue);
    }

    /**
     * Returns a defensive copy of the board tiles.
     * @return defensive copies of the board tiles */
    public List<TileDTO> getBoard() {
        return new ArrayList<>(board);
    }

    /**
     * Returns a defensive copy of the cards in the upper row.
     * @return a defensive copy of the cards in the upper row */
    public List<CardDTO> getUpperList() {
        return new ArrayList<>(upperList);
    }

    /**
     * Returns a defensive copy of the cards in the lower row.
     * @return a defensive copy of the cards in the lower row */
    public List<CardDTO> getLowerList() {
        return new ArrayList<>(lowerList);
    }

    /**
     * Returns the number of players in the current match.
     * @return the number of players in the current match */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Returns the current game phase.
     * @return the current game phase */
    public GamePhaseEnum getCurrentPhase() {
        return currPhaseState;
    }

    /**
     * Returns the current game phase as a string.
     * @return the current game age (1–3) */
    public int getCurrAge() {
        return currAge;
    }

    /**
     * Returns a defensive copy of the list of all players in the match.
     * @return a defensive copy of the list of all players in the match */
    public List<PlayerDTO> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Returns the nickname of the player whose turn it currently is.
     * @return the nickname of the player whose turn it currently is */
    public String getCurrPlayer() {
        return currPlayer;
    }

    /**
     * Returns a copy of the draw actions still available to the current player.
     * @return a copy of the draw actions still available to the current player */
    public ActionsDTO getToDoActions() {
        return new ActionsDTO(toDoActions);
    }

    /**
     * Returns the current turn of the match.
     * @return the current turn number */
    public int getCurrTurn() {
        return currTurn;
    }

    /**
     * Returns a defensive copy of the current stats of every player.
     * @return a defensive copy of the current stats of every player */
    public List<PlayerStatsDTO> getPlayerStats() {
        return new ArrayList<>(playerStats);
    }

    /**
     * Returns a defensive copy of the current status flags of every player.
     * @return a defensive copy of the current status flags of every player */
    public List<PlayerStatusDTO> getPlayerStatuses() {
        return new ArrayList<>(playerStatuses);
    }

    /**
     * Replaces the entire model state with the data from the given {@link BoardDTO},
     * typically called upon connection or reconnection to restore the full board view.
     * Initializes player statuses if they have not been set yet.
     *
     * @param b the full board state received from the server
     */
    public synchronized void update(BoardDTO b) {
        this.numPlayers = b.getNumPlayers();
        this.queue = new ArrayList<>(b.getQueueTiles());
        this.board = new ArrayList<>(b.getBoardTiles());
        this.upperList = new ArrayList<>(b.getUpperList());
        this.lowerList = new ArrayList<>(b.getLowerList());
        this.deckSize = b.getDeckSize();
        this.players = new ArrayList<>(b.getPlayers());
        this.playerStats = new ArrayList<>(b.getPlayerStats());
        this.playerStatuses = new ArrayList<>(b.getPlayerStatuses());
        this.currPhaseState = b.getCurrentPhase();
        this.currPlayer = b.getCurrentPlayerNickname();
        this.toDoActions = b.getTodoActions();
        this.currTurn = b.getCurrTurn();
        if (this.playerStatuses.isEmpty()) {
            for (PlayerDTO p : this.players) {
                this.playerStatuses.add(new PlayerStatusDTO(p.getNickname()));
            }
        }
    }

    /**
     * Updates the board and queue to reflect that the current player has placed their
     * pawn on the given tile. Marks the target board tile as occupied and frees the
     * current player's queue slot.
     *
     * @param tile the board tile that was just occupied
     */
    public synchronized void onMoveUpdate(TileDTO tile) {
        for (int i = 0; i < board.size(); i++) {
            if (board.get(i).getId() == tile.getId()) {
                board.set(i, new TileDTO(tile, currPlayer, true));
                break;
            }
        }
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getPlayer().equals(currPlayer)) {
                queue.set(i, new TileDTO(queue.get(i), "", false));
                break;
            }
        }
    }

    /**
     * Updates the current active player.
     *
     * @param player the nickname of the player whose turn it now is
     */
    public synchronized void onCurrPlayerUpdate(String player) {
        currPlayer = player;
    }

    /**
     * Updates the current game phase.
     *
     * @param phase the new game phase
     */
    public synchronized void onPhaseUpdate(PhaseDTO phase) {
        currPhaseState = phase.getPhase();
    }

    /**
     * Removes the drawn card from the appropriate draw list and adds it to the
     * drawing player's hand. Building cards are added to the player's building list;
     * all other cards are added to the character list.
     *
     * <p>Note: the if-else branching on card type could be replaced with a Strategy pattern.</p>
     *
     * @param c        the card that was drawn
     * @param nickname the nickname of the player who drew the card
     */
    public synchronized void onDrawUpdate(CardDTO c, String nickname) {
        boolean found = false;
        for (int i = 0; i < upperList.size() && !found; i++) {
            if (upperList.get(i).getId() == c.getId()) {
                upperList.remove(i);
                found = true;
            }
        }
        for (int i = 0; i < lowerList.size() && !found; i++) {
            if (lowerList.get(i).getId() == c.getId()) {
                lowerList.remove(i);
                found = true;
            }
        }
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getNickname().equals(nickname)) {
                if (CardRegistry.getType(c.getId()) == CardTypeEnum.BUILDING) {
                    List<CardDTO> builds = new ArrayList<>(players.get(i).getMyBuildings());
                    builds.add(c);
                    players.set(i, players.get(i).withMyBuilds(builds));
                } else {
                    List<CardDTO> chars = new ArrayList<>(players.get(i).getMyCharacters());
                    chars.add(c);
                    players.set(i, players.get(i).withMyCharacters(chars));
                }
                break;
            }
        }
    }

    /**
     * Replaces the status flags of the player identified by the given DTO's nickname.
     *
     * @param status the updated status of the player
     */
    public synchronized void onStatusUpdate(PlayerStatusDTO status) {
        for (int i = 0; i < playerStatuses.size(); i++) {
            if (playerStatuses.get(i).getNickname().equals(status.getNickname())) {
                playerStatuses.set(i, new PlayerStatusDTO(
                        status.getNickname(),
                        status.hasProtection(),
                        status.hasDoubleShamanIncome(),
                        status.isExtraFlag(),
                        status.isPaintFlag(),
                        status.getCategoryDiscounts(),
                        status.isHuntBonus()
                ));
                break;
            }
        }
    }

    /**
     * Replaces the stats of the player identified by the given DTO's nickname.
     *
     * @param stats the updated stats of the player
     */
    public synchronized void onStatsUpdate(PlayerStatsDTO stats) {
        for (int i = 0; i < playerStats.size(); i++) {
            if (playerStats.get(i).getNickname().equals(stats.getNickname())) {
                playerStats.set(i, new PlayerStatsDTO(
                        playerStats.get(i).getNickname(),
                        stats.getnFood(),
                        stats.getPPs(),
                        stats.getnStars(),
                        stats.getTotBuildDisc(),
                        stats.getFoodDiscount()));
            }
        }
    }

    /**
     * Replaces the entire player stats list with the given one, typically called
     * after an event resolves and all players' stats change at once.
     *
     * @param stats the updated stats of all players
     */
    public synchronized void updateAllStats(List<PlayerStatsDTO> stats) {
        this.playerStats = new ArrayList<>(stats);
    }

    /**
     * Updates the queue, board, and player stats to reflect that a player's pawn
     * has been returned to the queue tile. Marks the target queue tile as occupied,
     * frees the player's board tile, and updates the player's stats.
     *
     * @param tileDTO        the queue tile the player has returned to
     * @param playerStatsDTO the updated stats of the player who returned to the queue
     */
    public synchronized void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId() == tileDTO.getId()) {
                queue.set(i, new TileDTO(tileDTO, playerStatsDTO.getNickname(), true));
                break;
            }
        }
        for (int i = 0; i < board.size(); i++) {
            TileDTO boardTile = board.get(i);
            if (boardTile.isOccupied() && boardTile.getPlayer().equals(currPlayer)) {
                board.set(i, new TileDTO(boardTile, "", false));
                break;
            }
        }
        for (int i = 0; i < playerStats.size(); i++) {
            if (playerStats.get(i).getNickname().equals(playerStatsDTO.getNickname())) {
                playerStats.set(i, new PlayerStatsDTO(
                        playerStats.get(i).getNickname(),
                        playerStatsDTO.getnFood(),
                        playerStatsDTO.getPPs(),
                        playerStatsDTO.getnStars(),
                        playerStatsDTO.getTotBuildDisc(),
                        playerStatsDTO.getFoodDiscount()));
                break;
            }
        }
    }

    /**
     * Updates the current age and replaces the upper and lower draw lists and
     * the deck size with the data for the new age.
     *
     * @param c the age transition data received from the server
     */
    public synchronized void onChangeAge(ChangeAgeDTO c) {
        this.currAge = c.getAge();
        this.upperList = new ArrayList<>(c.getUpperList());
        this.lowerList = new ArrayList<>(c.getLowerList());
        this.deckSize = c.getDeckSize();
    }

    /**
     * Returns the local player's nickname.
     * @return the local player's nickname */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * Returns the number of cards remaining in the deck.
     * @return the number of cards remaining in the deck */
    public int getDeckSize() {
        return this.deckSize;
    }

    /**
     * Sets the local player's nickname. Called after a successful login.
     *
     * @param nickname the nickname accepted by the server
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Replaces the current draw actions with the given ones.
     * Called when the server notifies the client of available draws.
     *
     * @param a the updated draw actions for the current turn
     */
    public void updateToDoActions(ActionsDTO a) {
        toDoActions = new ActionsDTO(a);
    }

    /**
     * Clears all remaining draw actions for the current turn,
     * called when the player skips their optional draw.
     */
    public void skip() {
        toDoActions = new ActionsDTO(0, 0, false);
    }

    /**
     * Searches for the card with the given ID across the upper and lower draw lists
     * and all players' hands (characters and buildings).
     *
     * @param cardId the unique identifier of the card to find
     * @return the matching {@link CardDTO}
     * @throws InvalidCardException if no card with the given ID is currently visible
     */
    public CardDTO findCardById(int cardId) {
        for (CardDTO c : upperList) {
            if (c.getId() == cardId) return c;
        }
        for (CardDTO c : lowerList) {
            if (c.getId() == cardId) return c;
        }
        for (PlayerDTO p : players) {
            for (CardDTO c : p.getMyCharacters()) {
                if (c.getId() == cardId) return c;
            }
            for (CardDTO c : p.getMyBuildings()) {
                if (c.getId() == cardId) return c;
            }
        }
        throw new InvalidCardException("La carta con ID " + cardId + " non è presente nel gioco.");
    }

    /**
     * Resets all model state to its initial empty values, as if no match were in progress.
     * Called when the player quits or the server crashes.
     */
    public synchronized void reset() {
        this.queue = new ArrayList<>();
        this.board = new ArrayList<>();
        this.upperList = new ArrayList<>();
        this.lowerList = new ArrayList<>();
        this.numPlayers = 0;
        this.currPhaseState = null;
        this.currAge = 1;
        this.players = new ArrayList<>();
        this.currPlayer = "";
        this.currTurn = 0;
        this.playerStatuses = new ArrayList<>();
        this.playerStats = new ArrayList<>();
        this.toDoActions = new ActionsDTO(0, 0, false);
    }
}