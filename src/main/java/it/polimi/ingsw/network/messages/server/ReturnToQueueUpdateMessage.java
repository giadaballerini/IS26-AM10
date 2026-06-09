package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.dto.TileDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server to a player who has been returned to the waiting queue.
 *
 * <p>Carries the tile that was freed and the player's updated statistics
 * so the client can animate the pawn removal and refresh the stats panel.
 */
public class ReturnToQueueUpdateMessage implements ServerMessage {

    /** The tile from which the player's pawn was removed. */
    private final TileDTO tileDTO;

    /** The player's updated statistics after being returned to the queue. */
    private final PlayerStatsDTO playerStatsDTO;

    /**
     * Creates a {@code ReturnToQueueUpdateMessage} for the given tile and
     * player stats.
     *
     * @param tileDTO        the freed tile
     * @param playerStatsDTO the player's updated statistics
     */
    public ReturnToQueueUpdateMessage(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        this.tileDTO = tileDTO;
        this.playerStatsDTO = playerStatsDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the tile from which the player's pawn was removed.
     *
     * @return freed tile DTO
     */
    public TileDTO getTileDTO() {
        return tileDTO;
    }

    /**
     * Returns the player's updated statistics after returning to the queue.
     *
     * @return updated player stats DTO
     */
    public PlayerStatsDTO getPlayerStatsDTO() {
        return playerStatsDTO;
    }
}