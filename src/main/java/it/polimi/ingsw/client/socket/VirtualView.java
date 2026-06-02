package it.polimi.ingsw.client.socket;

import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.observer.ModelObserver;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.util.List;
import java.util.Map;

public interface VirtualView extends ModelObserver {

    void onLogin(String nickname);
    void onLoginSuccess(String nickname);
    void onLoginFailed(String error);
    void onLobbiesRequested(Map<Integer, List<LobbyDTO>> lobbies);
    void handleDisconnection(String nickname);
    void onGameCreated(int gameId);
    void onClientMessage(ClientMessage message);
    void setVisitor(ClientMessageVisitor visitor);
    void onJoinGame(int gameid);
    void onRankingResponse(Map<String, Integer> ranking);
}