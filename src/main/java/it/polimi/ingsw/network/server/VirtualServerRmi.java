package it.polimi.ingsw.network.server;

import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.exceptions.InvalidTimingException;
import it.polimi.ingsw.network.dto.LobbyDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface VirtualServerRmi extends Remote {
    void login(String nickname, VirtualViewRmi clientStub) throws RemoteException;

    int createGame(String nickname, int numPlayers) throws RemoteException;

    void joinGame(String nickname, int id) throws RemoteException;

    void move(String nickname, int tileId) throws RemoteException;

    void draw(int id, String nickname) throws RemoteException;

    void skip(String nickname) throws RemoteException;

    void quit(String nickname) throws RemoteException, InvalidTimingException;

    Map<Integer, List<LobbyDTO>> getLobbies(String nickname) throws RemoteException;

    Map<String, Integer> requestRanking(String nickname) throws RemoteException;

    void handleDisconnection(String nickname) throws RemoteException;
    void ping() throws RemoteException;

}
