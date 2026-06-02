package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class LoginSuccessMessage implements ServerMessage {
    private final String nickname;
    public LoginSuccessMessage(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname(){
        return this.nickname;
    }
    public void accept(ServerMessageVisitor message){
        message.visit(this);
    }
}
