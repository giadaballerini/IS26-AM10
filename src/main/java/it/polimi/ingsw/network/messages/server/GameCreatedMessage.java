package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class GameCreatedMessage implements ServerMessage{
    private final int gameId;
    public GameCreatedMessage(int gameId){
        this.gameId = gameId;
    }
    public int getGameId() {
        return gameId;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
