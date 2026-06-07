package it.polimi.ingsw.visitors;

import it.polimi.ingsw.network.messages.server.*;
import it.polimi.ingsw.network.messages.service.PingMessage;

/**
 * Visitor for messages sent from the server to the client.
 * <p>
 * Defines visit methods for all server-side message types, covering game state
 * updates, session management responses, and service messages.
 * </p>
 */
public interface ServerMessageVisitor {

    /**
     * Visits a message notifying that the game has advanced to a new era.
     *
     * @param changeAgeUpdateMessage the message containing the new era index
     */
    void visit(ChangeAgeUpdateMessage changeAgeUpdateMessage);

    /**
     * Visits a message notifying which player's turn it currently is.
     *
     * @param currPlayerUpdateMessage the message containing the current player's nickname
     */
    void visit(CurrPlayerUpdateMessage currPlayerUpdateMessage);

    /**
     * Visits a message notifying that a player has drawn a card.
     *
     * @param drawUpdateMessage the message containing the drawn card and the player's nickname
     */
    void visit(DrawUpdateMessage drawUpdateMessage);

    /**
     * Visits a message notifying that the game has ended, containing final standings.
     *
     * @param gameEndingUpdateMessage the message containing end-game statistics
     */
    void visit(GameEndingUpdateMessage gameEndingUpdateMessage);

    /**
     * Visits a message notifying that a player has moved their pawn to a tile.
     *
     * @param moveUpdateMessage the message containing the target tile and the player's nickname
     */
    void visit(MoveUpdateMessage moveUpdateMessage);

    /**
     * Visits a message notifying the client which cards are currently drawable.
     *
     * @param notifyDrawableMessage the message containing drawable card information
     */
    void visit(NotifyDrawableMessage notifyDrawableMessage);

    /**
     * Visits a message notifying that a player has skipped their optional draw.
     *
     * @param notifySkipMessage the message containing the nickname of the player who skipped
     */
    void visit(NotifySkipMessage notifySkipMessage);

    /**
     * Visits a message notifying that the game phase has changed.
     *
     * @param phaseUpdateMessage the message containing the new game phase
     */
    void visit(PhaseUpdateMessage phaseUpdateMessage);

    /**
     * Visits a message requesting the client to update the leaderboard display.
     *
     * @param requestLeaderboardUpdateMessage the leaderboard update request message
     */
    void visit(RequestLeaderboardUpdateMessage requestLeaderboardUpdateMessage);

    /**
     * Visits a message notifying that a player's pawn has returned to the queue.
     *
     * @param returnToQueueUpdateMessage the message containing the tile and player statistics
     */
    void visit(ReturnToQueueUpdateMessage returnToQueueUpdateMessage);

    /**
     * Visits a message requesting the client to display the full game board.
     *
     * @param showBoardMessage the board display request message
     */
    void visit(ShowBoardMessage showBoardMessage);

    /**
     * Visits a message notifying an update to a player's status flags.
     *
     * @param statusUpdateMessage the message containing the updated player status
     */
    void visit(StatusUpdateMessage statusUpdateMessage);

    /**
     * Visits a message notifying an update to one or more players' statistics.
     *
     * @param statsUpdateMessage the message containing the updated player statistics
     */
    void visit(StatsUpdateMessage statsUpdateMessage);

    /**
     * Visits a ping message from the server, used to check connection liveness.
     *
     * @param pingMessage the ping message
     */
    void visit(PingMessage pingMessage);

    /**
     * Visits a message containing the events that occurred during the event phase.
     *
     * @param eventMessage the message containing the event cards and their effects
     */
    void visit(EventMessage eventMessage);

    /**
     * Visits a message containing the list of available lobbies.
     *
     * @param availableLobbiesMessage the message containing the lobby list
     */
    void visit(AvailableLobbiesMessage availableLobbiesMessage);

    /**
     * Visits a message confirming that a new game has been created.
     *
     * @param gameCreatedMessage the message containing the new game's ID
     */
    void visit(GameCreatedMessage gameCreatedMessage);

    /**
     * Visits an error message sent by the server.
     *
     * @param errorMessage the message containing the error description
     */
    void visit(ErrorMessage errorMessage);

    /**
     * Visits a message confirming that the client has successfully joined a game.
     *
     * @param gameJoinedMessage the message confirming the game join
     */
    void visit(GameJoinedMessage gameJoinedMessage);

    /**
     * Visits a message acknowledging the client's quit request.
     *
     * @param quitAckMessage the quit acknowledgement message
     */
    void visit(QuitAckMessage quitAckMessage);

    /**
     * Visits a message confirming a successful login.
     *
     * @param loginSuccessMessage the message confirming the login success
     */
    void visit(LoginSuccessMessage loginSuccessMessage);

    /**
     * Visits a message notifying that the login attempt has failed.
     *
     * @param loginFailedMessage the message containing the reason for login failure
     */
    void visit(LoginFailedMessage loginFailedMessage);

    /**
     * Visits a message notifying that a previously disconnected player has reconnected.
     *
     * @param reconnectionMessage the message containing the reconnected player's game state
     */
    void visit(ReconnectionMessage reconnectionMessage);

    /**
     * Visits a message containing the global ranking response.
     *
     * @param rankingResponseMessage the message containing the ranking data
     */
    void visit(RankingResponseMessage rankingResponseMessage);
}