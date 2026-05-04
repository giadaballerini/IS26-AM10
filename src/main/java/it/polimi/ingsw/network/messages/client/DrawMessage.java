package it.polimi.ingsw.network.messages.client;

import it.polimi.ingsw.visitors.ClientMessageVisitor;

public class DrawMessage implements ClientMessage {

    private final int cardId;
    private final String nickname;

    public DrawMessage(int cardId, String nickname)
    {
        this.cardId = cardId;
        this.nickname = nickname;
    }
    public void accept(ClientMessageVisitor visitor){
        visitor.visit(this);
    }

    public int getCardId(){
        return cardId;
    }
    public String getNickname(){return nickname;}
}
