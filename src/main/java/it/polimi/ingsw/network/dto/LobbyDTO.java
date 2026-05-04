package it.polimi.ingsw.network.dto;


import java.io.Serializable;
import java.util.List;


public class LobbyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final int capacity;
    private final List<String> nicknames;

    public LobbyDTO(int id, int capacity, List<String> nicknames) {
        this.id = id;
        this.capacity = capacity;
        this.nicknames = List.copyOf(nicknames); // Immutabile per sicurezza
    }

    public int getId() { return id; }
    public int getCapacity() { return capacity; }
    public List<String> getNicknames() { return nicknames; }
    public int getCurrPlayers() { return nicknames.size(); }
}