package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.BoardDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class ShowBoardMessage implements ServerMessage {
    private final BoardDTO board;

    public ShowBoardMessage(BoardDTO board){
        this.board = board;
    }
    public BoardDTO getBoard() {
        return board;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
