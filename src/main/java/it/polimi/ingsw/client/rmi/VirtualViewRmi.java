package it.polimi.ingsw.client.rmi;

import it.polimi.ingsw.network.dto.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface VirtualViewRmi extends Remote {

    void onCurrPlayerUpdate(String nickname) throws RemoteException;
    void onMoveUpdate(TileDTO tile, String nextPlayer) throws RemoteException;
    void onPhaseUpdate(PhaseDTO phaseDTO) throws RemoteException;
    void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) throws RemoteException;
    void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) throws RemoteException;

    void onDrawUpdate(CardDTO c, String nickname) throws RemoteException;

    void onStatusUpdate(PlayerStatusDTO status) throws RemoteException;
    void onStatsUpdate(PlayerStatsDTO stats, int cardId) throws RemoteException;
    void onChangeAge(ChangeAgeDTO dto) throws RemoteException;
    void refresh(List<PlayerDTO> listPlayers, BoardDTO board) throws RemoteException;
    void notifySkip(String nickname) throws RemoteException;
    void notifyDrawable(ActionsDTO actions) throws RemoteException;
    void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) throws RemoteException;
    void showBoard(BoardDTO boardDTO) throws RemoteException;
    void onQuitServer(String reason) throws RemoteException;
    void onEvent(EventDTO event) throws RemoteException;
    void printError(String e) throws RemoteException;

    void ping() throws RemoteException;

    void reconnect(int matchId) throws RemoteException;
}