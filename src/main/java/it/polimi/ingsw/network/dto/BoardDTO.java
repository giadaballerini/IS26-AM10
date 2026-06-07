package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.enumerations.GamePhaseEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BoardDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    private final List<CardDTO> upperList;
    private final List<CardDTO> lowerList;
    private final List<PlayerDTO> players;
    private final List<PlayerStatsDTO> playerStats;
    private final ActionsDTO toDoActions;
    private final List<TileDTO> boardTiles;
    private final List<TileDTO> queueTiles;


    private final String currentPlayerNickname;
    private final GamePhaseEnum currentPhase;
    private final int currTurn;
    private final int numPlayers;
    private final int deckSize;

    public BoardDTO(List<CardDTO> upperList, List<CardDTO> lowerList, List<PlayerDTO> players,
                    List<TileDTO> boardTiles, List<TileDTO> queueTiles, List<PlayerStatsDTO> playerStats,
                    String currentPlayerNickname,ActionsDTO toDoActions, GamePhaseEnum currentPhase,
                    int currTurn, int numPlayers, int deckSize) {
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.players = players;
        this.boardTiles = boardTiles;
        this.queueTiles = queueTiles;
        this.playerStats = playerStats;
        this.currentPlayerNickname = currentPlayerNickname;
        this.toDoActions = toDoActions;
        this.currentPhase = currentPhase;
        this.currTurn = currTurn;
        this.numPlayers = numPlayers;
        this.deckSize = deckSize;
    }

    public List<CardDTO> getUpperList() {
        return new ArrayList<>(upperList);
    }

    public List<CardDTO> getLowerList() {
        return new ArrayList<>(lowerList);
    }

    public List<TileDTO> getboardTiles() {
        return new ArrayList<>(boardTiles);
    }

    public List<TileDTO> getqueueTiles() {
        return new ArrayList<>(queueTiles);
    }

    public List<PlayerDTO> getPlayers() {return new ArrayList<>(players);}

    public String getCurrentPlayerNickname() {
        return currentPlayerNickname;
    }

    public List<PlayerStatsDTO> getPlayerStats() {return new ArrayList<>(playerStats);}

    public GamePhaseEnum getCurrentPhase() {
        return this.currentPhase;
    }

    public int getCurrTurn() {return currTurn;}

    public int getNumPlayers(){return numPlayers;}

    public int getDeckSize() {
        return deckSize;
    }

    public ActionsDTO getTodoActions() {
        return new ActionsDTO(toDoActions);
    }
}
