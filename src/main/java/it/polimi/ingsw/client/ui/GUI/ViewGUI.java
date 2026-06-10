package it.polimi.ingsw.client.ui.GUI;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.VirtualModel;
import it.polimi.ingsw.client.ui.UserInterface;
import it.polimi.ingsw.network.dto.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

/**
 * JavaFX implementation of {@link UserInterface} that bridges incoming server
 * notifications with the GUI layer.
 *
 * <p>{@code ViewGUI} acts as the single entry point through which the
 * {@link it.polimi.ingsw.client.Client} pushes game-state updates to the
 * interface. Every method delegates to the appropriate JavaFX scene controller
 * ({@link GameController}, {@link MainMenuController}, or
 * {@link LeaderboardController}) by dispatching work onto the JavaFX
 * Application Thread via {@link Platform#runLater(Runnable)}. Controller
 * references are {@code volatile} so that assignments made on the FX thread
 * are immediately visible to the network thread that calls these methods.</p>
 *
 * <p>The singleton field {@link #gui} gives other GUI components quick access
 * to the active instance without dependency injection.</p>
 *
 * <p>Methods that are meaningful only in the TUI ({@link #start()},
 * {@link #displayHelpMessage()}, {@link #info(int)}, {@link #showStatusScreen()},
 * {@link #showCompletedDraw()}) are intentionally left as no-ops.</p>
 */
public class ViewGUI implements UserInterface {

    /** Globally accessible reference to the currently active {@code ViewGUI} instance. */
    public static ViewGUI gui;

    private Client client;
    private VirtualModel model;

    /** Controller for the in-game scene; {@code null} when no game is in progress. */
    private volatile GameController gameController;

    /** Controller for the main menu / lobby scene. */
    private volatile MainMenuController menuController;

    /** Controller for the global leaderboard scene. */
    private volatile LeaderboardController leaderboardController;

    /**
     * Flag set to {@code true} when {@link #showBoard()} is called before
     * {@link #gameController} has been assigned. The refresh is replayed
     * as soon as the controller is injected via {@link #setGameController(GameController)}.
     */
    private volatile boolean pendingBoardRefresh = false;

    /**
     * Constructs a {@code ViewGUI} with a pre-existing {@link VirtualModel}.
     *
     * @param model the client-side model holding the local game state
     */
    public ViewGUI(VirtualModel model) {
        this.model = model;
        gui = this;
    }

    /**
     * Constructs a {@code ViewGUI} without an initial model.
     * The model must be supplied later via {@link #setModel(VirtualModel)}.
     */
    public ViewGUI() {
        gui = this;
    }

    /**
     * Injects the {@link MainMenuController} created by the JavaFX FXML loader
     * for the main-menu / lobby scene.
     *
     * @param menuController the controller to register; may be {@code null} to
     *                       clear the reference when the scene is torn down
     */
    public void setMenucontroller(MainMenuController menuController) {
        this.menuController = menuController;
    }

    /**
     * Injects the {@link GameController} created by the JavaFX FXML loader for
     * the in-game scene.
     *
     * <p>If a board refresh was requested before the controller was available
     * (i.e. {@link #pendingBoardRefresh} is {@code true}), the refresh is
     * immediately replayed on the FX thread.</p>
     *
     * @param gameController the controller to register; pass {@code null} when
     *                       the game scene is unloaded
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        if (gameController != null) {
            if (pendingBoardRefresh) {
                pendingBoardRefresh = false;
                Platform.runLater(gameController::refreshBoard);
            }
        }
    }

    /**
     * Injects the {@link LeaderboardController} for the global-ranking scene.
     *
     * @param leaderboardController the controller to register
     */
    public void setLeaderboardController(LeaderboardController leaderboardController) {
        this.leaderboardController = leaderboardController;
    }

