package it.polimi.ingsw.visitors;

import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.exceptions.AlreadyExistingUsernameException;
import it.polimi.ingsw.network.client.socket.ClientSocket;
import it.polimi.ingsw.network.dto.PlayerStatsDTO;
import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;
import it.polimi.ingsw.network.messages.service.PongMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Concrete implementation of {@link ServerMessageVisitor}.
 * <p>
 * Handles all messages received from the server by updating the {@link VirtualModel}
 * and notifying the {@link UserInterface}. Order-sensitive UI updates (e.g. events
 * before game ending) are serialized through a single-threaded executor to prevent
 * race conditions.
 * </p>
 */
public class ServerMessageVisitorImpl implements ServerMessageVisitor {

    /** The client-side model keeping track of the game state. */
    private VirtualModel model;

    /** The user interface to notify of game state changes. */
    private final UserInterface ui;

    /** The client socket used to send messages to the server. */
    private final ClientSocket clientSocket;

    /**
     * Single-threaded executor that serializes order-sensitive UI updates.
     * Ensures that, for example, {@code onEvent} is always delivered to the
     * view before {@code onGameEnding}, avoiding the race condition caused
     * by freely scheduled threads.
     */
    private final ExecutorService uiEventExecutor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "ui-event-sequencer"));

    /**
     * Constructs a new {@code ServerMessageVisitorImpl}.
     *
     * @param virtualModel the client-side virtual model to update
     * @param ui           the user interface to notify
     * @param clientSocket the client socket used for outbound communication
     */
    public ServerMessageVisitorImpl(VirtualModel virtualModel, UserInterface ui, ClientSocket clientSocket) {
        this.model = virtualModel;
        this.ui = ui;
        this.clientSocket = clientSocket;
    }

    /**
     * Visits an {@link AvailableLobbiesMessage}: updates lobby availability
     * on the socket and forwards the lobby list to the UI.
     *
     * @param message the message containing the available lobbies
     */
    @Override
    public void visit(AvailableLobbiesMessage message) {
        try {
            var lobbies = message.getLobbies();
            boolean hasLobbies = lobbies != null && lobbies.values().stream().anyMatch(l -> !l.isEmpty());
            clientSocket.setHasAvailableLobbies(hasLobbies);
            ui.displayLobbies(lobbies);
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link ChangeAgeUpdateMessage}: updates the model with the new
     * era and notifies the UI.
     *
     * @param changeAgeUpdateMessage the message containing the new era data
     */
    @Override
    public void visit(ChangeAgeUpdateMessage changeAgeUpdateMessage) {
        try {
            model.onChangeAge(changeAgeUpdateMessage.getAgeDTO());
            ui.onChangeAge(changeAgeUpdateMessage.getAgeDTO().getAge());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link CurrPlayerUpdateMessage}: updates the current player
     * in the model and notifies the UI.
     *
     * @param message the message containing the current player's nickname
     */
    @Override
    public void visit(CurrPlayerUpdateMessage message) {
        try {
            model.onCurrPlayerUpdate(message.getNickname());
            ui.onCurrPlayerUpdate(message.getNickname());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link DrawUpdateMessage}: updates the model with the drawn card
     * and notifies the UI.
     *
     * @param drawUpdateMessage the message containing the drawn card and the player's nickname
     */
    @Override
    public void visit(DrawUpdateMessage drawUpdateMessage) {
        try {
            model.onDrawUpdate(drawUpdateMessage.getCardDTO(), drawUpdateMessage.getNickname());
            ui.onDrawUpdate(drawUpdateMessage.getCardDTO(), drawUpdateMessage.getNickname());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits an {@link ErrorMessage}: forwards the error to the UI.
     *
     * @param errorMessage the message containing the error description
     */
    @Override
    public void visit(ErrorMessage errorMessage) {
        ui.printError(new Exception(errorMessage.getMessage()));
    }

    /**
     * Visits an {@link EventMessage}: snapshots player stats before applying
     * the event, updates the model, then dispatches the UI notification
     * through the serialized executor to preserve ordering.
     *
     * @param eventMessage the message containing the event cards and resulting stats
     */
    @Override
    public void visit(EventMessage eventMessage) {
        List<PlayerStatsDTO> statsBefore = model.getPlayerStats();
        model.updateAllStats(eventMessage.getEvent().getStats());
        try {
            uiEventExecutor.execute(() -> ui.onEvent(eventMessage.getEvent(), statsBefore));
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link GameCreatedMessage}: stores the assigned game ID on the
     * socket and notifies the UI.
     *
     * @param message the message containing the new game's ID
     */
    @Override
    public void visit(GameCreatedMessage message) {
        clientSocket.setMatchId(message.getGameId());
        ui.onCreate(message.getGameId());
    }

    /**
     * Visits a {@link GameJoinedMessage}: stores the game ID on the socket
     * and notifies the UI.
     *
     * @param message the message confirming the game join
     */
    @Override
    public void visit(GameJoinedMessage message) {
        clientSocket.setMatchId(message.getId());
        ui.onJoin(message.getId());
    }

    /**
     * Visits a {@link GameEndingUpdateMessage}: marks the game as ended on the
     * socket and dispatches the UI notification through the serialized executor,
     * ensuring it arrives after any pending event notifications.
     *
     * @param message the message containing final standings and ranking positions
     */
    @Override
    public void visit(GameEndingUpdateMessage message) {
        try {
            clientSocket.setGameEnded();
            uiEventExecutor.execute(() -> ui.onGameEnding(message.getStats(), message.getRankingPos(), message.getGlobalRankingPos()));
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link MoveUpdateMessage}: updates the board state in the model
     * and notifies the UI of the pawn movement.
     *
     * @param message the message containing the target tile and the moving player's nickname
     */
    @Override
    public void visit(MoveUpdateMessage message) {
        try {
            model.onMoveUpdate(message.getTile());
            ui.onMoveUpdate(message.getTile(), message.getNickname());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link NotifyDrawableMessage}: updates the pending actions in the
     * model and notifies the UI to refresh drawable card highlights.
     *
     * @param message the message containing the updated drawable actions
     */
    @Override
    public void visit(NotifyDrawableMessage message) {
        try {
            model.updateToDoActions(message.getActionsDTO());
            ui.showDrawable();
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link NotifySkipMessage}: updates the model and notifies the UI
     * that a player has skipped their optional draw.
     *
     * @param message the message containing the nickname of the player who skipped
     */
    @Override
    public void visit(NotifySkipMessage message) {
        try {
            model.skip();
            ui.notifySkip(message.getNickname());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link PhaseUpdateMessage}: updates the current game phase in the
     * model and notifies the UI.
     *
     * @param message the message containing the new game phase
     */
    @Override
    public void visit(PhaseUpdateMessage message) {
        try {
            model.onPhaseUpdate(message.getPhase());
            ui.onPhaseUpdate(message.getPhase());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link RequestLeaderboardUpdateMessage}: forwards the ranking
     * data to the UI for display.
     *
     * @param message the message containing the current leaderboard rankings
     */
    @Override
    public void visit(RequestLeaderboardUpdateMessage message) {
        try {
            ui.showLeaderboard(message.getRanks());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link ReturnToQueueUpdateMessage}: updates the model with the
     * player's return to the queue and notifies the UI.
     *
     * @param message the message containing the tile and updated player statistics
     */
    @Override
    public void visit(ReturnToQueueUpdateMessage message) {
        try {
            model.onReturnToQueue(message.getTileDTO(), message.getPlayerStatsDTO());
            ui.onReturnToQueue(message.getTileDTO(), message.getPlayerStatsDTO());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link ShowBoardMessage}: updates the full board state in the
     * model and requests the UI to display it.
     *
     * @param showBoardMessage the message containing the full board snapshot
     */
    @Override
    public void visit(ShowBoardMessage showBoardMessage) {
        try {
            model.update(showBoardMessage.getBoard());
            ui.showBoard();
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link StatusUpdateMessage}: updates the player status flags
     * in the model and notifies the UI.
     *
     * @param message the message containing the updated player status
     */
    @Override
    public void visit(StatusUpdateMessage message) {
        try {
            model.onStatusUpdate(message.getStatus());
            ui.onStatusUpdate(message.getStatus());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link StatsUpdateMessage}: updates player statistics in the
     * model and notifies the UI.
     *
     * @param statsUpdateMessage the message containing the updated statistics
     */
    @Override
    public void visit(StatsUpdateMessage statsUpdateMessage) {
        try {
            model.onStatsUpdate(statsUpdateMessage.getStats());
            ui.onStatsUpdate(statsUpdateMessage.getStats());
        } catch (Exception e) {
            ui.printError(e);
        }
    }

    /**
     * Visits a {@link PingMessage}: replies with a {@link PongMessage} to confirm
     * the connection is alive. Triggers a quit and crash notification if the send fails.
     *
     * @param pingMessage the ping message received from the server
     */
    @Override
    public void visit(PingMessage pingMessage) {
        try {
            clientSocket.sendMessage(new PongMessage());
        } catch (Exception e) {
            model = ui.quit();
            ui.onServerCrash();
        }
    }

    /**
     * Visits a {@link QuitAckMessage}: resets the model, notifies the UI of the
     * quit, clears the match ID, and reinstalls a fresh visitor on the socket.
     *
     * @param ackMessage the quit acknowledgement message containing the quit reason
     */
    @Override
    public void visit(QuitAckMessage ackMessage) {
        this.model = ui.quit();
        ui.onQuit(ackMessage.getReason());
        clientSocket.setMatchId(0);
        clientSocket.setVisitor(new ServerMessageVisitorImpl(model, ui, clientSocket));
    }

    /**
     * Visits a {@link LoginSuccessMessage}: confirms the login on the socket and,
     * if the client is not reconnecting mid-game, notifies the UI.
     *
     * @param message the message confirming the successful login
     */
    @Override
    public void visit(LoginSuccessMessage message) {
        clientSocket.onLoginSuccess();
        if (!clientSocket.isInGame()) {
            ui.onLogin(message.getNickname());
        }
    }

    /**
     * Visits a {@link LoginFailedMessage}: notifies the UI of the failure and
     * signals the socket that login was unsuccessful.
     *
     * @param message the message containing the reason for login failure
     */
    @Override
    public void visit(LoginFailedMessage message) {
        ui.printError(new AlreadyExistingUsernameException(message.getError()));
        clientSocket.onLoginFailed();
    }

    /**
     * Visits a {@link ReconnectionMessage}: stores the match ID on the socket
     * and triggers the reconnection flow in the UI.
     *
     * @param message the message containing the match ID to reconnect to
     */
    @Override
    public void visit(ReconnectionMessage message) {
        clientSocket.setMatchId(message.getMatchId());
        ui.reconnect(message.getMatchId());
    }

    /**
     * Visits a {@link RankingResponseMessage}: displays the global ranking
     * in the UI on a dedicated thread.
     *
     * @param rankingResponseMessage the message containing the global ranking data
     */
    @Override
    public void visit(RankingResponseMessage rankingResponseMessage) {
        try {
            new Thread(() -> ui.showRanking(rankingResponseMessage.getRanking()), "End-Game UI").start();
        } catch (Exception e) {
            ui.printError(e);
        }
    }
}