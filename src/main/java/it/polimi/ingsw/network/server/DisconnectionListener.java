package it.polimi.ingsw.network.server;

import it.polimi.ingsw.server.MatchManager;

/**
 * Callback interface used to notify the server when a client can no longer
 * be reached, regardless of the underlying transport (RMI or socket).
 *
 * <p>Implemented by {@link it.polimi.ingsw.network.server.rmi.ServerRmi} and
 * by {@link it.polimi.ingsw.network.server.socket.ClientHandlerSocket}, both
 * of which delegate to
 * {@link MatchManager#disconnect(String)}.
 *
 * <p>Disconnection handling is transport-agnostic: the same {@link MatchManager} method is invoked whether
 * the dead connection was RMI or socket.
 */
public interface DisconnectionListener {

    /**
     * Called when the client identified by {@code nickname} has disconnected
     * or become unreachable.
     *
     * <p>Implementations must not throw checked exceptions.
     *
     * @param nickname the nickname of the disconnected player
     */
    void handleDisconnection(String nickname);
}