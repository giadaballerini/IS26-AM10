package it.polimi.ingsw.model.entities.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.enumerations.GamePhaseEnum;
import it.polimi.ingsw.model.action.Action;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private boolean occupied;
    private char id;
    private int minPlayers;
    private Player player;
    private final List<CardEffectInstant> autoEffects;
    private final List<CardEffectInteractive> interactiveEffects;

    @JsonCreator
    public Tile(@JsonProperty("id") char id,@JsonProperty("minPlayers") int minPlayers,
                @JsonProperty("instantEffects") List<CardEffectInstant> autoEffects,
                @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects){
        this.id = id;
        this.minPlayers = minPlayers;
        this.occupied = false;
        this.player = null;
        this.autoEffects = autoEffects;
        this.interactiveEffects = interactiveEffects;
    }

    public void execInstantEffect(GamePhaseEnum currPhase){
        for (int i = 0; i < autoEffects.size(); i++) {
            CardEffectInstant instant = autoEffects.get(i);
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

    public Player getPlayer(){
        return player;
    }

    public boolean isOccupied(){
        return occupied;
    }

    public void setPlayer(Player p){
        this.player = p;
        occupied = true;
    }

    public int getMinPlayers(){
        return minPlayers;
    }

    public char getId(){
        return id;
    }

    public void removePlayer(){
        this.setPlayer(null);
        occupied = false;
    }
}
