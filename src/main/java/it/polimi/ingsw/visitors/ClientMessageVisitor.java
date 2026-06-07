package it.polimi.ingsw.visitors;

import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.service.PongMessage;

/**
 * Visitor for messages sent from the client to the server.
 * <p>
 * Extends {@link GameMessageVisitor} by adding visit methods for messages
 * related to session management (login, game creation/joining, lobbies,
 * rankings, disconnection) and service messages (pong).
 * </p>
 */
public interface ClientMessageVisitor extends GameMessageVisitor {

    /**
     * Visits a game creation message.
     *
     * @param createGameMessage the message containing the new game parameters
     */
    void visit(CreateGameMessage createGameMessage);

    /**
     * Visits a request to join an existing game.
     *
     * @param joinGameMessage the message containing the game access parameters
     */
    void visit(JoinGameMessage joinGameMessage);

    /**
     * Visits a login message.
     *
     * @param loginMessage the message containing the player's credentials
     */
    void visit(LoginMessage loginMessage);

    /**
     * Visits a request for the list of available lobbies.
     *
     * @param askLobbiesMessage the lobby request message
     */
    void visit(AskLobbiesMessage askLobbiesMessage);

    /**
     * Visits a pong message, used to reply to the server's ping
     * and keep the connection alive.
     *
     * @param pongMessage the pong message
     */
    void visit(PongMessage pongMessage);

    /**
     * Visits a message to abandon the current game.
     *
     * @param quitMessage the quit message
     */
    void visit(QuitMessage quitMessage);

    /**
     * Visits an application exit message.
     *
     * @param exitMessage the exit message
     */
    void visit(ExitMessage exitMessage);

    /**
     * Visits a global ranking request message.
     *
     * @param rankingRequestMessage the ranking request message
     */
    void visit(RankingRequestMessage rankingRequestMessage);

    /**
     * Sets the visitor for in-game messages, delegating the handling
     * of in-game actions to a dedicated {@link GameMessageVisitor}.
     *
     * @param visitor the visitor to use for in-game messages
     */
    void setGameVisitor(GameMessageVisitor visitor);
}