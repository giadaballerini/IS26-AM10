package it.polimi.ingsw.model.entities.tile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.entities.card.effects.instant.CardEffectInstant;
import it.polimi.ingsw.model.entities.card.effects.interactive.CardEffectInteractive;
import it.polimi.ingsw.model.entities.pawn.Pawn;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private boolean occupied;
    private char id;
    private int minPlayers;
    private Pawn pawn;
    private final List<CardEffectInstant> autoEffects;
    private final List<CardEffectInteractive> interactiveEffects;

    @JsonCreator
    public Tile(@JsonProperty("id") char id,@JsonProperty("minPlayers") int minPlayers,
                @JsonProperty("autoEffects") List<CardEffectInstant> autoEffects,
                @JsonProperty("interactiveEffects") List<CardEffectInteractive> interactiveEffects){
        this.id = id;
        this.minPlayers = minPlayers;
        this.occupied = false;
        this.pawn = null;
        this.autoEffects = autoEffects;
        this.interactiveEffects = interactiveEffects;
    }

    public Tile(Tile other){
        occupied = false;
        id = other.id;
        minPlayers = other.minPlayers;
        pawn = null;
        autoEffects = new ArrayList<>(other.autoEffects);
        interactiveEffects = new ArrayList<>(other.interactiveEffects);
    }
    public void execInstantEffect(){

    }
    public void execInteractiveEffect(){

    }

    public Pawn getPawn(){
        return pawn;
    }

    public boolean isOccupied(){
        return occupied;
    }

    public void setPawn(Pawn p){
        this.pawn = p;
        occupied = true;
    }

    public int getMinPlayers(){
        return minPlayers;
    }

    public char getId(){
        return id;
    }

}
