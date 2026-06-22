package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.client.rmi.ClientRmi;
import it.polimi.ingsw.client.rmi.VirtualViewRmi;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.dto.LobbyDTO;
import it.polimi.ingsw.network.server.DisconnectionListener;
import it.polimi.ingsw.network.server.VirtualServerRmi;
import it.polimi.ingsw.server.MatchManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

/**
 * RMI server stub registered in the RMI registry under the name
 * {@code "GameServer"}.
 *
 * <p>Acts as the thin RMI transport layer between a remote {@link ClientRmi}
 * and the shared {@link MatchManager}. Each incoming RMI call is validated at
 * the transport level (exceptions declared by the interface) and then
 * forwarded directly to the match manager.
 *
 * <p>Also implements {@link DisconnectionListener} so that
 * {@link ClientHandlerRmi} can notify this object when a client's health check
 * fails; the disconnection is then propagated to {@link MatchManager}.
 */
public class ServerRmi extends UnicastRemoteObject
        implements VirtualServerRmi, DisconnectionListener {

    /** The shared match coordinator that all server-side handlers route through. */
    private final MatchManager matchManager;

    /**
     * Creates and exports a new {@code ServerRmi} bound to the given
     * {@link MatchManager}.
     *
     * @param matchManager the shared match coordinator
     * @throws RemoteException if the RMI export fails
     */
    public ServerRmi(MatchManager matchManager) throws RemoteException {
        super();
        this.matchManager = matchManager;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates a {@link ClientHandlerRmi} for the new client, registers it
     * with the match manager, and passes {@code this} as the
     * {@link DisconnectionListener} so that health check failures are
     * propagated back here.
     */
    @Override
    public void login(String nickname, VirtualViewRmi clientStub)
            throws RemoteException, InvalidUsernameException {
        ClientHandlerRmi rmiHandler = new ClientHandlerRmi(nickname, clientStub, this);
        matchManager.login(nickname, rmiHandler);
        rmiHandler.startHealthCheck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createGame(String nickname, int numPlayers)
            throws RemoteException, InvalidTimingException {
        return matchManager.createGame(nickname, numPlayers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void joinGame(String nickname, int id)
            throws RemoteException, InvalidLobbyException, InvalidTimingException {
        matchManager.joinGame(nickname, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String nickname, int tileId)
            throws RemoteException, InvalidMoveException, OccupiedTileException, InvalidPhaseException,
            InvalidPlayerException, InvalidTimingException {
        matchManager.move(nickname, tileId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(int id, String nickname)
            throws RemoteException, InvalidDrawException, InvalidTimingException {
        matchManager.drawCard(id, nickname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skip(String nickname)
            throws RemoteException, InvalidSkipException, InvalidTimingException {
        matchManager.skip(nickname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void quit(String nickname) throws RemoteException, InvalidTimingException {
        matchManager.quit(nickname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<LobbyDTO>> getLobbies(String nickname)
            throws RemoteException, InvalidTimingException {
        return matchManager.getLobbies(nickname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Integer> requestRanking(String nickname)
            throws RemoteException, InvalidTimingException {
        return matchManager.requestRanking(nickname);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link MatchManager#disconnect(String)}. Exceptions are
     * intentionally swallowed here because disconnection callbacks must not
     * throw checked exceptions.
     */
    @Override
    public void handleDisconnection(String nickname) {
        matchManager.disconnect(nickname);
    }

    /**
     * No-op server ping target. Called by the client-side health check thread
     * to verify that the server stub is reachable.
     */
    @Override
    public void ping() { }
}