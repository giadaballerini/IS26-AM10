package it.polimi.ingsw.persistency;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.DrawCardEnum;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.entities.card.Card;
import it.polimi.ingsw.model.entities.tile.Tile;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

public class GameSnapshot {
    private final int matchId;
    private final int numPlayers;

    private final List<Card> deck;
    private final List<Card> buildings;
    private final List<Card> upperList;
    private final List<Card> lowerList;
    private final List<Tile> board;
    private final List<Tile> queue;
    private final List<Player> players;
    private final List<PendingAction> toDoActions;
    private final String currentPlayerNickname;

    private final GamePhaseEnum currentPhase;
    private final int currAge;
    private final int currTurn;
    private final boolean skippableDraw;

    @JsonCreator
    public GameSnapshot(
            @JsonProperty("matchId") int matchId,
            @JsonProperty("numPlayers") int numPlayers,
            @JsonProperty("deck") List<Card> deck,
            @JsonProperty("buildings") List<Card> buildings,
            @JsonProperty("upperList") List<Card> upperList,
            @JsonProperty("lowerList") List<Card> lowerList,
            @JsonProperty("board") List<Tile> board,
            @JsonProperty("queue") List<Tile> queue,
            @JsonProperty("players") List<Player> players,
            @JsonProperty("currentPlayerNickname") String currentPlayerNickname,
            @JsonProperty("currentPhase") GamePhaseEnum currentPhase,
            @JsonProperty("currAge") int currAge,
            @JsonProperty("currTurn") int currTurn,
            @JsonProperty("skippableDraw") boolean skippableDraw,
            @JsonProperty("toDoActions") List<PendingAction> toDoActions) {

        this.matchId = matchId;
        this.numPlayers = numPlayers;
        this.deck  = deck;
        this.buildings = buildings;
        this.upperList = upperList;
        this.lowerList = lowerList;
        this.board = board;
        this.queue = queue;
        this.players = players;
        this.currentPlayerNickname = currentPlayerNickname;
        this.currentPhase = currentPhase;
        this.currAge = currAge;
        this.currTurn = currTurn;
        this.skippableDraw = skippableDraw;
        this.toDoActions = toDoActions;
    }

    public int getMatchId(){ return matchId; }
    public int getNumPlayers(){ return numPlayers; }
    public List<Card> getDeck(){ return deck; }
    public List<Card> getBuildings(){ return buildings; }
    public List<Card> getUpperList(){ return upperList; }
    public List<Card> getLowerList(){ return lowerList; }
    public List<Tile> getBoard(){ return board; }
    public List<Tile> getQueue(){ return queue; }
    public List<Player> getPlayers(){ return players; }
    public String getCurrentPlayerNickname(){ return currentPlayerNickname; }
    public GamePhaseEnum getCurrentPhase(){ return currentPhase; }
    public int getCurrAge(){ return currAge; }
    public int getCurrTurn(){ return currTurn; }
    public boolean isSkippableDraw(){ return skippableDraw; }
    public List<PendingAction> getToDoActions(){ return toDoActions; }


    public static class PendingAction {

        private final String ownerNickname;
        private final DrawCardEnum type;

        @JsonCreator
        public PendingAction(
                @JsonProperty("ownerNickname") String ownerNickname,
                @JsonProperty("type") DrawCardEnum type) {
            this.ownerNickname = ownerNickname;
            this.type= type;
        }

        public String getOwnerNickname() { return ownerNickname; }
        public DrawCardEnum getType() { return type; }
    }
}


