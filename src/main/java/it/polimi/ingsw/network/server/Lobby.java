package it.polimi.ingsw.network.server;

import it.polimi.ingsw.enumerations.ColorPawnEnum;
import it.polimi.ingsw.model.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Lobby {
    private int id;
    private int capacity;
    private int nCurrPlayers;
    private List<String> nicknames = new ArrayList<>();

    public Lobby(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.nCurrPlayers = 0;
    }

    public int getId(){
        return id;
    }

    public int getCapacity(){
        return capacity;
    }

    public boolean addPlayer(String nickname){
        if(nCurrPlayers < capacity) {
            nicknames.add(nickname);
            nCurrPlayers++;
            return true;
        }
        return false;
    }

    public boolean checkFullLobby(){
        return nCurrPlayers == capacity;
    }

    public List<Player> buildPlayers(){
        List <Player> players = new ArrayList<>();
        List<ColorPawnEnum> availableColors = new ArrayList<>(Arrays.asList(ColorPawnEnum.values()));

        Collections.shuffle(availableColors);

        for(int i = 0; i < capacity; i++){
            Player p = new Player(nicknames.get(i), availableColors.get(i));
            players.add(p);
        }
        return players;
    }

    public List<String> getNicknames(){
        return new ArrayList<>(nicknames);
    }
}
