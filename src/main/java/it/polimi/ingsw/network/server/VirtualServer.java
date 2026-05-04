package it.polimi.ingsw.network.server;

import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.network.dto.LobbyDTO;

import java.util.List;
import java.util.Map;

public interface VirtualServer {
    void login(String nickname, VirtualViewRmi clientStub);

    int createGame(String nickname, int numPlayers);

    void joinGame(String nickname, int id);

    void move(String nickname, int tileId);

    void draw(int id, String nickname);

    void skip(String nickname);

    void quit(String nickname);

    Map<Integer, List<LobbyDTO>> getLobbies(String nickname);

    Map<String, Integer> requestRanking(String nickname);

    void handleDisconnection(String nickname);
}
