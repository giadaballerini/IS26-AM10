package it.polimi.ingsw.visitors;

import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.exceptions.InvalidTimingException;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.service.PongMessage;
import it.polimi.ingsw.network.server.MatchManager;

import java.util.List;
import java.util.Map;

public class ClientMessageVisitorImpl implements ClientMessageVisitor{

    private final MatchManager matchManager;
    private final VirtualView clientHandler;
    private volatile GameMessageVisitor gameVisitor;

    public ClientMessageVisitorImpl(MatchManager matchManager, VirtualView clientHandler) {
        this.matchManager = matchManager;
        this.clientHandler = clientHandler;
    }

    public void setGameVisitor(GameMessageVisitor gameVisitor){
        this.gameVisitor = gameVisitor;
    }

    @Override
    public void visit(MoveMessage moveMessage) {
        try{
            if(gameVisitor == null)
                throw new InvalidTimingException("Non puoi usare questto comando al momento");
            gameVisitor.visit(moveMessage);
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(DrawMessage drawMessage) {
        try{
            if(gameVisitor == null)
                throw new InvalidTimingException("Non puoi usare questto comando al momento");
            gameVisitor.visit(drawMessage);
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(SkipMessage skipMessage) {
        try{
            if(gameVisitor == null)
                throw new InvalidTimingException("Non puoi usare questto comando al momento");
            gameVisitor.visit(skipMessage);
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
            clientHandler.onJoinGame(joinGameMessage.getId());
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
    public void visit(LoginMessage loginMessage) {
        String nickname = loginMessage.getNickname();
        try {
            matchManager.login(nickname, this.clientHandler);
            clientHandler.onLogin(nickname);
            clientHandler.onLoginSuccess(nickname);
        }catch (Exception e){
            clientHandler.onLoginFailed(e.getMessage());
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
            System.out.println("[PONG] Ricevuto da " + clientHandler.getNickname());
        }catch (Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    @Override
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

    public void visit(RankingRequestMessage rankingRequestMessage){
        try{
            Map<String, Integer> ranking = matchManager.requestRanking(clientHandler.getNickname());
            clientHandler.onRankingResponse(ranking);
        }catch(Exception e){
            clientHandler.onErrorMessage(e.getMessage());
        }
    }
}