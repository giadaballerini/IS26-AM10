package it.polimi.ingsw.client;

import it.polimi.ingsw.network.dto.*;

import java.util.List;
import java.util.Map;

public interface UserInterface {
    void showBoard();
    void onMoveUpdate(TileDTO tile, String currPlayer);
    void onCurrPlayerUpdate(String nickname);
    void onPhaseUpdate(PhaseDTO phaseDTO);
    void onDrawUpdate(CardDTO c, String nickname);
    void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO);
    void onChangeAge(int age);
    void onStatsUpdate(PlayerStatsDTO playerStatsDTO);
    void onStatusUpdate(PlayerStatusDTO playerStatusDTO);
    void displayLobbies(Map<Integer, List<LobbyDTO>> lobbies);
    void printError(Exception e);
    void onLogin(String nickname);
    void onCreate(int id);
    void onJoin(int id);

    void showLeaderboard(Map<PlayerDTO, Integer> ranks);

    void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos);

    void showDrawable();

    void start();

    void notifySkip(String nickname);

    void onEvent(String event);

    VirtualModel quit();

    void exit();

    void displayHelpMessage();

    void info(int cardId);
}
