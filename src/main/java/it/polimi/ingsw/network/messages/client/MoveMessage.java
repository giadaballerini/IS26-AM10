package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

/**
 * Sent by the client to place the player's pawn on a board tile.
 *
 * <p>The server validates the move and broadcasts a
 * {@link it.polimi.ingsw.network.messages.server.MoveUpdateMessage} to all
 * clients if the move is accepted.
 */
public class MoveMessage implements ClientMessage {

    /** Nickname of the player making the move. */
    private final String nickname;

    /** Position (ID) of the tile the player wants to occupy. */
    private final int tilePos;

    /**
     * Creates a {@code MoveMessage} for the given player and tile.
     *
     * @param nickname nickname of the moving player
     * @param tilePos  ID of the target tile
     */
    public MoveMessage(String nickname, int tilePos) {
        this.nickname = nickname;
        this.tilePos = tilePos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the nickname of the player making the move.
     *
     * @return player nickname
     */
    public String getPlayer() {
        return this.nickname;
    }

    /**
     * Returns the ID of the tile the player wants to occupy.
     *
     * @return target tile ID
     */
    public int getTilePos() {
        return this.tilePos;
    }
}