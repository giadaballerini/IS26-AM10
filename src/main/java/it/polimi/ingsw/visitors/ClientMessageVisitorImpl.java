package it.polimi.ingsw.visitors;

import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.service.PongMessage;
import it.polimi.ingsw.network.server.MatchManager;
import it.polimi.ingsw.network.server.socket.ClientHandlerSocket;

import java.util.List;
import java.util.Map;

public class ClientMessageVisitorImpl implements ClientMessageVisitor{

    private final MatchManager matchManager;
    private final VirtualView clientHandler;


    public ClientMessageVisitorImpl(MatchManager matchManager, VirtualView clientHandler) {
        this.matchManager = matchManager;
        this.clientHandler = clientHandler;
    }

    @Override
    public void visit(MoveMessage moveMessage) {
        try{
            matchManager.move(clientHandler.getNickname(), moveMessage.getTilePos());
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(DrawMessage drawMessage) {
        try{
            matchManager.drawCard(drawMessage.getCardId(), clientHandler.getNickname());
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(SkipMessage skipMessage) {
        try{
            matchManager.skip(clientHandler.getNickname());
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(CreateGameMessage createGameMessage) {
        try{
            int gameId = matchManager.createGame(clientHandler.getNickname(), createGameMessage.getNumPlayers());
            clientHandler.onGameCreated(gameId);
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(JoinGameMessage joinGameMessage) {
        try{
            matchManager.joinGame(clientHandler.getNickname(), joinGameMessage.getId());
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(LoginMessage loginMessage) {
        String nickname = loginMessage.getNickname();
        try {
            clientHandler.onLogin(nickname);
            matchManager.login(nickname, this.clientHandler);
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(AskLobbiesMessage askLobbiesMessage) {
        try{
            Map<Integer, List<LobbyDTO>> lobbies = matchManager.getLobbies(clientHandler.getNickname());
            clientHandler.onLobbiesRequested(lobbies);
        } catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(PongMessage pongMessage) {
        try{
            //TODO
        } catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    public void visit(QuitMessage quitMessage){
        try {
            matchManager.quit(clientHandler.getNickname());
        }catch(Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(ExitMessage exitMessage) {
        try{
            clientHandler.handleDisconnection(clientHandler.getNickname());
        } catch (Exception e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }
}
