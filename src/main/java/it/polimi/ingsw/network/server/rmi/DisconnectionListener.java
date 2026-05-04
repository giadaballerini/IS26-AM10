package it.polimi.ingsw.network.server.rmi;

public interface DisconnectionListener {
    void handleDisconnection(String nickname);
}
