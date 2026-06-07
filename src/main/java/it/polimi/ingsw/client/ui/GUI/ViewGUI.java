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


public class ViewGUI implements UserInterface {

    public static ViewGUI gui;

    private Client client;
    private VirtualModel model;

    private volatile GameController gameController;
    private volatile MainMenuController menuController;
    private volatile LeaderboardController leaderboardController;
    private volatile boolean pendingBoardRefresh = false;

    public ViewGUI(VirtualModel model) {
        this.model = model;
        gui = this;
    }

    public ViewGUI() {
        gui = this;
    }


    public void setMenucontroller(MainMenuController menuController) {
        this.menuController = menuController;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        if (gameController != null) {
            if (pendingBoardRefresh) {
                pendingBoardRefresh = false;
                Platform.runLater(gameController::refreshBoard);
            }
        }
    }

    public void setLeaderboardController(LeaderboardController leaderboardController) {this.leaderboardController = leaderboardController;}



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

    @Override
    public void onMoveUpdate(TileDTO tile, String currPlayer) {

        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onMoveUpdate(tile, currPlayer);
        });
    }

    @Override
    public void onCurrPlayerUpdate(String nickname) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onCurrPlayerUpdate(nickname);
        });
    }

    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onPhaseUpdate(phaseDTO);
        });
    }


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

    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null){
                gc.onReturnToQueue(tileDTO, playerStatsDTO);
            }
        });
    }

    @Override
    public void onChangeAge(int age) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) {
                gc.onChangeAge(age);
            }
        });
    }

    @Override
    public void onStatsUpdate(PlayerStatsDTO playerStatsDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.updateStats(List.of(playerStatsDTO));
        });
    }

    @Override
    public void onStatusUpdate(PlayerStatusDTO playerStatusDTO) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onStatusUpdate(playerStatusDTO);
        });
    }

    @Override
    public void showDrawable() {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.showDrawable();
        });
    }

    @Override
    public void notifySkip(String nickname) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.notifySkip(nickname);
        });
    }

    @Override
    public void onEvent(EventDTO events, List<PlayerStatsDTO> statsBefore) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.onEvent(events, statsBefore);
        });
    }


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

    @Override
    public void showLeaderboard(Map<PlayerDTO, Integer> ranks) {
        Platform.runLater(() -> {
            GameController gc = gameController;
            if (gc != null) gc.showLeaderboard(ranks);
        });
    }


    @Override
    public void displayLobbies(Map<Integer, List<LobbyDTO>> lobbies) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.updateLobbies(lobbies);
        });
    }

    @Override
    public void printError(Exception e) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.showError(e.getMessage());
        });
    }


    @Override
    public void onLogin(String nickname) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onLoginSuccess(nickname);
        });
    }

    @Override
    public void onCreate(int id) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onCreateSuccess(id);
        });
    }

    @Override
    public void onJoin(int id) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onJoinSuccess(id);
        });
    }


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

    @Override
    public void onServerCrash() {
        Platform.runLater(() -> {
            gameController = null;
            MainMenuController mc = menuController;
            if(mc != null)
                mc.returnToMenu("Il server è crashato, riconnettersi per riprendere la partita.");
        });
    }

    /**
     * Chiamato da ClientRmi durante il teardown.
     * Nessun lavoro UI necessario qui; il model viene gestito da ClientRmi.
     */
    @Override
    public VirtualModel quit() {
        if (model != null)
            model.reset();
        gameController = null;
        pendingBoardRefresh = false;
        return model;
    }

    @Override
    public void exit() {
        Platform.runLater(Platform::exit);
    }

    @Override
    public void reconnect(int matchId) {
        Platform.runLater(() -> {
            MainMenuController mc = menuController;
            if (mc != null) mc.onReconnectSuccess();
        });
    }

    public void reconnectWaiting() {
        GameController gc = gameController;
        if (gc != null) gc.showReconnectionWaiting();
    }

    @Override
    public void showRanking(Map<String, Integer> stringIntegerMap) {
        Platform.runLater(() -> {
            LeaderboardController lc = leaderboardController;
            if(lc != null) lc.showGlobalRanking(stringIntegerMap);
        });
    }

    @Override public void start() {
        // per tui
    }
    @Override public void displayHelpMessage(){
        // per tui
    }
    @Override public void info(int cardId){
        // per tui
    }

    @Override
    public void showStatusScreen() {
        //per tui
    }

    public void showCompletedDraw(){
        //per tui
    }

    @Override
    public void setClient(Client client) { this.client = client; }

    public Client getClient() { return client; }
    public VirtualModel getModel() { return model; }
    public String getNickname() { return model.getNickname(); }
    public void setModel(VirtualModel model) { this.model = model; }
}