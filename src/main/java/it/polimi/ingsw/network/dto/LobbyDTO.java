package it.polimi.ingsw.network.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a game lobby waiting for players to join.
 *
 * <p>Sent by the server in response to a lobby request so that the client
 * can display the available matches and let the player choose one to join.
 */
public class LobbyDTO implements Serializable {
    /**
     *  Required by the {@link Serializable} interface.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier of this lobby. */
    private final int id;

    /** Number of players this lobby can accommodate. */
    private final int capacity;

    /** Nicknames of the players who have already joined. */
    private final List<String> nicknames;

    /**
     * Creates a {@code LobbyDTO} with the given attributes.
     *
     * @param id        unique lobby identifier
     * @param capacity  number of players
     * @param nicknames nicknames of players already in the lobby
     */
    public LobbyDTO(int id, int capacity, List<String> nicknames) {
        this.id = id;
        this.capacity = capacity;
        this.nicknames = List.copyOf(nicknames);
    }

    /**
     * Returns the unique identifier of this lobby.
     *
     * @return lobby ID
     */
    public int getId() { return id; }

    /**
     * Returns the number of players this lobby can accommodate.
     *
     * @return lobby capacity
     */
    public int getCapacity() { return capacity; }

    /**
     * Returns an immutable list of the nicknames of players already in the
     * lobby.
     *
     * @return list of joined player nicknames
     */
    public List<String> getNicknames() { return nicknames; }

    /**
     * Returns the number of players currently in the lobby.
     *
     * @return current player count
     */
    public int getCurrPlayers() { return nicknames.size(); }
}