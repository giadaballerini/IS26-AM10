package it.polimi.ingsw.network.messages.server;

import it.polimi.ingsw.visitors.ServerMessageVisitor;

public class LoginFailedMessage implements  ServerMessage {
    private final String error;
    public LoginFailedMessage(String error) {
        this.error = error;
    }
    public String getError() {
        return this.error;
    }

    @Override
    public void accept(ServerMessageVisitor visitor) {
        visitor.visit(this);
    }
}
