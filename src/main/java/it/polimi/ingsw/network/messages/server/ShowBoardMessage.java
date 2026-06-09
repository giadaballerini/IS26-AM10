package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.BoardDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

/**
 * Sent by the server with the full board state, typically at game start or
 * after a client reconnects, so the client can rebuild its local model.
 */
public class ShowBoardMessage implements ServerMessage {

    /** Complete snapshot of the current board state. */
    private final BoardDTO board;

    /**
     * Creates a {@code ShowBoardMessage} carrying the given board snapshot.
     *
     * @param board full board state
     */
    public ShowBoardMessage(BoardDTO board) {
        this.board = board;
    }

    /**
     * Returns the full board state snapshot.
     *
     * @return board DTO
     */
    public BoardDTO getBoard() {
        return board;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}