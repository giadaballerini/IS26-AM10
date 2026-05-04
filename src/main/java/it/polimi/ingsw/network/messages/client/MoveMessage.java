package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class MoveMessage implements ClientMessage {
    private final String nickname;
    private final int tilePos;

    public MoveMessage(String nickname, int tilePos){
        this.nickname = nickname;
        this.tilePos = tilePos;
    }

    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    public String getPlayer(){return this.nickname;}
    public int getTilePos(){return this.tilePos;}
}
