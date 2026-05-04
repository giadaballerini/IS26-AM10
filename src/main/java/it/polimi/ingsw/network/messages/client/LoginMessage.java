package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class LoginMessage implements ClientMessage {

    private final String nickname;
    //private final String password;

    public LoginMessage(String nickname) {
        this.nickname = nickname;
        //this.password = password;
    }

    public void accept(ClientMessageVisitor visitor){
        visitor.visit(this);
    }

    public String getNickname() {
        return nickname;
    }

}
