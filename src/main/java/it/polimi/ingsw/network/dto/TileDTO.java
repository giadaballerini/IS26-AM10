package it.polimi.ingsw.network.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a single board tile as transferred between server and client.
 *
 * <p>Carries all the information needed to render the tile on the board: its
 * identity, occupancy state, the player who placed a pawn on it, and the
 * rewards it grants when occupied.
 */
public class TileDTO implements Serializable {
    /**
     * Required by the {@link Serializable} interface.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Whether a player's pawn is currently placed on this tile. */
    private final boolean occupied;

    /** Unique identifier of this tile. */
    private final int id;

    /**
     * Minimum number of players required in the match for this tile to be
     * available.
     */
    private final int minPlayers;

    /** Nickname of the player whose pawn is on this tile, or {@code null} if unoccupied. */
    private final String player;

    /** Number of upper row draws the player earns by occupying this tile. */
    private final int upDraws;

    /** Number of lower row draws the player earns by occupying this tile. */
    private final int downDraws;

    /** Amount of food the player earns by occupying this tile. */
    private final int foodAmount;

    /**
     * Creates a {@code TileDTO} with all attributes specified explicitly.
     *
     * @param occupied    {@code true} if the tile is currently occupied
     * @param id          unique tile identifier
     * @param minPlayers  minimum number of players required for this tile
     * @param player      nickname of the occupying player, or {@code null}
     * @param upDraws     the upper row draws granted by this tile
     * @param downDraws   the lower row draws granted by this tile
     * @param foodAmount  food granted by this tile
     */
    public TileDTO(boolean occupied, int id, int minPlayers, String player,
                   int upDraws, int downDraws, int foodAmount) {
        this.occupied = occupied;
        this.id = id;
        this.minPlayers = minPlayers;
        this.player = player;
        this.upDraws = upDraws;
        this.downDraws = downDraws;
        this.foodAmount = foodAmount;
    }

    /**
     * Copy constructor that overrides the occupancy state and the occupying
     * player. All other attributes are copied from {@code tileDTO}.
     *
     * @param tileDTO  the source tile
     * @param player   nickname of the new occupying player, or {@code null}
     * @param occupied new occupancy state
     */
    public TileDTO(TileDTO tileDTO, String player, boolean occupied) {
        this.occupied = occupied;
        this.id = tileDTO.getId();
        this.minPlayers = tileDTO.getMinPlayers();
        this.player = player;
        this.upDraws = tileDTO.getUpDraws();
        this.downDraws = tileDTO.getDownDraws();
        this.foodAmount = tileDTO.getFoodAmount();
    }

    /**
     * Returns whether a player's pawn is currently on this tile.
     *
     * @return {@code true} if the tile is occupied
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Returns the unique identifier of this tile.
     *
     * @return tile ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the minimum number of players required for this tile to be
     * available in the match.
     *
     * @return minimum player count
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Returns the nickname of the player whose pawn is on this tile, or
     * {@code null} if the tile is unoccupied.
     *
     * @return occupying player's nickname, or {@code null}
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Returns the number of upper-row draws the player earns by occupying
     * this tile.
     *
     * @return upper-row draw reward
     */
    public int getUpDraws() {
        return upDraws;
    }

    /**
     * Returns the number of lower-row draws the player earns by occupying
     * this tile.
     *
     * @return lower-row draw reward
     */
    public int getDownDraws() {
        return downDraws;
    }

    /**
     * Returns the amount of food the player earns by occupying this tile.
     *
     * @return food reward
     */
    public int getFoodAmount() {
        return foodAmount;
    }
}