package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class CurrPlayerUpdateMessage implements ServerMessage {

    private final String nickname;

    public CurrPlayerUpdateMessage(String nickname){
        this.nickname = nickname;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    public String getNickname() {
        return nickname;
    }
}
