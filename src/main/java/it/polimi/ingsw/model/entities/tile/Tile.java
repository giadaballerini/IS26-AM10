package it.polimi.ingsw.model.entities.tile;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.TileDTO;

import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility  = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Tile {
    private boolean occupied;
    private final int id;
    private final int minPlayers;
    @JsonIgnore
    private Player player;
    private final List<CardEffectInstant> instantEffects;
    private final List<CardEffectInteractive> interactiveEffects;
    private String playerNickname;

    @JsonCreator
    public Tile(@JsonProperty("id") int id,
                @JsonProperty("minPlayers") int minPlayers,
                @JsonProperty("instantEffects") List<CardEffectInstant> instantEffects,
                @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects,
                @JsonProperty("occupied") boolean occupied,
                @JsonProperty("playerNickname") String playerNickname) {
        this.id = id;
        this.minPlayers = minPlayers;
        this.instantEffects = instantEffects != null ? instantEffects : new ArrayList<>();
        this.interactiveEffects = interactiveEffects != null ? interactiveEffects : new ArrayList<>();
        this.occupied = occupied;
        this.playerNickname = playerNickname != null ? playerNickname : "";
    }

    public void execInstantEffect(){
        for (CardEffectInstant instant : instantEffects) {
            instant.apply(player);
        }
    }

    public List<Action> execInteractiveEffect(){
        List<Action> actions = new ArrayList<>();
        for(CardEffectInteractive e : interactiveEffects){
            Action a = e.apply(getPlayer());
            actions.add(a);
        }
        return actions;
    }

    @JsonIgnore
    public Player getPlayer(){
        return player;
    }

    public boolean isOccupied(){
        return occupied;
    }

    public void setPlayer(Player p){
        this.player = p;
        this.playerNickname = (p != null) ? p.getNickname() : "";
        occupied = (p != null);
    }

    public int getMinPlayers(){
        return minPlayers;
    }

    public int getId(){
        return id;
    }

    public void removePlayer(){
        this.setPlayer(null);
    }

    public void applyQueueBonus(Player p) {
        boolean hasFoodEffect = instantEffects.stream().anyMatch(CardEffectInstant::isFoodEffect);
        p.applyQueueFoodBonus(hasFoodEffect);
    }

    public TileDTO toDTO(){
        int upDraws = interactiveEffects.stream().mapToInt(CardEffectInteractive::getUpDraws).sum();
        int downDraws = interactiveEffects.stream().mapToInt(CardEffectInteractive::getDownDraws).sum();
        int food = instantEffects.stream().mapToInt(CardEffectInstant::getFoodAmount).sum();

        if(isOccupied()){
            return new TileDTO(occupied, id, minPlayers, player.getNickname(), upDraws, downDraws, food);
        }
        return new TileDTO(occupied, id, minPlayers, "", upDraws, downDraws, food);
    }

    public String getPlayerNickname(){
        return playerNickname != null ? playerNickname : "";
    }
}