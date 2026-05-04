package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class JoinGameMessage implements ClientMessage {

    private final int id;
    private final String nickname;

    public JoinGameMessage(int id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
    public void accept(ClientMessageVisitor visitor){
        visitor.visit(this);
    }

    public int getId() {
        return id;
    }
    public String getNickname() {
        return nickname;
    }
}
