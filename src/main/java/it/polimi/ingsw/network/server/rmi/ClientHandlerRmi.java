package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.messages.client.ClientMessage;
import it.polimi.ingsw.network.server.ClientHandler;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.visitors.ClientMessageVisitor;

import java.util.concurrent.*;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class ClientHandlerRmi extends ClientHandler {

    private final String nickname;
    private final VirtualViewRmi clientStub;
    private volatile ClientMessageVisitor visitor;
    private final DisconnectionListener disconnectionListener;
    private volatile boolean disconnected = false;

    private final ExecutorService messageSender = Executors.newSingleThreadExecutor();
    private final ExecutorService pingExecutor = Executors.newCachedThreadPool();

    private static final int PING_INTERVAL = 2000;
    private static final int PING_TIMEOUT = 3000;
    private static final int MAX_FAILURES = 2;

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

            while (!disconnected) {
                try {
                    Thread.sleep(PING_INTERVAL);
                    Future<Void> future = pingExecutor.submit(() -> {
                        clientStub.ping();
                        return null;
                    });

                    try {
                        future.get(PING_TIMEOUT, TimeUnit.MILLISECONDS);
                        failureCount = 0;
                        System.out.println("[HEALTH CHECK OK] " + nickname);

                    } catch (TimeoutException e) {
                        future.cancel(true);
                        failureCount++;
                        System.out.println("[HEALTH CHECK FALLITO] " + nickname
                                + " (tentativo " + failureCount + "/" + MAX_FAILURES + ")");
                    } catch (InterruptedException e) {
                        System.out.println("[HEALTH CHECK] Thread interrotto per " + nickname);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (ExecutionException e) {
                        failureCount++;
                        System.out.println("[HEALTH CHECK FALLITO] " + nickname
                                + " (tentativo " + failureCount + "/" + MAX_FAILURES + ")");
                    }

                    if (failureCount >= MAX_FAILURES) {
                        synchronized (ClientHandlerRmi.this) {
                            if (disconnected) break;
                            disconnected = true;
                            pingExecutor.shutdownNow();
                            messageSender.shutdownNow();
                        }
                        handleDisconnection();
                        break;
                    }

                } catch (InterruptedException e) {
                    System.out.println("[HEALTH CHECK] Thread interrotto per " + nickname);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            pingExecutor.shutdownNow();
        }, "HealthCheck-" + nickname);

        healthCheckThread.setDaemon(true);
        healthCheckThread.start();
    }
    private void handleDisconnection(){
        disconnectionListener.handleDisconnection(nickname);
    }

    private void handleRemoteException(RemoteException e) {
        if (disconnected) return;
        synchronized (this) {
            if (disconnected) return;
            disconnected = true;
            pingExecutor.shutdownNow();
            messageSender.shutdownNow();
        }
        System.err.println("[RMI] Client " + nickname + " non raggiungibile: " + e.getMessage());
        disconnectionListener.handleDisconnection(nickname);
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
        messageSender.submit(() -> {
            try { clientStub.printError(errorMsg); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onReconnection(int matchId){
        if(!isAvailable()) return;
        messageSender.submit(() -> {
            try{clientStub.reconnect(matchId);}
            catch(RemoteException e){ handleRemoteException(e);}
        });
    }

    @Override
    public void onCurrPlayerUpdate(String nickname) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onCurrPlayerUpdate(nickname); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onRequestLeaderboard(Map<PlayerDTO, Integer> ranks) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onRequestLeaderboard(ranks); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onGameEnding(List<PlayerStatsDTO> stats, int rankingPos, int globalRankingPos) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onGameEnding(stats, rankingPos, globalRankingPos); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onMoveUpdate(TileDTO tile, String nextPlayer) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onMoveUpdate(tile, nextPlayer); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onReturnToQueue(TileDTO tileDTO, PlayerStatsDTO playerStatsDTO) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onReturnToQueue(tileDTO, playerStatsDTO); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onDrawUpdate(CardDTO c, String nickname) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onDrawUpdate(c, nickname); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onStatusUpdate(PlayerStatusDTO status) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onStatusUpdate(status); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onStatsUpdate(PlayerStatsDTO stats, int cardId) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onStatsUpdate(stats, cardId); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void refresh(List<PlayerDTO> listPlayers, BoardDTO board) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.refresh(listPlayers, board); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onPhaseUpdate(PhaseDTO phaseDTO) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onPhaseUpdate(phaseDTO); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void showBoard(BoardDTO board) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.showBoard(board); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void notifyDrawable(ActionsDTO actions) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.notifyDrawable(actions); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void notifySkip(String nickname) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.notifySkip(nickname); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onChangeAge(ChangeAgeDTO dto) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onChangeAge(dto); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onEvent(EventDTO events) {
        if (!isAvailable()) return;
        messageSender.submit(() -> {
            try { clientStub.onEvent(events); }
            catch (RemoteException e) { handleRemoteException(e); }
        });
    }

    @Override
    public void onQuitServer(String reason){
        if(!isAvailable())
            return;
        messageSender.submit(() -> {
            try{
                clientStub.onQuitServer(reason);
            }catch (RemoteException e) { handleRemoteException(e); }
        });
    }

}