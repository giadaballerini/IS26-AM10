package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.TileDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Broadcast by the server to all clients when a player places their pawn on a
 * board tile.
 */
public class MoveUpdateMessage implements ServerMessage {

    /** The tile that was just occupied with the updated state. */
    private final TileDTO tile;

    /** Nickname of the player who made the move. */
    private final String nickname;

    /**
     * Creates a {@code MoveUpdateMessage} for the given tile and player.
     *
     * @param tile     updated tile DTO reflecting the new occupancy
     * @param nickname nickname of the player who moved
     */
    public MoveUpdateMessage(TileDTO tile, String nickname) {
        this.tile = tile;
        this.nickname = nickname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the tile that was just occupied.
     *
     * @return updated tile DTO
     */
    public TileDTO getTile() {
        return tile;
    }

    /**
     * Returns the nickname of the player who made the move.
     *
     * @return moving player's nickname
     */
    public String getNickname() {
        return nickname;
    }
}