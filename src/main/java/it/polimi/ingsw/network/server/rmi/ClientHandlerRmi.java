package it.polimi.ingsw.network.server.rmi;


import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;


public class ClientHandlerRmi extends ClientHandler {

    private final String nickname;
    private final VirtualViewRmi clientStub;
    private ClientMessageVisitor visitor;
    private final DisconnectionListener disconnectionListener;
    private volatile boolean disconnected = false;

    private static final int PING_INTERVAL = 5000;
    private static final int PING_TIMEOUT = 3000;
    private static final int MAX_FAILURES = 3;

    public ClientHandlerRmi(String nickname, VirtualViewRmi clientStub, DisconnectionListener disconnectionListener) {
        this.nickname = nickname;
        this.clientStub = clientStub;
        this.visitor = null;
        this.disconnectionListener = disconnectionListener;
        startHealthCheck();
    }

    private void startHealthCheck() {
        Thread healthCheckThread = new Thread(() -> {
            int failureCount = 0;

            while(!disconnected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    try{
                        clientStub.ping();
                        failureCount = 0;
                        System.out.println("[HEALTH CHECK OK] " + nickname);

                    } catch (RemoteException e) {
                        failureCount++;
                        System.out.println("[HEALTH CHECK FALLITO] " + nickname
                                + " (tentativo " + failureCount + "/" + MAX_FAILURES + ")");
                        if (failureCount >= MAX_FAILURES) {
                            handleDisconnection();
                            this.disconnected = true;
                        }
                    }

                } catch (InterruptedException e) {
                    System.out.println("[HEALTH CHECK] Thread interrotto per " + nickname);
                    Thread.currentThread().interrupt();
                    this.disconnected = true;
                    break;
                }
            }
        }, "HealthCheck-" + nickname);

        healthCheckThread.setDaemon(true);
        healthCheckThread.start();
    }

    private void handleDisconnection(){
        disconnectionListener.handleDisconnection(nickname);
    }

    private void handleRemoteException(RemoteException e) {
        if (!disconnected) {
            disconnected = true;
            System.err.println("[RMI] Client " + nickname + " non raggiungibile: " + e.getMessage());
            disconnectionListener.handleDisconnection(nickname);
        }
    }

    private boolean isAvailable() {
        return !disconnected;
    }


    @Override
    public String getNickname() { return nickname; }

    @Override
    public void onClientMessage(ClientMessage m) {
        if (visitor != null) m.accept(visitor);
    }

    @Override
    public void setVisitor(ClientMessageVisitor visitor) {
        this.visitor = visitor;
    }


    @Override
    public void onErrorMessage(String errorMsg) {
        if(!isAvailable()) return;
        try{
            clientStub.printError(errorMsg);
        } catch (RemoteException e) {
            handleRemoteException(e);
        }
    }

    @Override
    public void onCurrPlayerUpdate(String nickname) {
        if (!isAvailable()) return;
        try { clientStub.onCurrPlayerUpdate(nickname); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) {
        if (!isAvailable()) return;
        try { clientStub.onRequestLeaderboard(ranks); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos) {
        if (!isAvailable()) return;
        try { clientStub.onGameEnding(stats, rankingPos); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onMoveUpdate(TileDTO tile, String nextPlayer) {
        if (!isAvailable()) return;
        try { clientStub.onMoveUpdate(tile, nextPlayer); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        if (!isAvailable()) return;
        try { clientStub.onReturnToQueue(tileDTO,playerStatsDTO); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        if (!isAvailable()) return;
        try { clientStub.onDrawUpdate(c, nickname); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        if (!isAvailable()) return;
        try { clientStub.onStatusUpdate(status); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) {
        if (!isAvailable()) return;
        try { clientStub.onStatsUpdate(stats, cardId); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void refresh(List<PlayerDTO> listPlayers, BoardDTO board) {
        if (!isAvailable()) return;
        try { clientStub.refresh(listPlayers, board); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        if (!isAvailable()) return;
        try { clientStub.onPhaseUpdate(phaseDTO); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void showBoard(BoardDTO board) {
        if (!isAvailable()) return;
        try { clientStub.showBoard(board); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void notifyDrawable(ActionsDTO actions) {
        if (!isAvailable()) return;
        try { clientStub.notifyDrawable(actions); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void notifySkip(String nickname) {
        if (!isAvailable()) return;
        try { clientStub.notifySkip(nickname); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onChangeAge(ChangeAgeDTO dto) {
        if (!isAvailable()) return;
        try { clientStub.onChangeAge(dto); }
        catch (RemoteException e) { handleRemoteException(e); }
    }

    @Override
    public void onEvent(String event) {
        if (!isAvailable()) return;
        try { clientStub.onEvent(event); }
        catch (RemoteException e) { handleRemoteException(e); }
    }
}