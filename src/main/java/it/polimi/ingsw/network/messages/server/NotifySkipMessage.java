package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class NotifySkipMessage implements ServerMessage {
    private final String nickname;
    public NotifySkipMessage(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
