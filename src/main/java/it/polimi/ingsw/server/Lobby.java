package it.polimi.ingsw.server;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.model.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a game lobby waiting for players before a match starts.
 *
 * <p>A lobby has a fixed capacity; once all slots are filled
 * ({@link #checkFullLobby()} returns {@code true}), the
 * {@link MatchManager} starts the match by calling {@link #buildPlayers()}.
 */
public class Lobby {

    /** Unique identifier of this lobby (equals the match ID that will be assigned). */
    private final int id;

    /** Number of players this lobby can accommodate. */
    private final int capacity;

    /** Number of players who have joined so far. */
    private int nCurrPlayers;

    /** Nicknames of players who have already joined, in join order. */
    private final List<String> nicknames = new ArrayList<>();

    /**
     * Creates a new lobby with the given ID and player capacity.
     *
     * @param id       unique lobby / match identifier
     * @param capacity number of players allowed
     */
    public Lobby(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.nCurrPlayers = 0;
    }

    /**
     * Returns the unique identifier of this lobby.
     *
     * @return lobby ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the maximum number of players this lobby can accommodate.
     *
     * @return player capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Attempts to add a player to the lobby.
     *
     * @param nickname the nickname of the player to add
     */
    public void addPlayer(String nickname) {
        if (nCurrPlayers < capacity) {
            nicknames.add(nickname);
            nCurrPlayers++;
        }
    }

    /**
     * Returns whether all player slots in the lobby are filled.
     *
     * @return {@code true} if the lobby is full and the match can start
     */
    public boolean checkFullLobby() {
        return nCurrPlayers == capacity;
    }

    /**
     * Builds and returns the list of {@link Player} objects for the match,
     * assigning each player a randomly shuffled pawn color.
     *
     * <p>Should be called only after {@link #checkFullLobby()} returns
     * {@code true}.
     *
     * @return list of players ready to start the match
     */
    public List<Player> buildPlayers() {
        List<Player> players = new ArrayList<>();
        List<ColorPawnEnum> availableColors = new ArrayList<>(Arrays.asList(ColorPawnEnum.values()));
        Collections.shuffle(availableColors);
        for (int i = 0; i < capacity; i++) {
            Player p = new Player(nicknames.get(i), availableColors.get(i));
            players.add(p);
        }
        return players;
    }

    /**
     * Returns a copy of the list of nicknames of players currently in the
     * lobby, in join order.
     *
     * @return list of player nicknames
     */
    public List<String> getNicknames() {
        return new ArrayList<>(nicknames);
    }
}