    /**
     * Requests a full board refresh in the game scene.
     *
     * <p>If {@link #gameController} is not yet available (e.g. the scene is
     * still loading), the request is deferred via {@link #pendingBoardRefresh}
     * and replayed once the controller is injected.</p>
     */
    @Override
    public void showBoard() {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) {
                gc.refreshBoard();
            } else {
                pendingBoardRefresh = true;
            }
        });
    }

    /**
     * Notifies the game scene that a player has moved their pawn to a new tile.
     *
     * @param tile       the {@link TileDTO} representing the destination tile,
     *                   including occupancy, draw slots, and food amount
     * @param currPlayer the nickname of the player who just moved
     */
    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onMoveUpdate(tile, currPlayer);
        });
    }

    /**
     * Notifies the game scene that the active turn has passed to a new player.
     *
     * @param nickname the nickname of the player whose turn it now is
     */
    @Override
    public void onCurrPlayerUpdate(String nickname) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onCurrPlayerUpdate(nickname);
        });
    }

    /**
     * Notifies the game scene that the game phase has changed.
     *
     * <p>Phases are defined by {@link it.polimi.ingsw.enumerations.GamePhaseEnum}
     * and drive which actions are available to the player (move, draw, skip, etc.).</p>
     *
     * @param phaseDTO a {@link PhaseDTO} wrapping the new {@code GamePhaseEnum} value
     */
    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onPhaseUpdate(phaseDTO);
        });
    }

    /**
     * Notifies the game scene that a card has been drawn.
     *
     * <p>If the drawing player is the local player (matched via
     * {@link VirtualModel#getNickname()}), the card is inserted into the
     * local village via {@link GameController#insertCard(int)}.
     *
     * @param card     the {@link CardDTO} that was drawn
     * @param nickname the nickname of the player who drew the card
     */
    @Override
    public void onDrawUpdate(CardDTO card, String nickname) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc == null) return;
            if (nickname.equals(model.getNickname())) {
                gc.insertCard(card.getId());
            } else {
                gc.onOpponentDraw(card, nickname);
            }
        });
    }

    /**
     * Notifies the game scene that a player has returned to the waiting queue
     * after occupying a tile and receiving their stats update.
     *
     * @param tileDTO        the tile the player vacated
     * @param playerStatsDTO the updated stats for that player
     */
    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) {
                gc.onReturnToQueue(tileDTO, playerStatsDTO);
            }
        });
    }

    /**
     * Notifies the game scene that the game has advanced to a new age.
     *
     * @param age the new age number (1-indexed)
     */
    @Override
    public void onChangeAge(int age) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) {
                gc.onChangeAge(age);
            }
        });
    }

    /**
     * Notifies the game scene that a single player's stats have changed
     * (e.g. food, prestige points, or stars after an action).
     *
     * @param playerStatsDTO the updated stats for the affected player
     */
    @Override
    public void onStatsUpdate(PlayerStatsDTO playerStatsDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.updateStats(List.of(playerStatsDTO));
        });
    }

    /**
     * Notifies the game scene that a player's status has changed
     * (e.g. connection state or in-queue flag).
     *
     * @param playerStatusDTO the updated status for the affected player
     */
    @Override
    public void onStatusUpdate(PlayerStatusDTO playerStatusDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onStatusUpdate(playerStatusDTO);
        });
    }

    /**
     * Instructs the game scene to reveal the drawable cards so the local
     * player can choose which card to draw during the draw phase.
     */
    @Override
    public void showDrawable() {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.showDrawable();
        });
    }

    /**
     * Notifies the game scene that a player has skipped their draw action.
     *
     * @param nickname the nickname of the player who skipped
     */
    @Override
    public void notifySkip(String nickname) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.notifySkip(nickname);
        });
    }

    /**
     * Notifies the game scene that one or more event cards have been resolved.
     *
     * <p>{@link EventDTO} carries both the list of triggered event cards and the
     * resulting per-player stats, while {@code statsBefore} provides a snapshot
     * of stats prior to the event so the UI can animate the delta.</p>
     *
     * @param events      the {@link EventDTO} containing triggered cards and
     *                    post-event player stats
     * @param statsBefore the list of {@link PlayerStatsDTO} captured immediately
     *                    before the events were applied
     */
    @Override
    public void onEvent(EventDTO events, List<PlayerStatsDTO> statsBefore) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onEvent(events, statsBefore);
        });
    }

    /**
     * Triggers the end-game screen with the final rankings.
     *
     * <p>If the event banner is currently being displayed (e.g. an event
     * animation is still running), the call is retried after a 500 ms
     * {@link PauseTransition} to avoid overlapping UI transitions.</p>
     *
     * @param stats           final {@link PlayerStatsDTO} list for all players
     * @param rankingPos      this player's position in the match-specific ranking
     * @param globalRankingPos this player's position in the global leaderboard
     */
    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        Platform.runLater(() -> {
            if (gameController == null) return;
            if (gameController.isEventBannerShowing()) {
                PauseTransition retry = new PauseTransition(Duration.millis(500));
                retry.setOnFinished(e -> onGameEnding(stats, rankingPos, globalRankingPos));
                retry.play();
            } else {
                gameController.onGameEnding(stats, rankingPos, globalRankingPos);
            }
        });
    }

    /**
     * Displays the in-game leaderboard overlay with the current global rankings.
     *
     * @param ranks a map from {@link PlayerDTO} to their global ranking position
     */
    @Override
    public void showLeaderboard(Map<PlayerDTO, Integer> ranks) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.showLeaderboard(ranks);
        });
    }

    /**
     * Updates the lobby list displayed in the main menu.
     *
     * @param lobbies a map from game ID to the list of {@link LobbyDTO} entries
     *                for that game
     */
    @Override
    public void displayLobbies(Map<Integer, List<LobbyDTO>> lobbies) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.updateLobbies(lobbies);
        });
    }

    /**
     * Displays an error message in the main menu scene.
     *
     * <p>Typically called when the server rejects an action (e.g. a failed
     * login or an invalid join attempt).</p>
     *
     * @param e the exception whose message will be shown to the user
     */
    @Override
    public void printError(Exception e) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.showError(e.getMessage());
        });
    }

    /**
     * Notifies the main menu that the login handshake completed successfully.
     *
     * @param nickname the nickname the server confirmed for this client
     */
    @Override
    public void onLogin(String nickname) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onLoginSuccess(nickname);
        });
    }

    /**
     * Notifies the main menu that a new game lobby was created successfully.
     *
     * @param id the server-assigned ID of the newly created game
     */
    @Override
    public void onCreate(int id) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onCreateSuccess(id);
        });
    }

    /**
     * Notifies the main menu that the client successfully joined an existing lobby.
     *
     * @param id the ID of the game that was joined
     */
    @Override
    public void onJoin(int id) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onJoinSuccess(id);
        });
    }

    /**
     * Handles a voluntary or server-initiated quit from the current game,
     * returning the player to the main menu.
     *
     * <p>The {@link #gameController} reference is cleared before delegating
     * to {@link MainMenuController#returnToLobby(String)} to ensure no
     * further game-scene updates are dispatched.</p>
     *
     * @param reason a human-readable explanation of why the player was removed
     *               from the game (e.g. kicked, game dissolved)
     */
    @Override
    public void onQuit(String reason) {
        Platform.runLater(() -> {
            gameController = null;
            MainMenuController mc = menuController;
            if (mc != null) {
                mc.returnToLobby(reason);
            }
        });
    }

    /**
     * Handles an unexpected server crash by clearing the game state and
     * prompting the player to reconnect from the main menu.
     */
    @Override
    public void onServerCrash() {
        Platform.runLater(() -> {
            gameController = null;
            MainMenuController mc = menuController;
            if (mc != null)
                mc.returnToMenu("Il server è crashato, riconnettersi per riprendere la partita.");
        });
    }

    /**
     * Tears down the GUI's game state and resets the {@link VirtualModel}.
     *
     * <p>Called by the RMI client during its teardown sequence. No UI work is
     * needed here; the model is managed by the caller. The method returns the
     * model so the caller can re-use or discard it.</p>
     *
     * @return the current {@link VirtualModel}, after {@link VirtualModel#reset()}
     *         has been called; may be {@code null} if none was set
     */
    @Override
    public VirtualModel quit() {
        if (model != null)
            model.reset();
        gameController = null;
        pendingBoardRefresh = false;
        return model;
    }

    /**
     * Shuts down the JavaFX platform, closing the application window.
     */
    @Override
    public void exit() {
        Platform.runLater(Platform::exit);
    }

    /**
     * Notifies the main menu that a reconnection to an ongoing match succeeded.
     *
     * @param matchId the ID of the match the client has reconnected to
     */
    @Override
    public void reconnect(int matchId) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onReconnectSuccess();
        });
    }

    /**
     * Instructs the game scene to display the reconnection-waiting overlay
     * while the server restores the match state.
     *
     * <p>Unlike most methods in this class, this is called directly on the
     * calling thread without {@code Platform.runLater}, so callers must ensure
     * they are already on the FX thread or that the operation is thread-safe.</p>
     */
    public void reconnectWaiting() {
        GameController gc = gameController;
        if (gc != null) gc.showReconnectionWaiting();
    }

    /**
     * Passes the global ranking map to the {@link LeaderboardController} for display.
     *
     * @param stringIntegerMap a map from player nickname to their global ranking position
     */
    @Override
    public void showRanking(Map<String, Integer> stringIntegerMap) {
        Platform.runLater(() -> {
            LeaderboardController lc = leaderboardController;
            if (lc != null) lc.showGlobalRanking(stringIntegerMap);
        });
    }

    /** No-op in the GUI — implemented only by the TUI. */
    @Override public void start() {}

    /** No-op in the GUI — implemented only by the TUI. */
    @Override public void displayHelpMessage() {}

    /** No-op in the GUI — implemented only by the TUI. */
    @Override public void info(int cardId) {}

    /** No-op in the GUI — implemented only by the TUI. */
    @Override
    public void showStatusScreen() {}

    /** No-op in the GUI — implemented only by the TUI. */
    public void showCompletedDraw() {}

    /**
     * Injects the {@link Client} instance responsible for sending messages to
     * the server (used by command handlers that need to trigger network calls).
     *
     * @param client the active {@link Client}
     */
    @Override
    public void setClient(Client client) { this.client = client; }

    /**
     * Returns the active {@link Client}.
     *
     * @return the client, or {@code null} if not yet set
     */
    public Client getClient() { return client; }

    /**
     * Returns the local {@link VirtualModel} holding the client-side game state.
     *
     * @return the current model
     */
    public VirtualModel getModel() { return model; }

    /**
     * Returns the local player's nickname as stored in the {@link VirtualModel}.
     *
     * @return the nickname string
     */
    public String getNickname() { return model.getNickname(); }

    /**
     * Replaces the current {@link VirtualModel} (e.g. after a reconnection
     * where a fresh model is built from the server's snapshot).
     *
     * @param model the new model to adopt
     */
    public void setModel(VirtualModel model) { this.model = model; }
}