package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class SkipMessage implements ClientMessage {

    private final String nickname;

    public SkipMessage(String nickname){
        this.nickname = nickname;
    }

    public void accept(ClientMessageVisitor visitor){
        visitor.visit(this);
    }

    public String getNickname(){
        return nickname;
    }
}
