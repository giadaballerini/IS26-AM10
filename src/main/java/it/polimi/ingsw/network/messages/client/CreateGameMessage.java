package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class CreateGameMessage implements ClientMessage{

    private final int numPlayers;
    private final String nickname;

    public CreateGameMessage(int numPlayers, String nickname){
        this.numPlayers = numPlayers;
        this.nickname = nickname;
    }
    public void accept(ClientMessageVisitor visitor){
        visitor.visit(this);
    }

    public int getNumPlayers() {
        return numPlayers;
    }
    public String getNickname() {
        return nickname;
    }
}
