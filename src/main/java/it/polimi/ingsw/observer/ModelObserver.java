package it.polimi.ingsw.observer;

import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.visitors.ClientMessageVisitor;
import it.polimi.ingsw.visitors.GameMessageVisitor;

import java.util.List;
import java.util.Map;

public interface ModelObserver {

    String getNickname();

    void onClientMessage(ClientMessage m);

    void setVisitor(ClientMessageVisitor visitor);

    void onErrorMessage(String errorMsg);

    void onCurrPlayerUpdate(String nickname);

    void onMoveUpdate(TileDTO tile, String nextPlayer);

    void onPhaseUpdate(PhaseDTO phaseDTO);

    void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks);

    void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos);

    void onDrawUpdate(CardDTO c, String nickname);

    void onStatusUpdate(PlayerStatusDTO status);

    void onStatsUpdate(PlayerStatsDTO stats, int cardId);

    void refresh(List<PlayerDTO> listPlayers, BoardDTO board);

    void notifySkip(String nickname);

    void notifyDrawable(ActionsDTO actions);

    void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO);

    void showBoard(BoardDTO boardDTO);

    void onChangeAge(ChangeAgeDTO dto);

    void onEvent(EventDTO event);

    void onQuitServer(String reason);

    void onReconnection(int matchId);

    default void injectGameVisitor(GameMessageVisitor visitor){}


    default void resetGameVisitor(){}
}