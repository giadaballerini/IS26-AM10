package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.server.MatchManager;
import it.polimi.ingsw.network.server.VirtualServerRmi;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class ServerRmi extends UnicastRemoteObject implements VirtualServerRmi, DisconnectionListener {
    private MatchManager matchManager;
    public ServerRmi(MatchManager matchManager) throws RemoteException {
        super();
        this.matchManager = matchManager;
    }


    public void login(String nickname, VirtualViewRmi clientStub) throws RemoteException, AlreadyExistingUsernameException, InvalidTimingException{
        ClientHandlerRmi rmiHandler = new ClientHandlerRmi(nickname, clientStub, this);
        matchManager.login(nickname, rmiHandler);

    }
    public int createGame(String nickname, int numPlayers) throws RemoteException, InvalidTimingException{
        return matchManager.createGame(nickname, numPlayers);
    }

    public void joinGame(String nickname, int id) throws RemoteException, InvalidLobbyException, InvalidTimingException{
        matchManager.joinGame(nickname, id);
    }

    public void move(String nickname, int tileId) throws RemoteException, InvalidMoveException, InvalidPhaseException, InvalidPlayerException, InvalidTimingException{
        matchManager.move(nickname, tileId);
    }

    public void draw(int id, String nickname) throws RemoteException, InvalidDrawException, InvalidTimingException {
        matchManager.drawCard(id, nickname);
    }

    public void skip(String nickname) throws RemoteException, InvalidSkipException, InvalidTimingException{
        matchManager.skip(nickname);
    }

    @Override
    public void quit(String nickname) throws RemoteException, InvalidTimingException{
        matchManager.quit(nickname);
    }

    public Map<Integer, List<LobbyDTO>> getLobbies(String nickname) throws RemoteException, InvalidTimingException {
        return matchManager.getLobbies(nickname);
    }

    public Map<String, Integer> requestRanking(String nickname) throws RemoteException, InvalidTimingException {
        return matchManager.requestRanking(nickname);
    }

    public void handleDisconnection(String nickname){   // non servono le exception, le catchiamo già
        matchManager.disconnect(nickname);
    }

    public void ping(){
        
    }
}
