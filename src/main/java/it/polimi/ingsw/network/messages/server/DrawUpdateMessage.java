package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.network.dto.CardDTO;
import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class DrawUpdateMessage implements ServerMessage {

    private final CardDTO c;
    private final String nickname;

    public DrawUpdateMessage(CardDTO c, String nickname) {
        this.c = c;
        this.nickname = nickname;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }

    public CardDTO getCardDTO() {
        return c;
    }
    public String getNickname() {
        return nickname;
    }
}
