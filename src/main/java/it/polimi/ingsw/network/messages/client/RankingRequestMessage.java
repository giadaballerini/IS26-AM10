package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class RankingRequestMessage implements ClientMessage{
    private final String nickname;

    public RankingRequestMessage(String nickname){
        this.nickname = nickname;
    }

    @Override
    public void accept(ClientMessageVisitor visitor) {
        visitor.visit(this);
    }

    public String getNickname(){return this.nickname;}
}
