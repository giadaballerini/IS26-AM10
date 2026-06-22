package it.polimi.ingsw.visitors;

import it.polimi.ingsw.client.socket.VirtualView;
import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.InvalidLobbyException;
import it.polimi.ingsw.exceptions.InvalidTimingException;
import it.polimi.ingsw.exceptions.InvalidUsernameException;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.messages.client.*;
import it.polimi.ingsw.network.messages.service.PongMessage;
import it.polimi.ingsw.server.MatchManager;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Concrete implementation of {@link ClientMessageVisitor}.
 * <p>
 * Handles all messages received from a client by delegating game-related
 * actions to a {@link GameMessageVisitor} and session-related actions
 * (login, lobby, ranking) directly to the {@link MatchManager}.
 * </p>
 */
public class ClientMessageVisitorImpl implements ClientMessageVisitor {

    /**
     *  Logger for this class.
     */
    private static final Logger LOG = java.util.logging.Logger.getLogger(ClientMessageVisitorImpl.class.getName());

    /** Manages match lifecycle: login, game creation, joining, and ranking. */
    private final MatchManager matchManager;

    /** The virtual view associated with the connected client. */
    private final VirtualView clientHandler;

    /**
     * The visitor responsible for handling in-game messages (move, draw, skip).
     * Volatile to ensure visibility across threads.
     */
    private volatile GameMessageVisitor gameVisitor;

    /**
     * Constructs a new {@code ClientMessageVisitorImpl}.
     *
     * @param matchManager  the match manager handling server-side game logic
     * @param clientHandler the virtual view of the client sending the messages
     */
    public ClientMessageVisitorImpl(MatchManager matchManager, VirtualView clientHandler) {
        this.matchManager = matchManager;
        this.clientHandler = clientHandler;
    }

    /**
     * Sets the in-game visitor to delegate game-related messages to.
     *
     * @param gameVisitor the visitor handling in-game actions
     */
    public void setGameVisitor(GameMessageVisitor gameVisitor) {
        this.gameVisitor = gameVisitor;
    }

    /**
     * Handles a move message by delegating to {@link MatchManager#move(String, int)},
     * which dispatches the move to the game controller and persists the
     * resulting state. Throws {@link InvalidTimingException} if the command
     * is issued at an invalid point in the game flow.
     *
     * @param moveMessage the move message sent by the client
     */
    @Override
    public void visit(MoveMessage moveMessage) {
        try {
            if (gameVisitor == null)
                throw new InvalidTimingException("Non puoi usare questo comando al momento");
            matchManager.move(moveMessage.getPlayer(), moveMessage.getTilePos());
        } catch (GameException | InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles a draw message by delegating to {@link MatchManager#drawCard(int, String)},
     * which dispatches the draw to the game controller and persists the
     * resulting state. Throws {@link InvalidTimingException} if the command
     * is issued at an invalid point in the game flow.
     *
     * @param drawMessage the draw message sent by the client
     */
    @Override
    public void visit(DrawMessage drawMessage) {
        try {
            if (gameVisitor == null)
                throw new InvalidTimingException("Non puoi usare questo comando al momento");
            matchManager.drawCard(drawMessage.getCardId(), drawMessage.getNickname());
        } catch (GameException | InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles a skip message by delegating to {@link MatchManager#skip(String)},
     * which dispatches the skip to the game controller and persists the
     * resulting state. Throws {@link InvalidTimingException} if the command
     * is issued at an invalid point in the game flow.
     *
     * @param skipMessage the skip message sent by the client
     */
    @Override
    public void visit(SkipMessage skipMessage) {
        try {
            if (gameVisitor == null)
                throw new InvalidTimingException("Non puoi usare questo comando al momento");
            matchManager.skip(skipMessage.getNickname());
        } catch (GameException | InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }
    /**
     * Handles a game creation request, notifying the client of the assigned game ID.
     *
     * @param createGameMessage the message containing the desired number of players
     */
    @Override
    public void visit(CreateGameMessage createGameMessage) {
        try {
            int gameId = matchManager.createGame(clientHandler.getNickname(), createGameMessage.getNumPlayers());
            clientHandler.onGameCreated(gameId);
        } catch (InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles a request to join an existing game.
     *
     * @param joinGameMessage the message containing the target game ID
     */
    @Override
    public void visit(JoinGameMessage joinGameMessage) {
        try {
            matchManager.joinGame(clientHandler.getNickname(), joinGameMessage.getId());
            clientHandler.onJoinGame(joinGameMessage.getId());
        } catch (InvalidTimingException | InvalidLobbyException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles a login request, registering the player and notifying success or failure.
     *
     * @param loginMessage the message containing the player's chosen nickname
     */
    @Override
    public void visit(LoginMessage loginMessage) {
        String nickname = loginMessage.getNickname();
        try {
            matchManager.login(nickname, this.clientHandler);
            clientHandler.onLogin(nickname);
            clientHandler.onLoginSuccess(nickname);
        } catch (InvalidUsernameException e) {
            clientHandler.onLoginFailed(e.getMessage());
        }
    }

    /**
     * Handles a request for the list of available lobbies and sends it to the client.
     *
     * @param askLobbiesMessage the lobby list request message
     */
    @Override
    public void visit(AskLobbiesMessage askLobbiesMessage) {
        try {
            Map<Integer, List<LobbyDTO>> lobbies = matchManager.getLobbies(clientHandler.getNickname());
            clientHandler.onLobbiesRequested(lobbies);
        } catch (InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles a pong message received from the client, confirming the connection is alive.
     *
     * @param pongMessage the pong response message
     */
    @Override
    public void visit(PongMessage pongMessage) {
        LOG.fine("[PONG] Received from " + clientHandler.getNickname());
    }

    /**
     * Handles a quit request, removing the player from the current game.
     *
     * @param quitMessage the quit message sent by the client
     */
    @Override
    public void visit(QuitMessage quitMessage) {
        try {
            matchManager.quit(clientHandler.getNickname());
        } catch (InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles an exit message, triggering the disconnection procedure for the client.
     *
     * @param exitMessage the exit message sent by the client
     */
    @Override
    public void visit(ExitMessage exitMessage) {
        clientHandler.handleDisconnection(clientHandler.getNickname());
    }

    /**
     * Handles a ranking request, retrieving the global ranking and sending it to the client.
     *
     * @param rankingRequestMessage the ranking request message
     */
    @Override
    public void visit(RankingRequestMessage rankingRequestMessage) {
        try {
            Map<String, Integer> ranking = matchManager.requestRanking(clientHandler.getNickname());
            clientHandler.onRankingResponse(ranking);
        } catch (InvalidTimingException e) {
            clientHandler.onErrorMessage(e.getMessage());
        }
    }
}