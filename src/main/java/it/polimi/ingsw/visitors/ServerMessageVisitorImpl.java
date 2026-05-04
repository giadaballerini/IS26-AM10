package it.polimi.ingsw.visitors;

import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;

public class ServerMessageVisitorImpl implements ServerMessageVisitor{

    private VirtualModel model;
    private final UserInterface ui;
    private final ClientSocket clientSocket;

    public ServerMessageVisitorImpl(VirtualModel virtualModel, UserInterface ui, ClientSocket clientSocket) {
        this.model = virtualModel;
        this.ui = ui;
        this.clientSocket = clientSocket;
    }

    @Override
    public void visit(AvailableLobbiesMessage message){
        try{
            var lobbies = message.getLobbies();
            boolean hasLobbies = lobbies != null && lobbies.values().stream().anyMatch(l -> !l.isEmpty());
            ui.displayLobbies(lobbies);
        } catch (Exception e){
            ui.printError(e);
        }
    }
    @Override
    public void visit(ChangeAgeUpdateMessage changeAgeUpdateMessage) {
        try{
            model.onChangeAge(changeAgeUpdateMessage.getAgeDTO());
            ui.onChangeAge(changeAgeUpdateMessage.getAgeDTO().getAge());
            ui.showBoard();
        }catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(CurrPlayerUpdateMessage message) {
        try{
            model.onCurrPlayerUpdate(message.getNickname());
            ui.onCurrPlayerUpdate(message.getNickname());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(DrawUpdateMessage drawUpdateMessage) {
        try{
            model.onDrawUpdate(drawUpdateMessage.getCardDTO(), drawUpdateMessage.getNickname());
            ui.onDrawUpdate(drawUpdateMessage.getCardDTO(), drawUpdateMessage.getNickname());
            ui.showBoard();
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(ErrorMessage errorMessage) {
        ui.printError(new Exception(errorMessage.getMessage()));
    }

    @Override
    public void visit(EventMessage eventMessage) {
        ui.onEvent(eventMessage.getEvent());
    }

    @Override
    public void visit(GameCreatedMessage message) {
        ui.onCreate(message.getGameId());
    }

    @Override
    public void visit(GameEndingUpdateMessage message) {
        try{
            ui.onGameEnding(message.getStats(), message.getRankingPos());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(MoveUpdateMessage message) {
        try{
            model.onMoveUpdate(message.getTile());
            ui.onMoveUpdate(message.getTile(), message.getNickname());
            ui.showBoard();
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(NotifyDrawableMessage message) {
        try{
            model.updateToDoActions(message.getActionsDTO());
            ui.showDrawable();
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(NotifySkipMessage message) {
        try{
            model.skip();
            ui.notifySkip(message.getNickname());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(PhaseUpdateMessage message) {
        try{
            model.onPhaseUpdate(message.getPhase());
            ui.onPhaseUpdate(message.getPhase());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(RequestLeaderboardUpdateMessage message) {
        try{
            ui.showLeaderboard(message.getRanks());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(ReturnToQueueUpdateMessage message) {
        try{
            model.onReturnToQueue(message.getTileDTO(), message.getPlayerStatsDTO());
            ui.onReturnToQueue(message.getTileDTO(), message.getPlayerStatsDTO());
            ui.showBoard();
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(ShowBoardMessage showBoardMessage) {
        try{
            model.update(showBoardMessage.getBoard());
            ui.showBoard();
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(StatusUpdateMessage message) {
        try{
            model.onStatusUpdate(message.getStatus());
            ui.onStatusUpdate(message.getStatus());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(StatsUpdateMessage statsUpdateMessage) {
        try{
            model.onStatsUpdate(statsUpdateMessage.getStats());
            ui.onStatsUpdate(statsUpdateMessage.getStats());
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(PingMessage pingMessage) {

    }


}
