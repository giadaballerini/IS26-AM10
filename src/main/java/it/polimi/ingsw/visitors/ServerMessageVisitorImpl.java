package it.polimi.ingsw.visitors;

import it.polimi.ingsw.client.UserInterface;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.exceptions.AlreadyExistingUsernameException;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;
import it.polimi.ingsw.network.messages.service.PongMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMessageVisitorImpl implements ServerMessageVisitor{

    private VirtualModel model;
    private final UserInterface ui;
    private final ClientSocket clientSocket;

    /**
     * Executor single-threaded che serializza gli aggiornamenti UI sensibili
     * all'ordine (es. onEvent deve arrivare alla view PRIMA di onGameEnding).
     * Sostituisce i Thread "liberi" che, essendo schedulati dalla JVM in modo
     * non deterministico, causavano la race condition per cui il banner degli
     * eventi di fine partita non veniva mai visualizzato.
     */
    private final ExecutorService uiEventExecutor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "ui-event-sequencer"));

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
            clientSocket.setHasAvailableLobbies(hasLobbies);
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
        List<PlayerStatsDTO> statsBefore = model.getPlayerStats();
        model.updateAllStats(eventMessage.getEvent().getStats());
        try {
            uiEventExecutor.execute(() -> ui.onEvent(eventMessage.getEvent(), statsBefore));
        }catch(Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(GameCreatedMessage message) {
        clientSocket.setMatchId(message.getGameId());
        ui.onCreate(message.getGameId());
    }

    @Override
    public void visit(GameJoinedMessage message) {
        clientSocket.setMatchId(message.getId());
        ui.onJoin(message.getId());
    }
    @Override
    public void visit(GameEndingUpdateMessage message) {
        try{
            clientSocket.setGameEnded();
            uiEventExecutor.execute(() -> ui.onGameEnding(message.getStats(), message.getRankingPos(), message.getGlobalRankingPos()));
        } catch (Exception e){
            ui.printError(e);
        }
    }

    @Override
    public void visit(MoveUpdateMessage message) {
        try{
            model.onMoveUpdate(message.getTile());
            ui.onMoveUpdate(message.getTile(), message.getNickname());
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
        try{
            clientSocket.sendMessage(new PongMessage());
        }catch (Exception e){
            model = ui.quit();
            ui.onServerCrash();
        }
    }

    @Override
    public void visit(QuitAckMessage ackMessage){
        this.model = ui.quit();
        ui.onQuit(ackMessage.getReason());
        clientSocket.setMatchId(0);
        clientSocket.setVisitor(new ServerMessageVisitorImpl(model,ui,clientSocket));
    }

    @Override
    public void visit(LoginSuccessMessage message){
        clientSocket.onLoginSuccess();
        if (!clientSocket.isInGame()) {
            ui.onLogin(message.getNickname());
        }
    }

    @Override
    public void visit(LoginFailedMessage message){
        ui.printError(new AlreadyExistingUsernameException(message.getError()));
        clientSocket.onLoginFailed();
    }

    @Override
    public void visit(ReconnectionMessage message){
        clientSocket.setMatchId(message.getMatchId());
        ui.reconnect(message.getMatchId());
    }

    @Override
    public void visit(RankingResponseMessage rankingResponseMessage) {
        try{
            new Thread(() -> ui.showRanking(rankingResponseMessage.getRanking()), "End-Game UI").start();
        }catch(Exception e){
            ui.printError(e);
        }
    }
}